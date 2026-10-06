# Flujo de conexión de la app Kaffa — módulo **Pedidos**

Documento de apoyo para la presentación en draw.io.
Archivo del diagrama: `docs/diagrama_flujo_pedidos.drawio`

---

## 0. Advertencia importante antes de presentar

Este repositorio (`movil-master`) contiene **solo el cliente Android (Kotlin + Jetpack Compose)**.
El backend es una **API REST externa en Laravel** que corre en la red local:

```
http://192.168.80.15:8000/api/v1/
```

Evidencia de que el backend es Laravel:

- `util/ApiErrors.kt:8` → el comentario dice literalmente *"Lectura uniforme de errores de la **API Laravel**"*.
- Formato de paginación de Laravel (`data` + `meta.current_page`, `meta.last_page`, `total`).
- Autenticación `Authorization: Bearer` (Sanctum / Passport).

Por eso, la parte del servidor que se explica abajo está **reconstruida a partir del contrato que el cliente exige**.
El backend **no está en este repositorio**. Si tienes el código Laravel a mano, reemplaza los bloques de la sección 5 por el código real y la explicación queda exacta.

---

## 1. Panorama general (diagrama ASCII)

```
┌───────────────────────── APP ANDROID (Kotlin) ─────────────────────────┐
│                                                                        │
│  1. UI (Jetpack Compose)         POSScreen.kt / OrderListScreen.kt     │
│            │                                                           │
│            ▼                                                           │
│  2. ViewModel (MVVM)            POSViewModel.createOrder()             │
│            │                                                           │
│            ▼                                                           │
│  3. Repository                  OrderRepositoryImpl                     │
│            │                                                           │
│            ▼                                                           │
│  4. Interfaz Retrofit           OrderApi  (@GET/@POST "pedidos")        │
│            │                                                           │
│            ▼                                                           │
│  5. Interceptores OkHttp        AuthInterceptor → Bearer <JWT>          │
│            │                     DebugInterceptor → log (solo debug)   │
│            ▼                                                           │
│  6. Base URL                    Constants.BASE_URL                      │
└────────────────────────────┬───────────────────────────────────────────┘
                             │
                    ═══ HTTP / JSON ═══   (LAN, cleartext)
                             │
┌────────────────────────────▼───────────────────────────────────────────┐
│                  BACKEND LARAVEL  (fuera del repo)                     │
│                                                                        │
│  7. Rutas + Middleware          routes/api.php → auth:sanctum           │
│            ▼                                                           │
│  8. Controller                 PedidoController@store                   │
│            ▼                                                           │
│  9. Servicio / Reglas          PedidoService (caja abierta, turno...)  │
│            ▼                                                           │
│ 10. Modelos Eloquent           Pedido, PedidoDetalle, PagoPedido,      │
│                                FacturaVenta                            │
│            ▼                                                           │
│ 11. Base de datos              MySQL / MariaDB                         │
│            ▼                                                           │
│ 12. Resource / Transformer      PedidoResource → JSON                   │
└────────────────────────────┬───────────────────────────────────────────┘
                             │
                    ═══ 200 OK / JSON ═══
                             │
        (vuelve por la misma cadena y se deserializa con Gson)
                             ▼
  GsonConverterFactory → Response<PedidoDto> → Resource<T>
      → ViewModel actualiza mutableStateOf(UiState)
      → Jetpack Compose recompone la pantalla
```

---

## 2. Configuración de la conexión

### 2.1 URL base y token

**`app/src/main/java/com/example/kaffacafeteria/util/Constants.kt:14-22`**

```kotlin
private const val HOST_EMULATOR  = "10.0.2.2"        // emulador Android
private const val HOST_PHYSICAL = "192.168.80.15"    // celular físico -> IP de la PC
private val host: String = if (isEmulator) HOST_EMULATOR else HOST_PHYSICAL

val BASE_URL: String = "http://$host:8000/api/v1/"
val IMAGE_BASE_URL: String = "http://$host:8000/"

const val PREF_NAME  = "kaffa_prefs"
const val TOKEN_KEY  = "auth_token"
const val PAGE_SIZE  = 15
```

> Nota para la presentación: **no hay `.env` ni inyección por build config**. La IP está fija en código,
> así que la app solo funciona en esa red LAN.

**`app/src/main/AndroidManifest.xml:18`**

```xml
android:usesCleartextTraffic="true"
```

Es obligatorio porque el backend habla HTTP sin cifrar.

### 2.2 Configuración de Retrofit + OkHttp (DI manual)

**`app/src/main/java/com/example/kaffacafeteria/KaffaApp.kt:21-54`**

```kotlin
class AppContainer(context: Application) {
    val tokenManager = TokenManager(context)
    val cartStore    = CartStore()

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor(tokenManager))   // inyecta el token
        .addInterceptor(DebugInterceptor.create())       // log de red
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(Constants.BASE_URL)                     // <- url del backend
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())  // JSON <-> Kotlin
        .build()

    val orderApi: OrderApi = retrofit.create(OrderApi::class.java)
    // ...
    val orderRepository: OrderRepository = OrderRepositoryImpl(orderApi)
}

class KaffaApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)   // se construye una sola vez al abrir la app
    }
}
```

Puntos clave: **no se usa Hilt ni Koin**, el contenedor de dependencias se arma a mano en `Application`.
Ese objeto `retrofit` es quien "sabe" la dirección del servidor.

---

## 3. Los endpoints de Pedidos

**`app/src/main/java/com/example/kaffacafeteria/data/remote/api/OrderApi.kt`**

```kotlin
interface OrderApi {

    @GET("pedidos")
    suspend fun getPedidos(
        @Query("per_page")   perPage:   Int?    = null,
        @Query("estado")     estado:    String? = null,
        @Query("sort_by")    sortBy:    String? = null,
        @Query("sort_order") sortOrder: String? = null
    ): Response<PaginatedResponse<PedidoDto>>

    @GET("pedidos/{id}")
    suspend fun getPedido(@Path("id") id: Int): Response<PedidoDto>

    @POST("pedidos")
    suspend fun createPedido(@Body request: PedidoRequest): Response<PedidoDto>

    @PUT("pedidos/{id}")
    suspend fun updatePedido(
        @Path("id") id: Int,
        @Body request: PedidoUpdateRequest
    ): Response<PedidoDto>

    @DELETE("pedidos/{id}")
    suspend fun deletePedido(@Path("id") id: Int): Response<Any>
}
```

URLs efectivas (`suspend` ⇒ se ejecuta fuera del hilo principal, sin bloquear la UI):

| Método | URL completa                                                        | Uso en la app                       |
|--------|---------------------------------------------------------------------|-------------------------------------|
| GET    | `http://<host>:8000/api/v1/pedidos?per_page=15&estado=pendiente`     | Lista de pedidos, panel barista     |
| GET    | `http://<host>:8000/api/v1/pedidos/{id}`                             | Detalle de un pedido                |
| POST   | `http://<host>:8000/api/v1/pedidos`                                  | **Crear el pedido desde el POS**    |
| PUT    | `http://<host>:8000/api/v1/pedidos/{id}`                             | Cambiar estado (pendiente→pagado…) |
| DELETE | `http://<host>:8000/api/v1/pedidos/{id}`                             | Borrar pedido                       |

---

## 4. El viaje completo de un `POST /pedidos` (capa por capa)

### Capa 1 — UI (Jetpack Compose)

El usuario arma el carrito en el punto de venta y pulsa **Cobrar**.
Ese evento entra al ViewModel. No hay llamada de red desde la UI.

### Capa 2 — ViewModel: arma el pedido y lanza la corrutina

**`app/src/main/java/com/example/kaffacafeteria/ui/pos/POSViewModel.kt:150-221`**

```kotlin
fun createOrder() {
    val medioPagoId = uiState.selectedMedioPagoId ?: run {
        uiState = uiState.copy(error = "Selecciona un método de pago válido")
        return
    }
    val medioPago = uiState.mediosPago.find { it.id == medioPagoId } ?: run { ... }

    // Regla de negocio validada en el CLIENTE
    if (medioPago.esVirtual && uiState.comprobanteUrl.isBlank()) {
        uiState = uiState.copy(error = "El comprobante es requerido para pagos virtuales")
        return
    }

    val total = cartStore.total
    val detalles = cartStore.items.map { item ->
        val precio = item.producto.precioVenta.toDoubleOrNull() ?: 0.0
        PedidoDetalleRequest(
            productoId     = item.producto.id,
            cantidad       = item.cantidad,
            precioUnitario = precio,
            subtotal       = precio * item.cantidad
        )
    }

    val pagos = listOf(
        PagoPedidoRequest(
            medioPagoId    = medioPagoId,
            monto          = total,
            comprobanteUrl = uiState.comprobanteUrl.ifBlank { null }
        )
    )

    val request = PedidoRequest(
        clienteId = uiState.user?.id ?: return,
        total     = total,
        propina   = 0.0,
        estado    = "pendiente",          // el pedido nace "pendiente"
        cajaId    = null,                 // el backend resuelve la caja abierta
        detalles  = detalles,
        pagos     = pagos,
        factura   = FacturaRequest(
            numeroFactura = "POS-${System.currentTimeMillis()}",
            subtotal = total, impuestos = 0.0, total = total
        )
    )

    viewModelScope.launch {                    // <-- corrutina: no bloquea la UI
        uiState = uiState.copy(isLoading = true, error = null)
        try {
            val result = orderApi.createPedido(request)     // <-- salta a la capa 3
            if (result.isSuccessful) {
                val order = result.body()
                uiState = uiState.copy(
                    isLoading = false,
                    showPaymentDialog = false,
                    showPaymentSuccess = true,
                    createdOrderId = order?.id
                )
                cartStore.clear()               // vacía el carrito
            } else {
                uiState = uiState.copy(
                    isLoading = false,
                    error = "Error al crear pedido: ${result.code()}"
                )
            }
        } catch (e: Exception) {
            uiState = uiState.copy(isLoading = false, error = e.message ?: "Error de conexión")
        }
    }
}
```

> Detalle importante para explicar: `viewModelScope.launch` mueve la llamada a una **corrutina**,
> por eso la pantalla puede seguir dibujándose (loading) mientras el servidor responde.

### Capa 3 — Repository: envuelve la llamada y unifica el resultado

**`app/src/main/java/com/example/kaffacafeteria/data/repository/OrderRepositoryImpl.kt`**

```kotlin
class OrderRepositoryImpl(private val orderApi: OrderApi) : OrderRepository {

    override suspend fun getPedidos(perPage: Int?, estado: String?):
        Resource<PaginatedResponse<PedidoDto>> = apiCall { orderApi.getPedidos(perPage, estado) }

    override suspend fun getPedido(id: Int): Resource<PedidoDto> =
        apiCall { orderApi.getPedido(id) }

    override suspend fun createPedido(request: PedidoRequest): Resource<PedidoDto> =
        apiCall { orderApi.createPedido(request) }

    override suspend fun updatePedido(id: Int, request: PedidoUpdateRequest): Resource<PedidoDto> =
        apiCall { orderApi.updatePedido(id, request) }

    override suspend fun deletePedido(id: Int): Resource<Any> =
        apiCall { orderApi.deletePedido(id) }

    private suspend fun <T> apiCall(call: suspend () -> retrofit2.Response<T>): Resource<T> {
        return try {
            val response = call()
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else                     Resource.Error("Error en la petición", response.code())
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error de conexión")
        }
    }
}
```

El `Resource<T>` (`util/Resource.kt`) es un tipo sellado con tres estados:

```kotlin
// util/Resource.kt
sealed class Resource<out T> {
    data object Loading : Resource<Nothing>()
    data class Success<T>(val data: T) : Resource<T>

    /**
     * @param mensaje   texto listo para mostrar al usuario
     * @param code      código HTTP de la respuesta
     * @param codigoApi código de regla de negocio del backend
     *                 (EMAIL_NOT_VERIFIED, CAJA_CERRADA, ...)
     */
    data class Error(
        val message:   String,
        val code:      Int?    = null,
        val codigoApi: String? = null
    ) : Resource<Nothing>()
}
```

> ⚠ **Bug que puedes mencionar en la presentación:**
> `apiCall` en la línea 40 **descarta el cuerpo del error** y devuelve el texto genérico
> `"Error en la petición"`. Por eso los códigos de regla de negocio del backend
> (por ejemplo `CAJA_CERRADA`) **nunca llegan a las pantallas de pedidos**.
> En `AuthRepositoryImpl` sí se usa `ApiErrors.parse(response)`, que sí los lee.
>
> ⚠ También hay `response.body()!!`, que revienta con NPE en un `2xx` sin cuerpo (típico en `DELETE`).

### Capa 4 — DTOs: el contrato JSON cliente ↔ servidor

**`app/src/main/java/com/example/kaffacafeteria/data/remote/dto/OrderDtos.kt`**

```kotlin
// ---------- lo que VIA (request) ----------
data class PedidoRequest(
    @SerializedName("cliente_id") val clienteId: Int,
    val total: Double,
    val propina: Double?,
    val estado: String,
    @SerializedName("caja_id") val cajaId: Int?,
    val detalles: List<PedidoDetalleRequest>,
    val pagos: List<PagoPedidoRequest>,
    val factura: FacturaRequest?
)

data class PedidoDetalleRequest(
    @SerializedName("producto_id")     val productoId: Int,
    val cantidad: Int,
    @SerializedName("precio_unitario") val precioUnitario: Double,
    val subtotal: Double
)

data class PagoPedidoRequest(
    @SerializedName("medio_pago_id")    val medioPagoId: Int,
    val monto: Double,
    @SerializedName("comprobante_url")  val comprobanteUrl: String? = null
)

// ---------- lo que VUELVE (response) ----------
data class PedidoDto(
    val id: Int,
    @SerializedName("cliente_id") val clienteId: Int,
    val total: String,                  // OJO: el backend devuelve los montos como STRING
    val propina: String?,
    val estado: String,
    @SerializedName("caja_id")     val cajaId: Int?,
    @SerializedName("barista_id")  val baristaId: Int?,
    val detalles: List<PedidoDetalleDto>?,
    val pagos: List<PagoPedidoDto>?,
    @SerializedName("factura_venta") val facturaVenta: FacturaVentaDto?,
    val cliente: UsuarioDto?,
    val barista: UsuarioDto?,
    val created_at: String?,
    val updated_at: String?
)
```

**Paginación (formato Laravel)** — `data/remote/dto/CommonDtos.kt:5-15`

```kotlin
data class PaginatedResponse<T>(val data: List<T>, val meta: MetaData)

data class MetaData(
    @SerializedName("current_page") val currentPage: Int,
    @SerializedName("last_page")   val lastPage: Int,
    val total: Int,
    val perPage: Int?     // <-- le falta @SerializedName("per_page"): hoy nunca se llena
)
```

> `@SerializedName` es la pieza clave: Kotlin usa camelCase, Laravel usa snake_case.
> Gsontraduce las claves del JSON a los campos del DTO.

### Capa 5 — Interceptores: la petición sale por aquí

**`app/src/main/java/com/example/kaffacafeteria/data/remote/interceptor/AuthInterceptor.kt`**

```kotlin
class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { tokenManager.getToken() }
        val request = if (token != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")   // <-- token JWT
                .addHeader("Accept", "application/json")
                .build()
        } else {
            chain.request().newBuilder()
                .addHeader("Accept", "application/json")
                .build()
        }
        return chain.proceed(request)   // <-- recién aquí sale la petición por la red
    }
}
```

**`data/remote/interceptor/DebugInterceptor.kt`**

```kotlin
object DebugInterceptor {
    fun create(): HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
               else                        HttpLoggingInterceptor.Level.NONE
        redactHeader("Authorization")   // nunca imprime el token
    }
}
```

Orden de ejecución de los interceptores:

```
Petición del ViewModel
   → AuthInterceptor  (agrega Authorization: Bearer <JWT>)
      → DebugInterceptor (log, redacta el token)
         → INTERNET (socket HTTP)
            ← respuesta del backend
         ← DebugInterceptor (log de respuesta)
      ← AuthInterceptor
   ← Response<PedidoDto> (Gson ya la convirtió a Kotlin)
```

### Petición y respuesta reales

```http
POST /api/v1/pedidos HTTP/1.1
Host: 192.168.80.15:8000
Accept: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

```json
{
  "cliente_id": 12,
  "total": 45.50,
  "propina": 0.0,
  "estado": "pendiente",
  "caja_id": null,
  "detalles": [
    { "producto_id": 7, "cantidad": 2, "precio_unitario": 18.00, "subtotal": 36.00 },
    { "producto_id": 9, "cantidad": 1, "precio_unitario": 9.50,  "subtotal": 9.50  }
  ],
  "pagos": [
    { "medio_pago_id": 2, "monto": 45.50, "comprobante_url": "http://.../comprobante.jpg" }
  ],
  "factura": {
    "numero_factura": "POS-1730400000000",
    "subtotal": 45.50, "impuestos": 0.0, "total": 45.50
  }
}
```

```http
HTTP/1.1 201 Created
Content-Type: application/json
```

```json
{
  "data": {
    "id": 348,
    "cliente_id": 12,
    "total": "45.50",
    "estado": "pendiente",
    "caja_id": 2,
    "barista_id": null,
    "created_at": "2026-09-30T16:42:11.000000Z"
  },
  "message": "Pedido creado correctamente"
}
```

> Nota: el POST devuelve `PedidoDto` **directo**, no envuelto en `PaginatedResponse`.
> El envoltorio `data` + `message` es el estándar de Laravel; el cliente solo lee `data`.

---

## 5. El backend (Laravel) — reconstruido desde el contrato del cliente

> No está en este repo. Lo que sigue es el código Laravel típico que el cliente **exige** que exista.
> Si tienes el backend, sustitúyelo por el real.

### 5.1 Rutas

```php
// routes/api.php
Route::middleware('auth:sanctum')->group(function () {
    Route::get('/pedidos',        [PedidoController::class, 'index']);
    Route::get('/pedidos/{id}',   [PedidoController::class, 'show']);
    Route::post('/pedidos',       [PedidoController::class, 'store']);
    Route::put('/pedidos/{id}',   [PedidoController::class, 'update']);
    Route::delete('/pedidos/{id}',[PedidoController::class, 'destroy']);
});
```

### 5.2 Middleware: validar el token

```php
public function handle(Request $request, Closure $next)
{
    $user = $request->user();          // lee el header Authorization: Bearer <JWT>
    if (!$user) return response()->json(['message' => 'No autorizado'], 401);
    return $next($request);
}
```

### 5.3 Controller: recibe, valida, responde

```php
public function store(Request $request)
{
    $data = $request->validate([
        'cliente_id'          => 'required|integer|exists:usuarios,id',
        'total'               => 'required|numeric',
        'propina'             => 'nullable|numeric',
        'estado'              => 'required|in:pendiente,pagado,en_preparacion,entregado,cancelado',
        'caja_id'             => 'nullable|integer|exists:cajas,id',
        'detalles'            => 'required|array|min:1',
        'detalles.*.producto_id'     => 'required|integer|exists:productos,id',
        'detalles.*.cantidad'        => 'required|integer|min:1',
        'detalles.*.precio_unitario' => 'required|numeric',
        'detalles.*.subtotal'        => 'required|numeric',
        'pagos'               => 'required|array|min:1',
        'pagos.*.medio_pago_id'      => 'required|integer|exists:medios_pago,id',
        'pagos.*.monto'              => 'required|numeric',
        'factura'             => 'nullable|array',
    ]);

    $pedido = $this->pedidoService->crear($data);

    return response()->json([
        'data'    => new PedidoResource($pedido),
        'message' => 'Pedido creado correctamente',
    ], 201);
}
```

### 5.4 Servicio: las reglas de negocio (aquí está el valor real)

Reglas que el cliente da por hecho (se deducen de la UI):

| Regla | Dónde se ve en el cliente |
|---|---|
| El barista necesita **turno activo** | `ui/gestion/TurnosScreen.kt:74` → *"Los pedidos requieren que el barista tenga un turno activo"* |
| La **caja debe estar abierta** | error de negocio `CAJA_CERRADA` en `util/Resource.kt:11` |
| `caja_id` llega `null` → el backend lo resuelve | `POSViewModel.kt:190`, `ClientViewModel.kt:167` |
| Pagos virtuales exigen `comprobante_url` | `POSViewModel.kt:161-164` |

```php
class PedidoService
{
    public function crear(array $data): Pedido
    {
        return DB::transaction(function () use ($data) {
            $caja = Caja::where('estado', 'abierta')
                        ->where('sucursal_id', auth()->user()->sucursal_id)
                        ->lockForUpdate()
                        ->first();

            if (!$caja) {
                // El cliente NO puede leer este código hoy (ver bug en OrderRepositoryImpl)
                throw new BusinessException('La caja está cerrada', 'CAJA_CERRADA');
            }
            if (!auth()->user()->turnoActivo()) {
                throw new BusinessException('Requiere turno activo', 'TURNO_INACTIVO');
            }

            $pedido = Pedido::create([
                'cliente_id' => $data['cliente_id'],
                'total'      => $data['total'],
                'propina'    => $data['propina'] ?? 0,
                'estado'     => 'pendiente',
                'caja_id'    => $caja->id,          // <- el backend resuelve la caja
                'barista_id' => auth()->id(),
            ]);

            foreach ($data['detalles'] as $d) {
                PedidoDetalle::create($pedido->id, $d);
            }
            foreach ($data['pagos'] as $p) {
                PagoPedido::create($pedido->id, $p);
            }
            FacturaVenta::create($pedido->id, $data['factura'] ?? null);

            return $pedido->load('detalles', 'pagos', 'facturaVenta', 'cliente');
        });
    }
}
```

> `DB::transaction` = todo o nada: si falla el insert de un detalle, no queda ningún pedido a medias.
> `lockForUpdate()` = bloquea la fila de la caja para que dos cajeros no creen pedidos al mismo tiempo.

### 5.5 Modelos y base de datos

```
pedidos
 ├─ id, cliente_id → usuarios.id
 ├─ total, propina, estado
 ├─ caja_id     → cajas.id
 ├─ barista_id  → usuarios.id
 └─ timestamps

pedido_detalles   → pedidos.id, producto_id → productos.id, cantidad, precio_unitario, subtotal
pago_pedidos      → pedidos.id, medio_pago_id → medios_pago.id, monto, comprobante_url
factura_ventas    → pedidos.id, numero_factura, subtotal, impuestos, total
```

### 5.6 Resource: el JSON que el cliente deserializa

```php
class PedidoResource extends JsonResource
{
    public function toArray(Request $request): array
    {
        return [
            'id'           => $this->id,
            'cliente_id'   => $this->cliente_id,
            'total'        => (string) $this->total,     // STRING a propósito: coincide con PedidoDto
            'propina'      => (string) $this->propina,
            'estado'       => $this->estado,
            'caja_id'      => $this->caja_id,
            'barista_id'   => $this->barista_id,
            'detalles'     => PedidoDetalleResource::collection($this->detalles),
            'pagos'        => PagoPedidoResource::collection($this->pagos),
            'factura_venta'=> new FacturaVentaResource($this->facturaVenta),
            'created_at'   => optional($this->created_at)->toDateTimeString(),
            'updated_at'   => optional($this->updated_at)->toDateTimeString(),
        ];
    }
}
```

---

## 6. Autenticación (el token en todo el viaje)

### 6.1 El login

**`data/remote/api/AuthApi.kt`** → `POST api/v1/login`
**`data/remote/dto/AuthDtos.kt:48-60`**

```kotlin
data class LoginResponse(
    @SerializedName("access_token")  val accessToken: String,
    @SerializedName("token_type")    val tokenType: String? = null,
    val usuario: UsuarioDto,
    @SerializedName("turno_activo")  val turnoActivo:  Boolean? = null,
    @SerializedName("caja_abierta")  val cajaAbierta:  Boolean? = null,
    @SerializedName("puede_operar")  val puedeOperar:  Boolean? = null,
    @SerializedName("motivo_bloqueo") val motivoBloqueo: String? = null
)
```

> Optimización: el backend ya devuelve el estado operativo del barista en el login,
> así la app **no necesita una segunda llamada** para saber si puede cobrar.

### 6.2 Dónde se guarda

**`data/repository/AuthRepositoryImpl.kt:27-44`**

```kotlin
override suspend fun login(correo: String, password: String): Resource<User> {
    return try {
        val response = authApi.login(LoginRequest(correo, password))
        if (response.isSuccessful) {
            val loginResponse = response.body()!!
            tokenManager.saveToken(loginResponse.accessToken)   // <-- se guarda en DataStore
            Resource.Success(loginResponse.usuario.toDomain())
        } else {
            Resource.Error(
                ApiErrors.parse(response, "No se pudo iniciar sesión"),
                response.code(),
                ApiErrors.code(response)
            )
        }
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Error de conexión")
    }
}
```

El token se guarda en **Jetpack DataStore** con la clave `auth_token` (`Constants.kt:20`).

> ⚠ Se guarda **en texto plano**. La dependencia `security-crypto` está declarada en
> `app/build.gradle.kts:88` pero **nunca se usa**.
> ⚠ No hay lógica de **refresco**: si el token expira (401) ya no se reintenta, pero sí hay
> cierre de sesión automático — `AuthInterceptor` borra el token y `AppNavigation` navega al login.

---

## 7. Manejo de errores (el backend habla 3 dialects)

**`app/src/main/java/com/example/kaffacafeteria/util/ApiErrors.kt`**

```kotlin
/**
 * Lectura uniforme de errores de la API Laravel.
 *
 * El backend responde con distintos formatos según el origen del error:
 *  - {"message": "..."}                        → mensaje simple (422 del login)
 *  - {"message": "...", "code": "CAJA_CERRADA"} → error de reglas de negocio
 *  - {"message": {...}, "errors": {...}}       → validación de formulario (422)
 */
object ApiErrors {

    fun parse(response: Response<*>?, fallback: String = "Ocurrió un error"): String {
        val body = runCatching { response?.errorBody()?.string() }.getOrNull()
        if (body.isNullOrBlank()) return fallback

        return try {
            val json = gson.fromJson(body, JsonObject::class.java) ?: return fallback

            // Validación: el primer mensaje de "errors" es el más útil para el usuario
            json.getAsJsonObject("errors")?.entrySet()?.firstOrNull()?.value
                ?.let { array ->
                    runCatching { array.asJsonArray[0].asString }.getOrNull()
                        ?.takeIf { it.isNotBlank() }
                        ?.let { return it }
                }

            when {
                json.has("error")   && !json.get("error").isJsonNull   -> json.get("error").asString
                json.has("message") && json.get("message").isJsonPrimitive -> json.get("message").asString
                json.has("message") && json.get("message").isJsonObject ->
                    json.getAsJsonObject("message").entrySet().firstOrNull()?.value?.asString
                        ?: fallback
                else -> fallback
            }
        } catch (e: Exception) { fallback }
    }

    /** Extrae el código de regla de negocio (CAJA_CERRADA, EMAIL_NOT_VERIFIED...). */
    fun code(response: Response<*>?): String? { ... }
}
```

**Los 2 caminos que existen hoy (y el problema que crean):**

| Camino | Archivos | Mensaje de error |
|---|---|---|
| **Con Repository** | `ui/orders/OrderViewModel.kt`, `ui/barista/BaristaPanelViewModel.kt` | ✅ Ya usa `ApiErrors.parse()` + `codigoApi` (mensaje real del backend) |
| **API directa** | `ui/pos/POSViewModel.kt`, `ui/cliente/ClientViewModel.kt`, `ui/admin/*` | ⚠ Parcial: POS/Cliente al crear pedido ya usan `ApiErrors.parse()`; el resto de pantallas admin muestra `"Error al cargar X (500)"` |

> Nota: `ApiErrors` lee el cuerpo de error **una sola vez** (Retrofit lo amortigua en un buffer
> de Okio de un solo uso) y lo memoriza por respuesta, así que llamar `parse()` y `code()` sobre
> la misma respuesta devuelve ambos datos.

---

## 8. Ciclo de vida de un pedido (los `PUT`)

```
                      ┌──────────────┐
   POST /pedidos ───► │  pendiente   │ ◄── se acaba de crear
                      └──────┬───────┘
                             │  PUT {estado:"pagado"}
                      ┌──────▼───────┐
                      │    pagado    │
                      └──────┬───────┘
                             │  PUT {estado:"en_preparacion"}
                      ┌──────▼───────────┐
                      │  en_preparacion  │
                      └──────┬───────────┘
                             │  PUT {estado:"entregado"}
                      ┌──────▼───────┐
                      │  entregado   │  ← el único estado que cuenta como ingreso
                      └──────────────┘

              en cualquier momento ──► PUT {estado:"cancelado"}
```

- Transiciones en la UI: `ui/orders/OrderDetailScreen.kt:104-110`
- Kanban del barista: `ui/barista/BaristaPanelScreen.kt:263-265`
  (`pendiente|pagado` → col 1, `en_preparacion` → col 2, `entregado|cancelado` → col 3)
- El panel del barista hace **polling** cada 8 s: `ui/barista/BaristaPanelViewModel.kt:59-66`

```kotlin
private suspend fun startAutoRefresh() {
    while (isActive) {
        delay(8000)      // cada 8 segundos
        loadPedidos()
    }
}
```

```kotlin
// ui/orders/OrderViewModel.kt  -> se serializa con PUT
override suspend fun updatePedido(id: Int, request: PedidoUpdateRequest) =
    orderRepository.updatePedido(id, request)

suspend fun updateEstado(id: Int, nuevoEstado: String) =
    updatePedido(id, PedidoUpdateRequest(estado = nuevoEstado))
```

---

## 9. Guion para la presentación (10 diapositivas)

1. **Título**: Kaffa – Flujo de conexión del módulo Pedidos. Stack: Android Kotlin + Jetpack Compose, Retrofit/OkHttp, Laravel REST API, MySQL.
2. **Problema**: ¿qué pasa cuando el barista cobra? Recordar: el cliente es Android nativo, el backend es Laravel y **no está en este repo**.
3. **Vista general**: el diagrama de capas (página 1 del .drawio).
4. **Configuración**: `Constants.BASE_URL` + `KaffaApp.kt` (Retrofit, interceptores, `GsonConverterFactory`).
5. **El contrato**: `OrderApi.kt` con los 5 verbos HTTP sobre `/pedidos`.
6. **Los DTOs**: `@SerializedName` hace el puente snake_case ↔ camelCase, y por qué los montos de respuesta son `String`.
7. **La ida**: `POSViewModel.createOrder()` arma el `PedidoRequest` en una corrutina.
8. **El viaje**: `AuthInterceptor` agrega `Authorization: Bearer <JWT>` y la petición sale a la LAN.
9. **El backend**: rutas → `auth:sanctum` → Controller → Service (reglas: caja abierta, turno activo) → Eloquent → MySQL → `PedidoResource`.
10. **La vuelta**: `201 Created` → Gson → `Response<PedidoDto>` → `Resource.Success` → `mutableStateOf` → Compose recompone. Más el ciclo de estados y los 3 formatos de error de Laravel.

Si te preguntan *"¿y el repo del backend?"*: está fuera de `movil-master`; el cliente lo consume como API REST black-box en `http://192.168.80.15:8000/api/v1/`.

---

## 10. Deuda técnica que puedes mencionar (pregunta común en defensas)

| # | Problema | Ubicación | Estado |
|---|----------|-----------|--------|
| 1 | `OrderRepositoryImpl` descarta el cuerpo del error → `CAJA_CERRADA` nunca se ve en pedidos | `data/repository/OrderRepositoryImpl.kt:40` | ✅ Corregido: `ApiErrors.parse()` + `codigoApi` en `Resource.Error` |
| 2 | `response.body()!!` → NPE en `2xx` sin cuerpo (típico en `DELETE`) | `data/repository/OrderRepositoryImpl.kt:38` | ✅ Corregido: `emptyBody()` para respuestas sin contenido |
| 3 | IP del servidor fija en código; no hay `.env` ni build config | `util/Constants.kt:16` | ✅ Corregido: `KAFFA_API_HOST` en `local.properties` → `BuildConfig.API_HOST` |
| 4 | `runBlocking` leyendo DataStore en el hilo de red en **cada** petición | `AuthInterceptor.kt:10` | ⏳ Pendiente |
| 5 | Token en texto plano aunque `security-crypto` esté declarado | `app/build.gradle.kts:88` | ⏳ Pendiente |
| 6 | Sin refresh de token ni logout automático ante 401 | — | ✅ Corregido: `AuthInterceptor` borra el token ante 401 y `AppNavigation` lleva al login |
| 7 | La capa `domain` para pedidos es código muerto: la UI usa `PedidoDto` directo | `domain/model/DomainModels.kt:45-86` | ⏳ Pendiente |
| 8 | `MetaData.perPage` sin `@SerializedName("per_page")` → nunca se llena | `data/remote/dto/CommonDtos.kt:14` | ✅ Corregido |
| 9 | `getPedidos(perPage = 1000)` y KPIs calculados en el cliente | `ui/admin/AdminDashboardViewModel.kt:51` | ✅ Corregido: el backend limita `per_page` a **100**, ahora `fetchAllPages()` recorre todas las páginas |
| 10 | Dos caminos distintos hacia la API (Repository vs API directa) → lógica duplicada | ver sección 7 | ⏳ Pendiente (parcial: los mensajes de error de POS/Cliente ya pasan por `ApiErrors`) |
| 11 | Polling con `delay(8000)` en el ViewModel, sin cancelar al navegar | `BaristaPanelViewModel.kt:59-66` | ✅ Corregido: `Job` único con guard + cancelación en `onCleared()` |
| 12 | El cliente envía `total`, `precio_unitario` y `numero_factura`: el backend debería **recalcularlos** | `ui/pos/POSViewModel.kt:185-199` | ⏳ Pendiente (decisión de backend) |
| 13 | El Home del invitado muestra "Error al cargar productos (401)": el catálogo estaba sólo bajo `auth:sanctum` | Backend `routes/api.php:102-109` | ✅ Corregido: `GET categorias/productos` (index/show) ahora públicos con `throttle:60,1`; las escrituras siguen en el grupo admin |
| 14 | "Agregar barista" desde admin no funcionaba: la app envía `roles:[id]` pero el backend sólo aceptaba `rol_ids` (creaba el usuario SIN rol), y la política de contraseña del `StoreUserRequest` era más estricta que la del diálogo (422 genérico) | Backend `UsuarioController.php`, `StoreUserRequest.php`, `UpdateUserRequest.php`; app `AdminViewModel.saveUser` | ✅ Corregido (parte 1): backend acepta `rol_ids` **o** `roles`; contraseña de cuentas administrativas relajada a `min:8`; la app muestra el mensaje real del servidor |
| 15 | El diálogo "Agregar Barista" no listaba roles → checkout de "rol" invisible → botón deshabilitado. Causa raíz: `GET /roles` devuelve paginación `{data,meta}` pero la app la tipaba como `List<RolFullDto>` → lista vacía | App `UserApi.getRoles`, `AdminViewModel.loadRoles`; Backend `RolController::index` | ✅ Corregido (parte 2): `getRoles()` usa `PaginatedResponse<RolFullDto>`; entrada `roles.data`; rol "barista" preseleccionado por defecto; backend devuelve `BaseResource::collection` (formato consistente) |
| 16 | Turnos solo con un día (`fecha`); ahora se pueden definir con **fecha de inicio y fecha final** (rango de días). El turno es "activo" si HOY cae dentro del rango | Backend: migración `2026_10_06_000000_add_fecha_fin_to_turnos_table.php`, `Turno.php`, `TurnoService::getActiveTurno`, `StoreTurnoRequest/UpdateTurnoRequest`, `EquipoController::index`; App: `TurnoDtos.kt`, `TurnosViewModel.createTurno`, `TurnosScreen` (2 date pickers, rango en lista) | ✅ Implementado — requiere `php artisan migrate` |

### Bugs adicionales encontrados y corregidos

| Problema | Detalle |
|----------|---------|
| `codigoApi` llegaba siempre `null` | Retrofit amortigua el cuerpo del error en un buffer de Okio **de un solo uso**: `ApiErrors.parse()` consumía el cuerpo y `ApiErrors.code()` leía una cadena vacía. Por eso el flujo "correo sin verificar" (`EMAIL_NOT_VERIFIED` en `LoginViewModel`) nunca se activaba. `ApiErrors` ahora memoriza la lectura por respuesta. |
| Errores "Error al crear pedido: 500" en POS y Cliente | Ahora usan `ApiErrors.parse()`, así que el usuario ve *"La caja indicada no está abierta"* en lugar del código HTTP. |
| Backend sin `code` en errores de negocio | `BusinessRuleException` sólo devolvía `{message}`. Ahora acepta un `$codigo` opcional (`CAJA_CERRADA`, `CAJA_NO_EXISTE`, `CAJA_AJENA`, `PERMISO_DENEGADO`) y `Handler` lo incluye en la respuesta **sólo cuando está definido** (el formato anterior se mantiene intacto). |

---

## 11. Índice de archivos citados

| Capa | Archivo |
|------|---------|
| Base URL / constantes | `app/src/main/java/com/example/kaffacafeteria/util/Constants.kt` |
| DI + Retrofit + OkHttp | `app/src/main/java/com/example/kaffacafeteria/KaffaApp.kt` |
| Interceptor de token | `app/src/main/java/com/example/kaffacafeteria/data/remote/interceptor/AuthInterceptor.kt` |
| Log de red | `app/src/main/java/com/example/kaffacafeteria/data/remote/interceptor/DebugInterceptor.kt` |
| Interfaz API pedidos | `app/src/main/java/com/example/kaffacafeteria/data/remote/api/OrderApi.kt` |
| Interfaz API auth | `app/src/main/java/com/example/kaffacafeteria/data/remote/api/AuthApi.kt` |
| DTOs de pedidos | `app/src/main/java/com/example/kaffacafeteria/data/remote/dto/OrderDtos.kt` |
| DTOs paginación | `app/src/main/java/com/example/kaffacafeteria/data/remote/dto/CommonDtos.kt` |
| DTOs de auth | `app/src/main/java/com/example/kaffacafeteria/data/remote/dto/AuthDtos.kt` |
| Repository de pedidos | `app/src/main/java/com/example/kaffacafeteria/data/repository/OrderRepositoryImpl.kt` |
| Repository de auth | `app/src/main/java/com/example/kaffacafeteria/data/repository/AuthRepositoryImpl.kt` |
| Interfaz del dominio | `app/src/main/java/com/example/kaffacafeteria/domain/repository/OrderRepository.kt` |
| Tipo Resource | `app/src/main/java/com/example/kaffacafeteria/util/Resource.kt` |
| Parseo de errores | `app/src/main/java/com/example/kaffacafeteria/util/ApiErrors.kt` |
| Crear pedido (POS) | `app/src/main/java/com/example/kaffacafeteria/ui/pos/POSViewModel.kt` |
| Crear pedido (Cliente) | `app/src/main/java/com/example/kaffacafeteria/ui/cliente/ClientViewModel.kt` |
| Listar / cambiar estado | `app/src/main/java/com/example/kaffacafeteria/ui/orders/OrderViewModel.kt` |
| Panel barista + polling | `app/src/main/java/com/example/kaffacafeteria/ui/barista/BaristaPanelViewModel.kt` |
| Pantalla de pedidos | `app/src/main/java/com/example/kaffacafeteria/ui/orders/OrderListScreen.kt` |
| Detalle y transiciones | `app/src/main/java/com/example/kaffacafeteria/ui/orders/OrderDetailScreen.kt` |
| Modelos de dominio | `app/src/main/java/com/example/kaffacafeteria/domain/model/DomainModels.kt` |
| Manifest (cleartext) | `app/src/main/AndroidManifest.xml` |
