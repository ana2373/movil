# KaffaCafeteria

Aplicación móvil Android para la gestión de una cafetería (punto de venta, inventario, pedidos, caja y chat interno). Desarrollada íntegramente en **Kotlin** con **Jetpack Compose** (Material 3) y una arquitectura limpia en capas (data / domain / ui).

## Índice

- [Características](#características)
- [Stack Tecnológico](#stack-tecnológico)
- [Arquitectura](#arquitectura)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Capa de Datos](#capa-de-datos)
- [Gestión de Estado](#gestión-de-estado)
- [Navegación](#navegación)
- [Persistencia Local](#persistencia-local)
- [Roles y Flujo de Autenticación](#roles-y-flujo-de-autenticación)
- [Compilación](#compilación)
- [Testing](#testing)
- [Observaciones y Limitaciones](#observaciones-y-limitaciones)

---

## Características

- **Roles:** `cliente`, `barista` y `admin`, con rutas y vistas diferenciadas por rol.
- **Punto de venta (POS):** selección de productos y panel de carrito lateral, generación de factura y pagos (efectivo/transferencia).
- **Pedidos:** creación, listado con filtro por estado y transición de estados (pendiente → en_preparacion → entregado/pagado).
- **Barista:** tablero Kanban con actualización automática (polling) y transición de estados.
- **Caja:** apertura y cierre de caja con montos inicial/final y registro de movimientos.
- **Chat interno:** mensajería entre usuarios con sondeo periódico.
- **Panel administrativo:** KPIs del día, gráfica de ventas de 7 días, ranking de productos.
- **Gestión:** usuarios, categorías, productos, medios de pago, gastos, mermas, proveedores, compras e insumos.
- **Tema oscuro/claro** persistente vía DataStore.
- **Perfil:** edición de datos, carga de foto (multipart) y cambio de contraseña.

## Stack Tecnológico

| Capa | Tecnología | Versión |
|------|------------|---------|
| Lenguaje | Kotlin | 2.2.10 |
| UI | Jetpack Compose + Material 3 (BOM) | 2026.02.01 |
| Arquitectura | MVVM + Clean Architecture | — |
| Networking | Retrofit + OkHttp + Gson converter | 2.9.0 / 4.12.0 |
| Serialización | Gson (Retrofit converter) | — |
| Imágenes | Coil | 2.4.0 |
| Navegación | Navigation Compose | 2.7.6 |
| Persistencia local | Jetpack DataStore (Preferences) | 1.0.0 |
| Plugins | AGP | 9.2.1 |
| SDK | minSdk 24 · targetSdk 36 · compileSdk 36 (API 36.1) | — |

> **Nota:** `WorkManager` y `security-crypto` están declarados como dependencias, pero no se emplean actualmente en el código (el token se almacena en DataStore sin cifrado).

## Arquitectura

El proyecto sigue un patrón de **arquitectura limpia en capas** combinado con **MVVM** para la capa de presentación:

- **`domain/`** — modelos (`data classes`) e interfaces de repositorios.
- **`data/`** — implementación de repositorios, API Retrofit, DTOs, interceptores y persistencia local.
- **`ui/`** — pantallas Compose + ViewModels (MVVM).
- **`util/`** — constantes, `Resource<T>` (sealed class), extensiones y factoría de ViewModels.

La **inyección de dependencias es manual**: un `AppContainer` construido en `KaffaApp : Application` centraliza el `OkHttpClient`, Retrofit y los repositorios. Los ViewModels (type `AndroidViewModel`) acceden al contenedor mediante `application as KaffaApp`, y se instancian con el helper `createViewModel { VM(it) }`.

```
KaffaApp (Application)
   └── AppContainer
        ├── OkHttpClient (AuthInterceptor + DebugInterceptor)
        ├── Retrofit (BASE_URL + GsonConverter)
        ├── 8 API interfaces (Retrofit)
        └── 3 Repositorios
```

## Estructura del Proyecto

```
app/src/main/java/com/example/kaffacafeteria/
├── KaffaApp.kt                 # Application + AppContainer (DI manual)
├── MainActivity.kt             # Punto de entrada, tema y modo edge-to-edge
├── domain/
│   ├── model/DomainModels.kt   # User, Role, Product, Category, Order...
│   └── repository/             # Interfaces Auth/Catalog/OrderRepository
├── data/
│   ├── local/TokenManager.kt   # DataStore (token, dark_mode)
│   ├── remote/
│   │   ├── api/                # Auth, Catalog, Order, CashRegister, User,
│   │   │                       # Transaction, Dashboard, Mensaje
│   │   ├── dto/                # Auth, Common, User, Order, CashRegister,
│   │   │                       # Supplier, Report, Chat
│   │   └── interceptor/        # AuthInterceptor, DebugInterceptor
│   └── repository/             # Impl de repositorios (apiCall helper)
├── util/
│   ├── Constants.kt            # BASE_URL, claves de pref, PAGE_SIZE
│   ├── Resource.kt             # sealed class Loading/Success/Error
│   ├── Extensions.kt           # formatCurrency (l10n CO), toImageUrl...
│   └── ViewModelUtil.kt        # createViewModel factory
└── ui/
    ├── theme/                  # Color / Theme / Type (paleta café)
    ├── navigation/AppNavigation.kt
    ├── components/             # CommonComponents, NavigationComponents
    └── screens/
        ├── auth/               # Login, Register
        ├── splash/             # Splash + promos rotativas
        ├── home/               # Home (cliente/visitante)
        ├── profile/
        ├── productos/
        ├── pos/
        ├── orders/
        ├── chat/
        ├── cliente/
        ├── barista/            # Panel Kanban, Caja
        └── admin/              # Panel, Usuarios, Categorías, Productos,
                                # Medios de pago, Reportes, Gastos, Mermas,
                                # Proveedores, Compras, Insumos, Turnos
```

## Capa de Datos

### Configuración de Red

- **Base URL:** `http://192.168.80.15:8000/api/v1/` (LAN local).
- **Base de imágenes:** `http://192.168.80.15:8000/`.
- **Interceptores:**
  - `AuthInterceptor` — inyecta `Authorization: Bearer <token>` (desde DataStore) y `Accept: application/json` en todas las peticiones.
  - `DebugInterceptor` — logging OkHttp a nivel `BODY` (siempre activo).
- Timeouts de conexión/lectura: 30 s.

> `usesCleartextTraffic="true"` está habilitado en el manifiesto al servirse el backend por HTTP plano.

### Interfaces API (Retrofit)

| API | Endpoints principales |
|-----|-----------------------|
| `AuthApi` | `POST login`, `POST registro`, `GET me`, `PUT perfil`, `POST perfil/foto` (multipart), `POST logout` |
| `CatalogApi` | CRUD Categorías/Productos, GET Insumos, CRUD Medios de Pago (listado + paginado) |
| `OrderApi` | `pedidos` GET paginado/filtrable, GET por id, POST/PUT/DELETE |
| `CashRegisterApi` | `cajas` GET/POST, `cajas/{id}/cerrar`, `movimiento-cajas` GET/POST |
| `UserApi` | `usuarios` CRUD, `roles` GET |
| `TransactionApi` | `compras`, `gastos`, `mermas`, `proveedores` (GET/POST) |
| `DashboardApi` | `GET dashboard` |
| `MensajeApi` | `mensajes/contactos`, `mensajes` (hilo), `POST mensajes`, `mensajes/leer` |

### Repositorios

Los repositorios exponen una envoltura genérica (`apiCall` helper) que mapea resultados a `Resource<T>` (Loading/Success/Error). `AuthRepositoryImpl` gestiona además el token en DataStore y el parseo del cuerpo de error con Gson.

## Gestión de Estado

- Cada pantalla define un `XxxUiState` (data class) mantenido en `mutableStateOf`.
- Los flujos de red se modelan con `Resource<T>` (`Loading` / `Success` / `Error`).
- La lógica de negocio corre en `viewModelScope` (corrutinas).
- Los ViewModels emplean `AndroidViewModel` y acceden al `AppContainer` a través de `Application`.

**Sondeo (polling) en segundo plano** (no se usa WorkManager):
- `ChatViewModel` consulta `/mensajes` cada **5 s**.
- `BaristaPanelViewModel` consulta `/pedidos` cada **8 s**.
- Carrusel de `HomeScreen` avanza cada **3 s**.

> El sondeo se ejecuta dentro de los ViewModels: al pasar la app a segundo plano se detiene.

## Navegación

`AppNavigation.kt` define un `sealed class Screen` con las rutas y un único `NavHost` cuyo destino inicial es `splash`:

```
splash → client_home / barista_home / home  (según rol)
login | register | profile | chat | pos | orders | orders/{pedidoId}
productos | admin_panel | admin/users | admin/categories
admin/products | admin/payment_methods | admin/reports | caja
compras | mermas | admin/gastos | admin/insumos
admin/proveedores | admin/turnos
```

- **Enrutado por rol:** `admin` → `AdminPanel`, `barista` → `BaristaHome`, resto → `Home` (cliente).
- La barra inferior se muestra solo en rutas de cliente (5 ítems: Inicio, Pedidos, Carrito, Productos, Chat).
- La navegación por autenticación se controla mediante `requireAuth`.

## Persistencia Local

- **DataStore Preferences** en `kaffa_prefs` con las claves:
  - `auth_token` — token JWT/Bearer.
  - `dark_mode` — preferencia de tema.
  - `promo_shown_date` — (constante definida, sin uso activo).
- **Turnos** (`TurnosViewModel`) es gestionado **solo en memoria** (se pierde al reiniciar la app).

## Roles y Flujo de Autenticación

- Roles: `cliente`, `barista`, `admin` (con propiedades derivadas `isAdmin`/`isBarista`/`isCliente` en el modelo `User`).
- **Login/Registro:** se validan credenciales contra el backend y el token se guarda en DataStore.
- **Registro:** validación de fortaleza de contraseña (mín. 8 caracteres con mayúsculas, minúsculas, dígito y símbolo).
- **Sesión:** `GET /me` (verificación de sesión durante el splash y para reconstruir el usuario).

## Compilación

Requisitos: **JDK 11+** y **Android SDK 36**.

```bash
# Compilar APK de depuración
./gradlew assembleDebug

# Compilar APK de release
./gradlew assembleRelease

# Ejecutar tests unitarios
./gradlew test

# Ejecutar tests instrumentados
./gradlew connectedAndroidTest
```

El artefacto se genera en `app/build/outputs/apk/`.

> El APK de release se construye **sin optimización** (`optimization { enable = false }`): no se aplica R8/ProGuard, por lo que el binario no está reducido ni ofuscado.

## Testing

- `ExampleUnitTest.kt` — test unitario JUnit trivial de ejemplo.
- `ExampleInstrumentedTest.kt` — verifica el nombre del paquete.

Ambos son plantillas por defecto; **no contienen lógica de negocio testeada**.

## Observaciones y Limitaciones

1. **IP de red fija** (`192.168.80.15`) en la URL base: la app solo funciona en la red LAN contratada.
2. **Logging de red a nivel `BODY`** siempre activo (no hay separación debug/release).
3. **Sin R8/ProGuard** en release → binario sin optimizar ni ofuscar.
4. **Token en DataStore sin cifrar** a pesar de la dependencia `security-crypto`.
5. **CRUD parcial:** categorías, productos y medios de pago solo permiten crear/eliminar; sin edición.
6. **Pantallas placeholder:** `ReportsScreen` (sin funcionalidad) y `TurnosScreen` (solo en memoria).
7. **Sondeo dependiente de ViewModels:** sin sincronización en segundo plano real al minimizar la app.
8. **Lógica duplicada** de carrito entre `POS` y `Client`, y llamadas repetidas de `getMediosPago`/`getCategorias`/`getProductos` en varios ViewModels.
