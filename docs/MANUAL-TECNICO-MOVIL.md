# Manual técnico — Aplicación móvil Kaffa (Android)

Documento técnico de la aplicación móvil **KaffaCafeteria**, una app Android
nativa escrita en **Kotlin + Jetpack Compose** que consume la API REST del
backend Kaffa (Laravel). Describe la arquitectura, el diseño de capas, la
capa de red, la persistencia local, cada módulo funcional, los flujos
principales y las tareas de compilación e integración.

- **Paquete base:** `com.example.kaffacafeteria`
- **Nombre del proyecto Gradle:** `KaffaCafeteria`
- **Versión:** `1.0` (`versionCode 1`)
- **Fecha del documento:** 2026-10-19
- **Última actualización:** 2026-11-01 (despliegue contra el VPS y entrega en AAB)

---

## Tabla de contenido

1. [Introducción](#1-introducción)
2. [Normas y estándares aplicados](#2-normas-y-estándares-aplicados)
3. [Stack tecnológico](#3-stack-tecnológico)
4. [Arquitectura general](#4-arquitectura-general)
5. [Estructura del proyecto](#5-estructura-del-proyecto)
6. [Inyección de dependencias](#6-inyección-de-dependencias)
7. [Capa de dominio](#7-capa-de-dominio)
8. [Capa de datos](#8-capa-de-datos)
   - 8.1 [Configuración de red](#81-configuración-de-red)
   - 8.2 [Interceptores](#82-interceptores)
   - 8.3 [DTOs y paginación](#83-dtos-y-paginación)
   - 8.4 [Repositorios](#84-repositorios)
   - 8.5 [Manejo de errores de la API](#85-manejo-de-errores-de-la-api)
   - 8.6 [Persistencia local](#86-persistencia-local)
9. [Capa de presentación (UI)](#9-capa-de-presentación-ui)
10. [Navegación](#10-navegación)
11. [Módulo de autenticación](#11-módulo-de-autenticación)
12. [Módulo de cliente](#12-módulo-de-cliente)
13. [Módulo de ventas (POS)](#13-módulo-de-ventas-pos)
14. [Módulo de pedidos](#14-módulo-de-pedidos)
15. [Módulo de barista](#15-módulo-de-barista)
16. [Módulo de caja](#16-módulo-de-caja)
17. [Módulo de administración](#17-módulo-de-administración)
18. [Módulo de gestión (inventario y finanzas)](#18-módulo-de-gestión-inventario-y-finanzas)
19. [Módulo de reportes y PDF](#19-módulo-de-reportes-y-pdf)
20. [Módulo de eventos](#20-módulo-de-eventos)
21. [Módulo de promociones](#21-módulo-de-promociones)
22. [Módulo de chat](#22-módulo-de-chat)
23. [Asistente KAFFA (IA local)](#23-asistente-kaffa-ia-local)
24. [Tema, colores y modo oscuro](#24-tema-colores-y-modo-oscuro)
25. [Seguridad](#25-seguridad)
26. [Compilación y ejecución](#26-compilación-y-ejecución)
    - 26.1 [Requisitos](#261-requisitos)
    - 26.2 [Configuración del host y puerto del backend](#262-configuración-del-host-y-puerto-del-backend)
    - 26.3 [Credenciales de firma (release)](#263-credenciales-de-firma-release)
    - 26.4 [Comandos](#264-comandos)
    - 26.5 [Despliegue en el VPS y entrega del AAB](#265-despliegue-en-el-vps-y-entrega-del-aab)
27. [Pruebas](#27-pruebas)
28. [Solución de problemas](#28-solución-de-problemas)
29. [Limitaciones conocidas](#29-limitaciones-conocidas)
30. [Glosario](#30-glosario)

---

## 1. Introducción

Kaffa es la aplicación móvil de la cafetería Kaffa. Cubre tres roles de
usuario (**cliente**, **barista** y **administrador**) y ofrece, según el rol:

- Catálogo y carrito para clientes, con pago en efectivo o transferencia.
- Punto de venta (POS), tablero Kanban de pedidos y caja para el barista.
- Panel completo de administración: usuarios, catálogo, promociones, eventos,
  inventario, compras, gastos, reportes estadísticos y turnos.
- Chat interno entre usuarios y un asistente conversacional (IA local).

La app es **cliente puro**: toda la lógica de negocio y las reglas de
autorización viven en el backend; la app se limita a presentar datos, validar
formularios localmente y reaccionar a los códigos de error del servidor.

---

## 2. Normas y estándares aplicados

El proyecto se desarrolla siguiendo un conjunto de normas y estándares
reconocidos de ingeniería de software, calidad, seguridad y arquitectura.
Aunque la aplicación no está **certificada** formalmente contra ninguna de
ellas, el diseño y la implementación se alinean con las prácticas que definen.

### 2.1 Normas de ingeniería y ciclo de vida

| Norma | Título | Aplicación en el proyecto |
|-------|--------|---------------------------|
| **ISO/IEC/IEEE 12207** | Procesos del ciclo de vida del software | Se cubren los procesos de especificación de requisitos, diseño arquitectónico, construcción, integración, verificación (pruebas manuales y automatizadas) y entrega (APK debug/release en `entrega/`) |
| **ISO/IEC/IEEE 29148** | Ingeniería de requisitos | Requisitos derivados de los roles y módulos documentados en este manual y en la carpeta `docs/` |
| **IEEE 1016** | Descripción del diseño de software (SDD) | Este mismo documento actúa como descripción de diseño: capas, componentes, interfaces, modelos de datos y flujos |

### 2.2 Normas de calidad del producto

| Norma | Título | Aplicación en el proyecto |
|-------|--------|---------------------------|
| **ISO/IEC 25010** | Modelo de calidad del producto software | La app atiende las características de **adecuación funcional**, **eficiencia de desempeño**, **compatibilidad**, **usabilidad**, **fiabilidad**, **seguridad**, **mantenibilidad** y **portabilidad** (ver 2.4) |
| **ISO 9001 (principios)** | Gestión de la calidad | Trazabilidad de cambios mediante Git, revisión por módulos y documentación de decisiones |
| **ISO/IEC 25040** | Evaluación de la calidad | Criterios de prueba de la [sección 27](#27-pruebas) |

### 2.3 Normas de seguridad de la información

| Norma | Título | Aplicación en el proyecto |
|-------|--------|---------------------------|
| **ISO/IEC 27001 / 27002** | Gestión y controles de seguridad de la información | Autenticación por token Bearer, control de acceso por roles, secretos fuera del repositorio (`local.properties`), `FileProvider` para compartir archivos |
| **OWASP MASVS / MASTG** | Estándares de verificación y pruebas de seguridad móvil | Alineación con MASVS-STORAGE (almacenamiento del token), MASVS-NETWORK (uso de HTTPS recomendado) y MASVS-AUTH (recomendación de cifrado). Ver [sección 25](#25-seguridad) |
| **Ley 1581 de 2012 (Colombia)** | Protección de datos personales | Principio de mínima recolección: la app sólo maneja los datos necesarios (correo, token de sesión, imágenes enviadas por el usuario) |

### 2.4 Mapeo con las características de calidad ISO/IEC 25010

| Característica | Cómo se cumple |
|----------------|----------------|
| **Adecuación funcional** | Módulos por rol (cliente, barista, admin) cubren los casos de uso del negocio |
| **Eficiencia de desempeño** | Paginación mediante `fetchAllPages`, timeouts de 30 s, refresco controlado (polling configurable) |
| **Compatibilidad** | Android API 24–36, pantallas adaptables con Compose |
| **Usabilidad** | Material 3, estados de carga/error/vacío consistentes, modo oscuro, mensajes de error traducidos al usuario |
| **Fiabilidad** | `Resource<T>` unifica resultados; el cierre de sesión local se completa aunque falle la red; `ApiErrors` tolera los tres formatos de error del backend |
| **Seguridad** | Ver 2.3 y [sección 25](#25-seguridad) |
| **Mantenibilidad** | Arquitectura por capas, DI centralizada, nomenclatura consistente, código sin dependencias ocultas |
| **Portabilidad** | Configuración del host del backend vía `local.properties` sin recompilar código fuente |

### 2.5 Convenciones de código y estilo

| Convención                            | Detalle                                                                                                            |
| ------------------------------------- | ------------------------------------------------------------------------------------------------------------------ |
| **Estilo Kotlin oficial**             | Nomenclatura: `PascalCase` para clases, `camelCase` para funciones/propiedades, `UPPER_SNAKE_CASE` para constantes |
| **Arquitectura MVVM + Clean**         | Separación UI / dominio / datos `(ver [sección 4](#4-arquitectura-general))`                                       |
| **Versionado semántico**              | `versionName 1.0` + `versionCode 1` (formato `MAYOR.MENOR`)                                                        |
| **Git**                               | Ramas y commits descriptivos; archivos sensibles excluidos por `.gitignore`                                        |
| **Nomenclatura de paquetes por capa** | `ui/`, `domain/`, `data/`, `util/`                                                                                 |

### 2.6 Documentos normativos internos

Los siguientes documentos del repositorio complementan las normas anteriores:

- `docs/ENTREGA-APK.md` — proceso de entrega y compilación del APK (entrega inicial).
- `docs/CHANGELOG-2026-11-01-DESPLIEGUE-VPS.md` — despliegue contra el VPS y generación del AAB.
- `docs/CREDENCIALES-FIRMA.md` — gestión de claves de firma (fuera del control de versiones).
- `docs/Flujo_pedidos.md` — especificación funcional del flujo de pedidos.
- `docs/EVENTOS-ASISTENTE-IA.md` — especificación del módulo de eventos y asistente.
- `README.md` — descripción general y puesta en marcha.

> **Nota:** estas normas se aplican como buenas prácticas y criterios de
> diseño; para una eventual certificación formal se requeriría auditoría
> externa, evidencia de procesos y trazabilidad adicional.

---

## 3. Stack tecnológico

| Capa | Tecnología | Versión |
|------|------------|---------|
| Lenguaje | Kotlin | 2.2.x |
| UI | Jetpack Compose + Material 3 | Compose BOM 2026.02.01 |
| Arquitectura | MVVM + Clean Architecture (capas) | — |
| Networking | Retrofit | 2.9.0 |
| Serialización | Gson (converter-gson) | 2.9.0 |
| Cliente HTTP | OkHttp + logging-interceptor | 4.12.0 |
| Imágenes | Coil (coil-compose) | 2.4.0 |
| Navegación | Navigation Compose | 2.7.6 |
| Persistencia local | DataStore Preferences | 1.0.0 |
| Seguridad | androidx.security-crypto | — |
| Concurrencia | Kotlinx Coroutines | 1.7.3 |
| Ciclo de vida | lifecycle-viewmodel / runtime-ktx | 2.6.2 – 2.8.7 |
| WorkManager | androidx.work (workmanager) | — |
| SDK Android | minSdk 24 · targetSdk 36 · compileSdk 36 | — |
| JDK | Java 11 (source/target) | — |

### 3.1 Dependencias declaradas (`app/build.gradle.kts`)

- `retrofit`, `converter-gson`, `okhttp`, `okhttp-logging`
- `datastore-preferences`, `security-crypto`
- `androidx.compose.ui`, `material3`, `material-icons-extended`
- `lifecycle-viewmodel-compose`, `lifecycle-runtime-compose`
- `navigation-compose`
- `coil-compose`
- `workmanager`
- Testing: `junit`, `androidx.espresso.core`, `androidx.compose.ui.test.junit4`

> El archivo declara además algunas dependencias duplicadas con versiones
> distintas (p. ej. Compose `1.5.4` y `1.7.6`); Gradle resuelve tomando la
> versión mayor más alta del conjunto.

---

## 4. Arquitectura general

La app sigue **Clean Architecture por capas** combinada con el patrón **MVVM**:

```
        ┌────────────────────────────────────────────┐
        │                 UI (Compose)                │
        │  Screens + ViewModels (mini-MVVM por pantalla)
        └───────────────┬────────────────────────────┘
                        │ usa
        ┌───────────────▼────────────────────────────┐
        │            domain/ (contratos)              │
        │  Modelos de dominio + interfaces Repository │
        └───────────────┬────────────────────────────┘
                        │ implementa
        ┌───────────────▼────────────────────────────┐
        │             data/ (implementación)           │
        │  Repositorios · APIs Retrofit · DTOs         │
        │  Interceptores · DataStore · Almacenes       │
        └───────────────┬────────────────────────────┘
                        │ HTTP
        ┌───────────────▼────────────────────────────┐
        │         Backend Kaffa (Laravel REST)         │
        └────────────────────────────────────────────┘
```

### Principios aplicados

- **Separación de responsabilidades:** la UI no conoce Retrofit; los
  repositorios devuelven `Resource<T>` (estado + datos + error legible).
- **Unidireccionalidad de datos:** el estado vive en el ViewModel
  (`mutableStateOf`) y la pantalla lo observa; los eventos del usuario llaman
  a funciones del ViewModel.
- **Resultado tipado:** `Resource<T>` (`Loading`, `Success`, `Error`) evita
  `try/catch` dispersos y unifica el manejo de errores.
- **Errores traducidos:** `ApiErrors` convierte respuestas HTTP heterogéneas
  del backend en mensajes listos para mostrar.
- **Reacción al backend:** códigos de negocio (`EMAIL_NOT_VERIFIED`,
  `CAJA_CERRADA`, `FUERA_HORARIO`, `TURNO_INACTIVO`…) se propagan hasta la UI
  para bloquear acciones con un mensaje claro.

---

## 5. Estructura del proyecto

```
app/src/main/java/com/example/kaffacafeteria/
├── KaffaApp.kt                  # Application + AppContainer (DI manual)
├── MainActivity.kt              # Punto de entrada, tema, edge-to-edge, deep links
├── domain/
│   ├── model/DomainModels.kt    # User, Role, Product, Category, Order, Payment...
│   ├── model/Promocion.kt       # Promoción local (vigencia, productos, descuento)
│   └── repository/              # Interfaces AuthRepository, CatalogRepository, OrderRepository
├── data/
│   ├── local/
│   │   ├── TokenManager.kt      # DataStore: token, dark_mode, promo_shown_date
│   │   ├── CartStore.kt         # Carrito en memoria (Compose SnapshotStateList)
│   │   ├── PromoSesion.kt       # Promoción del momento (compartida por la app)
│   │   └── ReportArchive.kt     # Archivo físico + índice JSON de reportes PDF
│   ├── remote/
│   │   ├── api/                 # Interfaces Retrofit (12 APIs)
│   │   ├── dto/                 # DTOs de petición/respuesta
│   │   └── interceptor/         # AuthInterceptor, DebugInterceptor
│   └── repository/              # AuthRepositoryImpl, CatalogRepositoryImpl, OrderRepositoryImpl
├── util/
│   ├── Constants.kt             # BASE_URL, IMAGE_BASE_URL, claves, PAGE_SIZE
│   ├── Resource.kt              # sealed class Loading/Success/Error
│   ├── ApiErrors.kt             # Parseo uniforme de errores Laravel
│   ├── Network.kt               # fetchAllPages, emptyBody, ApiException
│   ├── Extensions.kt            # formatCurrency, toImageUrl, imagenLocal
│   ├── ViewModelUtil.kt         # createViewModel { VM(it) }
│   ├── PdfReportBuilder.kt      # Generación de PDF nativo
│   └── PdfFileActions.kt        # Abrir/compartir/descargar PDF
└── ui/
    ├── theme/                   # Color.kt, Theme.kt, Type.kt (paleta café)
    ├── navigation/AppNavigation.kt
    ├── components/              # CommonComponents, NavigationComponents, PromocionPopup
    ├── splash/                  # Splash con el logo de Kaffa
    ├── home/                    # Inicio cliente/visitante + eventos públicos
    ├── cliente/                 # Home alternativa con carrito propio
    ├── auth/                    # Login, Registro, Recuperar contraseña
    ├── productos/               # Catálogo público
    ├── pos/                     # Punto de venta
    ├── orders/                  # Lista y detalle de pedidos
    ├── chat/                    # Mensajería interna
    ├── profile/                 # Perfil y edición de datos
    ├── barista/                 # Panel Kanban, Caja, Turno
    ├── gestion/                 # Insumos, Mermas, Compras, Gastos, Proveedores, Turnos
    ├── asistente/               # Asistente flotante + ViewModel
    └── admin/                   # Panel, usuarios, catálogo, promociones, eventos, reportes
```

Recursos relevantes:

- `app/src/main/AndroidManifest.xml` — permisos, actividad, FileProvider, deep link.
- `app/src/main/res/` — mipmaps de logo, drawables de respaldo por categoría, temas.

---

## 6. Inyección de dependencias

La **DI es manual**. `AppContainer` (construido en `KaffaApp.onCreate()`) es el
único grafo de dependencias:

```
KaffaApp (Application)
   └── AppContainer
        ├── TokenManager(context)
        ├── CartStore()
        ├── OkHttpClient (AuthInterceptor + DebugInterceptor, timeouts 30 s)
        ├── Retrofit (BASE_URL + GsonConverter)
        ├── APIs: authApi, catalogApi, orderApi, cashRegisterApi, userApi,
        │         transactionApi, dashboardApi, turnoApi, mensajeApi,
        │         reporteApi, eventoApi, asistenteApi
        └── Repositorios: authRepository, catalogRepository, orderRepository
```

Los ViewModels extienden `AndroidViewModel`, obtienen el contenedor con
`(application as KaffaApp).container` y se instancian en las pantallas con el
helper `createViewModel { MiViewModel(it) }` (`ViewModelUtil.kt`).

**Motivos del enfoque manual:** proyecto pequeño, sin necesidad de KSP/Hilt,
arranque rápido, trazabilidad total de dependencias.

---

## 7. Capa de dominio

`domain/model/DomainModels.kt` define los modelos que usa la UI, desacoplados
de los DTOs del backend:

| Modelo | Campos principales | Notas |
|--------|-------------------|-------|
| `User` | `id`, `nombre`, `correo`, `activo`, `roles`, `foto` | Propiedades derivadas `isAdmin`, `isBarista`, `isCliente` |
| `Role` | `id`, `nombre` | `admin` / `barista` / `cliente` |
| `Product` | `id`, `nombre`, `descripcion`, `precioVenta`, `categoria`, `imagen`, `activo` | — |
| `Category` | `id`, `nombre`, `activo` | — |
| `MedioPago` | `id`, `nombre`, `esVirtual`, `activo` | `esVirtual=true` ⇒ requiere comprobante |
| `Order` | `id`, `clienteId`, `total`, `propina`, `estado`, `detalles`, `pagos`, `factura` | — |
| `OrderDetail` | `productoId`, `cantidad`, `precioUnitario`, `subtotal`, `producto` | — |
| `Payment` | `medioPagoId`, `monto`, `comprobanteUrl`, `medioPago` | — |
| `Invoice` | `numeroFactura`, `subtotal`, `impuestos`, `total` | — |

Las interfaces de repositorio (`domain/repository/`) definen el contrato que la
capa de datos implementa:

- `AuthRepository`: `login`, `register`, `updateProfile`, `subirFoto`, `getMe`,
  `logout`, `getToken`, `isLoggedIn`, `getTokenFlow`, `reenviarVerificacion`,
  `forgotPassword`, `resetPassword`.
- `CatalogRepository`: CRUD de categorías, productos y medios de pago.
- `OrderRepository`: `getPedidos`, `getPedido`, `createPedido`, `updatePedido`,
  `deletePedido`.

**Registro sin sesión.** El registro no devuelve token: la app espera que el
usuario verifique el correo. Por eso `register()` devuelve
`Resource<RegistroExitoso>` (mensaje + correo), no `User`.

---

## 8. Capa de datos

### 8.1 Configuración de red

`util/Constants.kt` resuelve el host y el puerto del backend así:

1. Si `local.properties` define `KAFFA_API_HOST`, se usa ese valor
   (compilado como `BuildConfig.API_HOST`).
2. Si `local.properties` define `KAFFA_API_PORT`, se usa ese valor
   (compilado como `BuildConfig.API_PORT`); si no, el puerto por defecto es
   `8000`.
3. Si no hay host configurado, se autodetecta emulador vs. dispositivo físico:
   - Emulador → `10.0.2.2` (alias del host de la máquina).
   - Físico → `127.0.0.1`.
4. Cuando el puerto es `80` se omite en la URL (no se genera `:80`).

Producción (VPS, configuración actual):

```
BASE_URL       = http://kaffasena.tech/api/v1/
IMAGE_BASE_URL = http://kaffasena.tech/
```

Desarrollo local (valores por defecto):

```
BASE_URL       = http://10.0.2.2:8000/api/v1/   # emulador
BASE_URL       = http://127.0.0.1:8000/api/v1/  # dispositivo físico
```

Otros valores: `PREF_NAME = "kaffa_prefs"`, `TOKEN_KEY = "auth_token"`,
`USER_DATA_KEY = "user_data"`, `PAGE_SIZE = 15`.

El `AndroidManifest.xml` declara `android:usesCleartextTraffic="true"` porque el
backend del VPS se publica por HTTP en el puerto 80 (sin TLS).

### 8.2 Interceptores

**`AuthInterceptor`**
- Lee el token de `TokenManager` (bloqueante con `runBlocking`, ya que OkHttp
  ejecuta interceptores en hilos de red).
- Si hay token añade `Authorization: Bearer <token>`; siempre añade
  `Accept: application/json`.
- Si la respuesta es **401 y había token**, borra el token y emite
  `TokenManager.sesionExpirada`; `AppNavigation` escucha ese flujo y devuelve
  al usuario al login. (Un 401 sin token —p. ej. login con credenciales
  incorrectas— no dispara esta lógica.)

**`DebugInterceptor`** (`HttpLoggingInterceptor`)
- Nivel `BODY` sólo en compilaciones **debug**; en release es `NONE` para no
  filtrar correos, tokens, contraseñas ni payloads de caja.
- Enmascara la cabecera `Authorization` con `redactHeader`.

Timeouts: conexión, lectura y escritura de **30 segundos**.

### 8.3 DTOs y paginación

Los DTOs viven en `data/remote/dto/` y usan `@SerializedName` para mapear el
snake_case de Laravel (`precio_venta`, `created_at`, `medio_pago_id`…).

Envoltura de listados (`CommonDtos.kt`):

```kotlin
PaginatedResponse<T>(data: List<T>, meta: MetaData)
MetaData(current_page, last_page, total, per_page)
```

> **Importante:** el backend limita `per_page` a 100
> (`HasBasicQueries::consultar`). Pedir 1000 sólo devuelve la primera página.
> Para agregados (KPIs, reportes) se usa `fetchAllPages { page -> api(..., page) }`
> (`util/Network.kt`), que recorre todas las páginas hasta `last_page`. La
> primera página es obligatoria: si falla, lanza `ApiException`.

API relevantes (`data/remote/api/`):

| API | Endpoints principales |
|-----|-----------------------|
| `AuthApi` | `POST login`, `POST registro`, `GET me`, `PUT perfil`, `POST perfil/foto`, `POST logout`, `POST email/verification-notification`, `POST forgot-password`, `POST reset-password` |
| `CatalogApi` | CRUD `categorias`, `productos`, `insumos`, `medios-pago`, recetas (`productos/{id}/insumos`) |
| `OrderApi` | `pedidos` GET/POST/PUT/DELETE |
| `CashRegisterApi` | `cajas` (abrir/cerrar), `movimiento-cajas` |
| `UserApi` | `usuarios` CRUD, `roles` |
| `TransactionApi` | `compras`, `gastos`, `mermas`, `proveedores` |
| `DashboardApi` | `GET dashboard` |
| `TurnoApi` | `turnos` CRUD, `GET turno-activo` |
| `MensajeApi` | `mensajes/contactos`, `mensajes` (hilo y envío), `mensajes/leer` |
| `ReporteApi` | `reportes` (novedades barista ↔ admin) |
| `EventoApi` | `GET eventos` (público), `GET eventos/admin`, POST/PUT/DELETE, `POST eventos/imagen` |
| `AsistenteApi` | `GET asistente/mensajes`, `POST asistente/chat`, `DELETE asistente/mensajes` |

### 8.4 Repositorios

`data/repository/` implementa las interfaces de dominio:

- **`AuthRepositoryImpl`** — gestiona login/registro/recuperación, guarda el
  token tras un login correcto y lo borra en `logout` (incluso si falla la red,
  el cierre de sesión local siempre se completa). Traduce `UsuarioDto → User`.
- **`CatalogRepositoryImpl`** y **`OrderRepositoryImpl`** — envuelven las
  llamadas en un helper `apiCall { ... }` que:
  1. Ejecuta la petición.
  2. Si es exitosa, devuelve `Resource.Success(body)`; si el cuerpo es vacío
     (DELETE/204), usa `emptyBody()` para no provocar `NPE`.
  3. Si falla, devuelve `Resource.Error(ApiErrors.parse(...), code, ApiErrors.code(...))`.
  4. Captura excepciones de red → `Resource.Error("Error de conexión")`.

### 8.5 Manejo de errores de la API

`util/ApiErrors.kt` unifica los tres formatos de error del backend:

- `{"message": "..."}` — mensaje simple (422 de login).
- `{"message": "...", "code": "CAJA_CERRADA"}` — error de reglas de negocio.
- `{"message": {...}, "errors": {...}}` — validación de formularios (422).

Detalles de implementación:

- Extrae el primer mensaje útil (`errors`, `error` o `message`).
- Devuelve el **código de negocio** (`code()`) para reaccionar a casos concretos.
- Memoriza el resultado por respuesta en un `WeakHashMap` sincronizado, porque
  el `errorBody` de OkHttp **sólo puede leerse una vez** (Retrofit lo almacena
  en un buffer que se vacía tras la primera lectura).

Códigos de negocio usados por la UI: `EMAIL_NOT_VERIFIED` (login),
`CAJA_CERRADA`, `FUERA_HORARIO`, `TURNO_INACTIVO`, `TURNO_EXPIRADO`.

### 8.6 Persistencia local

**`TokenManager`** — DataStore `kaffa_prefs`:

| Clave | Tipo | Uso |
|-------|------|-----|
| `auth_token` | String | Token Bearer de la sesión |
| `dark_mode` | Boolean | Preferencia de tema oscuro |
| `promo_shown_date` | String | Fecha de la promo mostrada |

Expone `tokenFlow` (Flow), `sesionExpirada` (SharedFlow), y operaciones
`getToken/saveToken/deleteToken/isDarkMode/setDarkMode`.

**`CartStore`** — carrito en memoria basado en `mutableStateListOf`: añadir,
actualizar cantidad, eliminar y limpiar; calcula `total` y `count`. Es
compartido entre el catálogo (donde se añade) y el POS (donde se cobra). No se
persiste entre arranques. Además guarda la **promoción aplicada**
(`aplicarPromo` / `quitarPromo`): cada línea puede llevar un
`descuentoPorcentaje`, del que salen `precioUnitario`, `subtotal` y `ahorro`.

**`PromoSesion`** — mantiene la promoción del momento elegida al abrir la app
para que el Inicio y el Carrito muestren la misma oferta.

**`ReportArchive`** — registro persistente de reportes PDF:

- Guarda los PDF en `Documents/Kaffa/reportes` (directorio externo de la app);
  si no está disponible cae a `filesDir/reportes`.
- Índice `reportes.json` con metadatos (id, título, tipo, periodo, archivo,
  autor, fecha, tamaño) para listar y borrar sin recorrer el disco.
- `publicarEnDescargas()` copia a Descargas vía MediaStore (Android 10+ sin
  permisos) o al directorio público en versiones anteriores.

---

## 9. Capa de presentación (UI)

Cada pantalla sigue un patrón consistente:

```
Screen (Composable)  ⇄  ViewModel (AndroidViewModel)
   - observa uiState        - mutableStateOf<UiState>
   - emite eventos          - llama repositorios/APIs en viewModelScope
   - dibuja estados         - actualiza el estado
     Loading/Error/Empty
```

- Estado inmutable por pantalla (`XxxUiState`) y `mutableStateOf` (`var uiState`).
- Estado de UI auxiliar (diálogos, texto de campos) con `remember`.
- Recolección de efectos con `LaunchedEffect` (cargar datos, reaccionar a éxito/error).
- Componentes reutilizables en `ui/components/` (`LoadingIndicator`,
  `ErrorMessage`, `EmptyState`, `KaffaBottomBar`, `NavItem`).

---

## 10. Navegación

`ui/navigation/AppNavigation.kt` define un `sealed class Screen` con todas las
rutas y un único `NavHost` cuyo destino inicial es `splash`.

**Enrutado por rol** (`homeRouteFor`):

| Rol | Destino |
|-----|---------|
| `admin` | `admin_panel` |
| `barista` | `barista_home` |
| resto / visitante | `home` |

**Barra inferior** (sólo rutas de cliente): Inicio, Pedidos, Carrito (POS),
Productos, Chat. `Orders`, `POS` y `Chat` exigen sesión (`requireAuth`).

**Deep link de recuperación de contraseña:**
`kaffa://reset-password?token=...&correo=...`

- `MainActivity` parsea el Intent de arranque y `onNewIntent` (app ya abierta)
  mediante `DeepLinks.parsear`.
- `AppNavigation` precarga el token en `ForgotPasswordViewModel` y navega a la
  pantalla de nueva contraseña, limpiando el splash.

**Expiración de sesión:** `AppNavigation` escucha `tokenManager.sesionExpirada`
y, si hay sesión activa, limpia el estado y navega al login con `popUpTo(0)`.

**Asistente flotante:** visible para cualquier usuario autenticado, por encima
de todas las pantallas. `sessionKey` se incrementa en cada login para que el
asistente descarte el historial del usuario anterior.

---

## 11. Módulo de autenticación

Archivos: `ui/auth/{Login,Register,ForgotPassword}{Screen,ViewModel}.kt`.

### 10.1 Login

- Valida correo/contraseña no vacíos.
- En éxito guarda sesión y navega según el rol.
- Si el backend responde `EMAIL_NOT_VERIFIED`, muestra un botón para
  **reenviar el correo de verificación** en lugar de dejar al usuario atascado.
- `init` comprueba si ya hay token y llama a `me()` para restaurar la sesión.

### 10.2 Registro

- Valida contra la misma política del backend: **mínimo 8 caracteres, con
  mayúscula, minúscula, número y símbolo**, correo con formato válido y
  confirmación coincidente.
- En éxito **no crea sesión**: pasa a la pantalla `RegistrationPendingScreen`
  ("revisa tu correo") con opción de reenviar el enlace o usar otro correo.

### 10.3 Recuperación de contraseña

Flujo en dos pasos:

1. **Solicitar enlace:** el usuario introduce su correo. El backend responde
   siempre igual (exista o no la cuenta), así que el mensaje de éxito es neutro.
2. **Nueva contraseña:** al abrir el deep link `kaffa://reset-password`, el
   token y el correo llegan precargados; sólo se pide la contraseña nueva
   (con las mismas reglas de complejidad). Incluye atajos para "volver a
   solicitar el enlace" si expiró.

---

## 12. Módulo de cliente

Existen dos pantallas de cliente:

- **`HomeScreen`** (`ui/home/`): inicio principal con barra inferior. Incluye
  encabezado con saludo/rol, accesos rápidos para staff (`Vender`, `Pedidos`,
  `Caja`, `Compras`, `Gastos`, `Mermas`, `Panel`), buscador, filtros por
  categoría, **Destacados** (carrusel rotativo automático), **Próximos eventos**
  y **Nuestro menú** en cuadrícula. Los invitados son redirigidos al login al
  comprar o abrir Pedidos/Carrito/Chat.
- **`ClientScreen`** (`ui/cliente/`): inicio de cliente con carrito propio en
  el estado del ViewModel (`ClientViewModel`) y cobro con selección de método
  de pago.

### Flujo de compra (cliente)

1. Se elige un producto (diálogo de detalle) o se pulsa **Comprar**.
2. Si no hay sesión → login.
3. El producto entra al carrito.
4. En el carrito se ajustan cantidades (+/−) o se eliminan ítems.
5. **Proceder al pago** abre el diálogo de métodos de pago:
   - **Efectivo:** pago al recoger.
   - **Transferencia (virtual):** exige **adjuntar comprobante** (imagen de la
     galería vía `PickVisualMedia`).
6. **Confirmar pedido** crea el pedido (`POST /pedidos`) con estado
   `pendiente`, detalle, pago y factura (`KAF-<timestamp>`).

La sección **Próximos eventos** consume `GET /eventos` (público, sólo
publicados) mediante `EventosPublicosViewModel`.

---

## 13. Módulo de ventas (POS)

Archivos: `ui/pos/{POSScreen,POSViewModel}.kt`. Dirigido a **barista/admin**.

- Cuadrícula de productos con filtro por categoría y buscador.
- Toca un producto para añadirlo al **carrito compartido** (`CartStore`).
- Carrito lateral con cantidades y total en vivo.
- **Cobrar** abre el diálogo de pago:
  - Radio de métodos de pago (virtuales marcados con "(virtual)").
  - Si el método es virtual, exige comprobante (imagen o URL).
- Al confirmar, crea el pedido (`POS-<timestamp>`) y limpia el carrito.
- Si no hay métodos configurados, se avisa que el admin debe crearlos.

---

## 14. Módulo de pedidos

Archivos: `ui/orders/`.

### `OrderListScreen`
- Filtros por estado: Todas, Pendiente, Pagado, En preparación, Entregado.
- Tarjetas con id, cliente, total, estado (`StatusBadge`) y fecha.

### `OrderDetailScreen`
- Datos del pedido (estado, cliente, fecha, total).
- Lista de productos y de pagos (incluye comprobante si existe).
- **Transiciones de estado** mediante botones según el estado actual:

  | Estado actual | Siguiente |
  |---------------|-----------|
  | `pendiente` | `pagado` |
  | `pagado` | `en_preparacion` |
  | `en_preparacion` | `entregado` |

`OrderViewModel` expone `loadPedidos`, `loadPedidoDetail`, `updateEstado`,
`assignBarista` (asignar barista a un pedido).

---

## 15. Módulo de barista

Archivos: `ui/barista/`.

### 14.1 Panel (`BaristaPanelScreen` / `BaristaPanelViewModel`)

- **Tablero Kanban** en columnas horizontales:
  - **Por atender** (`pendiente`, `pagado`)
  - **En preparación** (`en_preparacion`)
  - **Terminados** (`entregado`, `cancelado`)
- Cada tarjeta avanza con los botones **Preparando** y **Entregado**.
- **Auto-refresco** cada 8 s (`startAutoRefresh`). Un guard evita lanzar
  múltiples bucles si se vuelve a entrar a la pantalla.
- **Módulos:** Inventario, Reportar inventario, Chat, Caja de cobro.
- **Reportar inventario** crea una merma (`POST /mermas`) que el admin ve en
  *Mermas*; si el backend falla, deja constancia local sin romper la app.

### 14.2 Turno del barista (`TurnoBaristaViewModel`)

`GET /turno-activo` es la fuente de verdad del bloqueo operativo:

| Campo | Significado |
|-------|-------------|
| `turno_activo` | Tiene turno asignado y vigente |
| `caja_abierta` | Hay una caja abierta en ese turno |
| `puede_operar` | Puede registrar ventas/mermas/gastos/compras |
| `motivo_bloqueo` | Mensaje listo (`CAJA_CERRADA`, `FUERA_HORARIO`, …) |

Reglas: sin turno → no opera; con turno pero sin caja → debe **abrir caja**
primero (esta acción no se bloquea); con turno y caja → puede operar. El estado
se refresca cada 15 s (`iniciarVigilancia`). Para cambiar el monto se abren y
cierran cajas desde la propia pantalla.

---

## 16. Módulo de caja

Archivos: `ui/barista/{CashRegisterScreen,CashRegisterViewModel}.kt`.

- Muestra la **caja actual** (si está abierta) con: quién abrió, fechas,
  saldo inicial, ingresos, egresos y saldo actual.
- **Abrir caja:** pide el saldo inicial (monto físico) → `POST /cajas`.
- **Cerrar caja:** pide el saldo final (monto físico) → `PUT /cajas/{id}`.
- **Registro de cajas** (historial) con estado Abierta/Cerrada.
- Los movimientos de la caja actual se listan con su tipo (ingreso/egreso).
- Los errores del backend se muestran con su código y mensaje (`errorFrom`).

`CajaDto` calcula en el cliente `totalIngresos`, `totalEgresos` y `saldoActual`
a partir de los movimientos cuando el backend no envía los cierres.

---

## 17. Módulo de administración

Archivos: `ui/admin/`. El **Panel de Administración** (`AdminPanelScreen`)
muestra métricas y tarjetas de acceso a los módulos.

### 16.1 Dashboard (`AdminDashboardViewModel`)
- KPIs: ingresos de hoy, pedidos de hoy, clientes registrados, ticket promedio.
- Gráfica de **balance de los últimos 7 días**.
- Ranking de **productos más vendidos** (top 5).
- Usa `fetchAllPages` para no calcular sobre una sola página.

### 16.2 Usuarios (`UsersScreen` / `AdminViewModel`)
- Lista de usuarios con roles; crear, editar y eliminar.
- `getRoles()` para asignar roles por checkbox.
- Tarjeta **Agregar Barista** preselecciona el rol `barista`.

### 16.3 Categorías y Productos
- CRUD de categorías (`createCategoria`, `deleteCategoria`).
- CRUD de productos (nombre, descripción, precio, categoría, imagen).
- **Recetas:** asociar insumos a un producto (`addInsumoToProducto`,
  `removeReceta`).

### 16.4 Métodos de pago (`PaymentMethodsScreen`)
- CRUD de medios de pago con marca `esVirtual`.
- Validación local contra nombres duplicados antes de enviar.

### 16.5 Reportes estadísticos (`ReportsScreen`, `ReportDetailScreen`,
`ArchivedReportsScreen`)
- Tipos: **diario**, **semanal**, **mensual** e **inventario**.
- Ver [sección 19](#19-módulo-de-reportes-y-pdf).

### 16.6 Promociones y Eventos
- Ver [sección 21](#21-módulo-de-promociones) y [sección 20](#20-módulo-de-eventos).

---

## 18. Módulo de gestión (inventario y finanzas)

Archivos: `ui/gestion/`.

| Pantalla            | Función                                                                                     |
| ------------------- | ------------------------------------------------------------------------------------------- |
| `InsumosScreen`     | Inventario de materias primas (stock actual y mínimo, unidad). `isAdmin` controla acciones. |
| `MermasScreen`      | Registro de pérdidas/consumos (insumo, cantidad, motivo).                                   |
| `ComprasScreen`     | Compras a proveedores (factura, fecha, total, detalle, pagos).                              |
| `GastosScreen`      | Gastos generales con categoría y pago asociado.                                             |
| `ProveedoresScreen` | Base de datos de proveedores (contacto, NIT, convenio).                                     |
| `TurnosScreen`      | Asignación de turnos a baristas (fecha, tipo, baristas).                                    |

`GestionViewModel` centraliza gastos y mermas. Notas de diseño:

- Al crear un **gasto**, se asocia automáticamente un medio de pago de efectivo
  (no virtual) si existe, ya que el backend guarda el monto vía `gasto-pagos`.
- Si el backend rechaza una **merma**, se añade localmente (id negativo) para
  no romper la experiencia, dejándola visible en el listado.

---

## 19. Módulo de reportes y PDF

Archivos: `ui/admin/ReportDetailViewModel.kt`, `util/PdfReportBuilder.kt`,
`util/PdfFileActions.kt`, `data/local/ReportArchive.kt`.

### 18.1 Cálculo de datos
- `fetchAllPages` recorre pedidos e insumos de todas las páginas.
- **Diario:** pedidos/pagos de hoy.
- **Semanal:** últimos 7 días.
- **Mensual:** mes en curso (por año y mes).
- **Inventario:** lista de insumos con `stock actual`, `stock mínimo` y estado
  (`BAJO STOCK` / `Normal`).

### 18.2 KPIs y secciones
- Reportes de venta: total ventas, total pedidos, ticket promedio; tablas de
  ventas por día y productos más vendidos.
- Inventario: total de insumos y cuántos están bajo stock; tabla de estado.

### 18.3 Generación y archivo
- `PdfReportBuilder.generar()` dibuja el PDF de forma nativa (sin librerías
  externas) fuera del hilo principal (`Dispatchers.IO`).
- El PDF se **archiva** (`ReportArchive.registrar`) para poder reabrirlo,
  compartirlo (`FileProvider`) o descargarlo (MediaStore) cuantas veces se
  quiera.
- `reabrirUltimo()` reutiliza el último PDF archivado sin recalcular.
- Sólo un administrador puede eliminar archivos (verificado en el ViewModel).

---

## 20. Módulo de eventos

Archivos: `ui/admin/EventosScreen.kt`, `EventosViewModel.kt`,
`ui/home/EventosPublicosViewModel.kt`.

- **Administración:** `GET /eventos/admin` lista todos; crear/editar/eliminar
  con `POST/PUT/DELETE /eventos`. Imagen de portada vía
  `POST /eventos/imagen` (multipart; la Uri se copia a un temporal antes de
  subir). El interruptor **Publicado/Borrador** cambia `publicado` sin abrir el
  formulario (enviando `imagenes = null` para conservar la galería).
- **Público:** `GET /eventos` devuelve sólo los publicados. La app los muestra
  en **Próximos eventos** del inicio como carrusel horizontal; tocar una tarjeta
  abre un diálogo con imagen, fecha/hora, lugar y descripción completa.
- El **próximo evento** (el de fecha más cercana, `List<EventoDto>.proximoEvento()`)
  aparece además en la pantalla emergente de promociones (`PromocionPopup`).

`EventoDto.portada` devuelve la imagen con menor `orden` (o la primera).

---

## 21. Módulo de promociones

Archivos: `domain/model/Promocion.kt`, `ui/admin/PromocionesScreen.kt`,
`PromocionesViewModel.kt`, `ui/components/PromocionPopup.kt`,
`data/local/{CartStore,PromoSesion}.kt`.

Las promociones son un **CRUD local** (DataStore `promociones_store`); el
backend no expone un endpoint de promociones.

Modelo `Promocion` (`domain/model/Promocion.kt`):

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `id` | `Int` | Correlativo generado al crear |
| `nombre` | `String` | Título |
| `descripcion` | `String` | Mensaje |
| `tipo` | `String` | Descuento / 2x1 / Combo / Personalizado |
| `colorIndex` | `Int` | Índice de la paleta |
| `imagenUri` | `String?` | Imagen de la galería |
| `expiracionMillis` | `Long?` | Vencimiento (día + hora); `null` = sin caducidad |
| `activa` | `Boolean` | Activada por el admin |
| `productoIds` | `List<Int>` | Productos que incluye la promoción (los elige el admin) |
| `descuentoPorcentaje` | `Int` | Descuento aplicado a esos productos al cobrar |

### Splash de arranque

- `SplashScreen` sólo muestra el **logo de Kaffa** (`@mipmap/kaffa_logo_3`).
- Espera la consulta de sesión y se retira solo a los 2 s (el botón "Entrar"
  permite saltar la espera).
- Las promociones **ya no se muestran en el splash**.

### Pantalla emergente (popup)

- `PromocionPopup` (`ui/components/`) es un diálogo compacto con la
  **promoción del momento** (imagen, tipo, nombre, descripción, productos,
  precio y botón de compra) y el **próximo evento** debajo.
- Aparece automáticamente **al abrir la app / entrar al Inicio** en cuanto se
  carga la promo destacada; si además hay un evento próximo, se muestra debajo.
  En el **Carrito (POS)** el popup se reserva para el próximo evento.
- Se cierra con la X y se puede volver a abrir con el icono de eventos
  (`Icons.Default.Event`) de la cabecera del Inicio.
- El popup de promoción **no depende de descartes previos**
  (`dismissedPromoIds`): vuelve a mostrarse en cada arranque de la app. Al
  cerrar, el evento sí se marca como descartado y queda en el carrusel.
- `PromoSesion` (en `AppContainer`) guarda la promoción elegida para que
  Inicio y Carrito muestren **la misma oferta** sin rotar dos veces.
- Rotación: `siguientePromocion(activas)` filtra las activas no expiradas, lee
  de DataStore el id de la última mostrada (`ultima_promo_mostrada`) y elige la
  **siguiente** con vuelta al inicio.

### Compra de la promoción en el carrito

- El botón "Comprar promo y pagar en el carrito" llama a
  `CartStore.aplicarPromo(promo, productos)`: los productos de la promoción
  entran al carrito con el **precio de la promoción** y la pantalla POS muestra
  una insignia con el ahorro y un botón "Quitar".
- `productosDePromo(promo, catalogo)` resuelve el contenido: los productos
  elegidos por el admin o, si no hay, los **destacados** del catálogo (café,
  pan, postres).
- El descuento se calcula **en el dispositivo**: `POSViewModel.createOrder`
  envía `precio_unitario` con el descuento ya aplicado, de modo que el total
  del pedido cuadra con el detalle **sin tocar el backend**.

> Las promociones se guardan en cada dispositivo; no se sincronizan. El id
> mostrado se guarda en DataStore, por lo que la rotación sobrevive al cierre.

---

## 22. Módulo de chat

Archivos: `ui/chat/{ChatScreen,ChatViewModel}.kt`.

- **Lista de contactos** (`GET /mensajes/contactos`).
- **Hilo** (`GET /mensajes?con=<id>`) con burbujas propias/ajenas.
- **Envío** (`POST /mensajes`).
- **Marcar leídos** (`POST /mensajes/leer`) al abrir un contacto.
- **Polling** cada 5 s: recarga el hilo activo y la lista de contactos, para
  simular actualización en tiempo real.

---

## 23. Asistente KAFFA (IA local)

Archivos: `ui/asistente/{AsistenteFlotante,AsistenteViewModel}.kt`.

- Botón flotante circular visible para **todos los usuarios autenticados**.
- Al pulsarlo abre un diálogo de chat con cabecera, historial, sugerencias y
  entrada de texto.
- **Historial** (`GET /asistente/mensajes`) y **respuestas**
  (`POST /asistente/chat`).
- **Sugerencias por rol:** admin (ventas de la semana, predicción, insumos por
  reponer), barista (ventas de hoy, pedidos pendientes, insumos) y cliente
  (productos, cómo pedir, eventos).
- `sincronizarSesion(sessionKey)` descarta el historial al cambiar de usuario
  para no mezclar conversaciones entre roles.
- `limpiarMarkdown` elimina `**` que el backend usa para negrita.
- `limpiarConversacion()` vacía el historial (`DELETE /asistente/mensajes`).

---

## 24. Tema, colores y modo oscuro

Archivos: `ui/theme/{Color,Theme,Type}.kt`.

- Paleta basada en tonos café/verde: `PrimaryGreen`, `VerdeClaro`,
  `CoffeeBrown`, `CafeOscuro`, `CafeClaro`, `Terracotta`, `Negro`, `Blanco`,
  `SecondaryGreen`, `LightBrown`, `NearBlack`.
- `KaffaTheme(darkTheme)` selecciona el esquema claro u oscuro.
- El modo oscuro se guarda en DataStore (`dark_mode`) y se alterna desde el
  perfil (`onToggleTheme`). `MainActivity` ajusta los iconos de la barra de
  sistema según el tema.
- La UI maneja `isDarkModeActive()` para adaptar colores de acento aclarados
  (por ejemplo, `VerdeClaro` para acentos oscuros en modo noche).
- Imágenes de respaldo por categoría (`imagenLocal()` en `Extensions.kt`)
  cuando un producto no tiene imagen o la URL falla.

---

## 25. Seguridad

Controles aplicados en línea con **ISO/IEC 27001/27002** y **OWASP MASVS**
(referencia completa en la [sección 2.3](#23-normas-de-seguridad-de-la-información)):

- **Token Bearer** inyectado por `AuthInterceptor`; borrado automático y
  redirección al login ante un 401 con token.
- **Logging deshabilitado en release** (`DebugInterceptor`).
- **Sin secretos en el repositorio:** las credenciales de firma y el host se
  leen de `local.properties` (ignorado por git) y se inyectan como
  `BuildConfig`.
- **Deep link** `kaffa://reset-password` declarado en la actividad con
  `autoVerify="false"`.
- **FileProvider** (`${applicationId}.fileprovider`) para compartir PDFs de
  forma controlada.

**Puntos a mejorar (recomendaciones):**

1. Cifrar el token con `security-crypto` (la dependencia está incluida pero el
   token se guarda en DataStore sin cifrar).
2. Endurecer el deep link verificando origen/expiración del token.
3. **Habilitar HTTPS en producción:** actualmente el backend del VPS
   (`kaffasena.tech`) responde por HTTP en el puerto 80 y la app usa
   `usesCleartextTraffic="true"`. Publicar el backend con TLS (Nginx/Certbot) y
   cambiar `BASE_URL`/`IMAGE_BASE_URL` a `https`.
4. Configurar **Play App Signing** si el AAB se publica en Google Play, para no
   depender únicamente del keystore local.

---

## 26. Compilación y ejecución

### 26.1 Requisitos
- JDK 11+ (el build del despliegue se hizo con el JBR en `/app/extra/jbr`,
  OpenJDK 25).
- Android SDK 36 (con `compileSdk 36`).
- Backend Kaffa accesible desde el dispositivo. En producción:
  `http://kaffasena.tech/api/v1/`.

### 26.2 Configuración del host y puerto del backend

En `local.properties` (raíz del proyecto, **no versionado**):

```properties
# Producción (VPS)
KAFFA_API_HOST=kaffasena.tech
KAFFA_API_PORT=80
```

```properties
# Desarrollo local
KAFFA_API_HOST=192.168.80.15
KAFFA_API_PORT=8000
```

| Situación                                     | Host resultante   | URL base                                     |
| --------------------------------------------- | ----------------- | -------------------------------------------- |
| `KAFFA_API_HOST` + `KAFFA_API_PORT` definidos | El valor indicado | `http://<host>:<puerto>/api/v1/` (sin `:80`) |
| Sólo `KAFFA_API_HOST`                         | El valor indicado | `http://<host>:8000/api/v1/`                 |
| Nada definido, emulador                       | `10.0.2.2`        | `http://10.0.2.2:8000/api/v1/`               |
| Nada definido, físico                         | `127.0.0.1`       | `http://127.0.0.1:8000/api/v1/`              |

Los valores llegan al código como `BuildConfig.API_HOST` y
`BuildConfig.API_PORT`, y los consume `util/Constants.kt`. **Tras cambiarlos hay
que recompilar** el APK/AAB.

> El host y el puerto se compilan dentro del paquete. No se pueden cambiar desde
> la app en tiempo de ejecución.

### 26.3 Credenciales de firma (release)

```properties
RELEASE_STORE_FILE=kaffa-release.jks
RELEASE_STORE_PASSWORD=...
RELEASE_KEY_ALIAS=...
RELEASE_KEY_PASSWORD=...
```

Si el keystore no existe, el build de release sale **sin firmar** (válido para
CI, inválido para entregar). Detalle en `docs/CREDENCIALES-FIRMA.md`.

### 26.4 Comandos

```bash
export JAVA_HOME=/app/extra/jbr
export ANDROID_HOME=/home/ana/Android/Sdk
export ANDROID_SDK_ROOT=/home/ana/Android/Sdk
export PATH="$JAVA_HOME/bin:$PATH"

./gradlew assembleDebug         # APK de depuración
./gradlew assembleRelease       # APK de release (firmado si hay keystore)
./gradlew bundleRelease         # AAB firmado para Play Store / entrega
./gradlew test                  # Tests unitarios
./gradlew connectedAndroidTest  # Tests instrumentados
```

Artefactos:

| Comando | Salida |
|---------|--------|
| `assembleDebug` | `app/build/outputs/apk/debug/app-debug.apk` |
| `assembleRelease` | `app/build/outputs/apk/release/app-release.apk` |
| `bundleRelease` | `app/build/outputs/bundle/release/app-release.aab` |

- `optimization { enable = false }` en release (sin minificado/R8).

### 26.5 Despliegue en el VPS y entrega del AAB

El backend y el frontend están desplegados en el VPS y se consumen por
`http://kaffasena.tech` (HTTP, puerto 80, ruta `/api/v1`). La app se recompiló
con ese host y se entregó como **Android App Bundle**:

| Artefacto en `entrega/` | Uso |
|-------------------------|-----|
| `KaffaCafeteria-v1.0-release.aab` | **Entrega** / subida a Play Store |
| `KaffaCafeteria-v1.0-vps.apk` | Instalación y pruebas directas en teléfono |

> **El `.aab` no se instala directamente en un teléfono**: es el formato de
> publicación. Para probarlo en un dispositivo se usa el APK o se convierte con
> `bundletool`.

Verificaciones del despliegue:

```bash
# 1. El backend responde desde Internet
curl -i http://kaffasena.tech/api/v1/categorias     # 200 + JSON

# 2. El host quedó compilado dentro del paquete
grep API_HOST app/build/generated/source/buildConfig/release/\
com/example/kaffacafeteria/BuildConfig.java          # "kaffasena.tech"

# 3. Firma del AAB
jarsigner -verify app/build/outputs/bundle/release/app-release.aab

# 4. Firma del APK
$ANDROID_HOME/build-tools/36.0.0/apksigner verify --print-certs \
  entrega/KaffaCafeteria-v1.0-vps.apk
```

Notas de entrega:

- `versionCode 1` / `versionName 1.0`: incrementar antes de publicar una
  actualización en Play Store.
- La firma local (`kaffa-release.jks`) debe conservarse; sin ella no se pueden
  publicar actualizaciones con la misma identidad.
- El AAB está firmado con la clave local (upload key). Para Play se recomienda
  enrolar **Play App Signing**.
- El detalle cronológico del despliegue está en
  `docs/CHANGELOG-2026-11-01-DESPLIEGUE-VPS.md`.

---

## 27. Pruebas

- **Unitarias:** `src/test/` con JUnit.
- **Instrumentadas:** `src/androidTest/` con Espresso y Compose UI Test
  (`ExampleInstrumentedTest.kt`).

No hay una batería extensa de pruebas; el proyecto se apoya en verificación
manual por rol. Se recomienda cubrir con tests las validaciones de contraseña,
`ApiErrors`, `fetchAllPages` y el cálculo de KPIs/reportes.

---

## 28. Solución de problemas

| Síntoma | Causa probable | Solución |
|---------|----------------|----------|
| "Error de conexión" en todas las pantallas | Backend apagado o host/puerto mal configurados | Verifica `KAFFA_API_HOST`/`KAFFA_API_PORT`, la conectividad y que el backend responda (`curl http://kaffasena.tech/api/v1/categorias`) |
| La app no conecta contra `kaffasena.tech` | Se compiló sin el host/puerto del VPS | Define `KAFFA_API_HOST=kaffasena.tech` y `KAFFA_API_PORT=80` en `local.properties` y recompila |
| Funciona en emulador pero no en físico | Host por defecto distinto | Define `KAFFA_API_HOST`/`KAFFA_API_PORT` en `local.properties` |
| El `.aab` no se puede instalar en el teléfono | El AAB es formato de publicación, no instalable | Usa el APK de pruebas o genera APKs con `bundletool` |
| Login rechazado con cuenta recién creada | Correo sin verificar (`EMAIL_NOT_VERIFIED`) | Usa **Reenviar correo de verificación** o el enlace del correo |
| La transferencia no se acepta | Falta el comprobante | Adjunta la imagen antes de confirmar |
| No aparecen datos en KPIs/reportes | Paginación incompleta | Asegúrate de usar `fetchAllPages` |
| No hay métodos de pago en el POS | Admin no los ha creado | Panel Admin → Métodos de pago |
| La rotación de promociones no cambia | Sin promociones activas/vigentes | Crea/activa promociones en el panel |

---

## 29. Limitaciones conocidas

1. **Promociones locales:** no se sincronizan entre dispositivos; cada uno rota
   su propia copia (DataStore).
2. **Eventos dependen del backend:** requieren conectividad y host correctos.
3. **Token en DataStore sin cifrar.**
4. **Sondeo dependiente de los ViewModels:** el polling (chat, Kanban, turno) se
   detiene al minimizar la app.
5. **Sin R8/ProGuard** en release; APK más grande y ofuscación nula.
6. **Carrito en memoria:** se pierde al cerrar la app.
7. **Cleartext HTTP** habilitado: el backend del VPS se publica por HTTP
   (`kaffasena.tech:80`, sin TLS).
8. **Host y puerto compilados:** cambiar de servidor exige editar
   `local.properties` y recompilar.
9. **AAB no instalable directamente:** para probar el artefacto de entrega en un
   teléfono se requiere Play Store o `bundletool`.
10. **`versionCode` fijo en 1:** debe incrementarse antes de publicar
    actualizaciones.

---

## 30. Glosario

| Término | Definición |
|---------|------------|
| **DTO** | Objeto de transferencia de datos que refleja el JSON del backend |
| **DataStore** | Sistema de persistencia clave-valor de Jetpack |
| **Deep link** | Enlace externo (`kaffa://...`) que abre una pantalla concreta |
| **FileProvider** | Mecanismo para compartir archivos entre apps de forma segura |
| **Kanban** | Tablero de columnas por estado de pedido |
| **MediaStore** | API de Android para escribir en Descargas sin permisos (10+) |
| **MVVM** | Model-View-ViewModel |
| **POS** | Point of Sale (punto de venta) |
| **Resource** | Envoltorio de estado `Loading/Success/Error` |
| **Sesión** | Estado autenticado identificado por el token Bearer |

---

*Fin del manual técnico.*
