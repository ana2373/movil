# Eventos + Asistente IA — Qué se hizo y dónde está

Documento de entrega del trabajo de **Eventos** (crear eventos desde el panel de
administración de la app y mostrarlos en la web) e **Integración de IA**
(ícono flotante del asistente en la app Android, disponible para todos los
usuarios).

Fecha: 2026-10-13

---

## 1. Resumen

| Necesidad | Solución implementada | Dónde |
|---|---|---|
| Crear eventos que se vean en la web | CRUD de eventos en el backend + pantalla "Eventos" en el panel admin de la app Android | Laravel + Android |
| Integrar una IA | Asistente de **IA local** (sin API externa) que responde con datos reales; ícono flotante en la app | Laravel + Android |
| Que la IA esté en el móvil | Ícono flotante tipo avatar de café, visible para **todos los usuarios autenticados** | Android |

La app Android ya existía; el frontend React de la web también. El backend
Laravel **no tenía** endpoints de `eventos` ni de `asistente`, por lo que fueron
los que se crearon.

---

## 2. Backend (Laravel)

Ruta del proyecto: `/home/ana/Documentos/backend-kaffa`

> El prefijo de todas las rutas es `/api/v1` (ver `routes/api.php`).

### 2.1 Archivos NUEVOS

Migraciones (crean las tablas):

- `database/migrations/2026_10_13_000000_create_eventos_table.php` — tabla `eventos`
  (`titulo`, `descripcion`, `lugar`, `fecha`, `hora`, `publicado`, `softDeletes`).
- `database/migrations/2026_10_13_000001_create_evento_imagenes_table.php` — galería
  `evento_imagenes` (`evento_id`, `url`, `orden`).
- `database/migrations/2026_10_13_000002_create_asistente_mensajes_table.php` — historial
  `asistente_mensajes` (`usuario_id`, `rol`, `contenido`).

Modelos:

- `app/Models/Evento.php` (relación `imagenes`, soft deletes).
- `app/Models/EventoImagen.php`.
- `app/Models/AsistenteMensaje.php`.

Controladores:

- `app/Http/Controllers/EventoController.php` — index público, adminIndex, show,
  store, update, destroy y `subirImagen`.
- `app/Http/Controllers/AsistenteController.php` — index (historial), chat, destroy.

Servicio (la "IA"):

- `app/Services/AsistenteService.php` — IA **local** por detección de intención
  (palabras clave). Calcula ventas, top de productos, insumos por reponer,
  gastos y una predicción simple (promedio móvil). Para el rol **cliente**
  responde sobre menú, formas de pago, cómo pedir y próximos eventos.
  Un comentario en el archivo indica dónde enchufar OpenAI/Gemini/Groq si se
  desea más adelante.

Autorización:

- `app/Policies/EventoPolicy.php` — policy de eventos.

### 2.2 Archivos MODIFICADOS

- `routes/api.php`
  - `GET /eventos` **público** (sólo eventos publicados).
  - Bloque autenticado `role:admin`: `GET /eventos/admin`, `POST /eventos`,
    `POST /eventos/imagen`, `PUT /eventos/{id}`, `DELETE /eventos/{id}`.
  - Bloque `asistente/*` (**cualquier autenticado**, ya no sólo admin):
    `GET /asistente/mensajes`, `POST /asistente/chat`, `DELETE /asistente/mensajes`.
- `app/Providers/AuthServiceProvider.php`
  - Registra `Evento::class => EventoPolicy::class` y añade `Evento` a
    `adminOnlyModels` (gate `create-record`).
- `app/Http/Controllers/AsistenteController.php`
  - Calcula si el usuario es staff (`admin`/`barista`) y se lo pasa al servicio.
- `app/Services/AsistenteService.php`
  - `responder($pregunta, $usuarioId, $esStaff)` y modo cliente
    (`generarRespuestaCliente`) con respuestas sobre menú, pagos, pedidos y eventos.

### 2.3 Endpoints (contrato)

Eventos:

```
GET    /api/v1/eventos            (público; sólo publicado=true, orden fecha asc)
GET    /api/v1/eventos/admin      (admin; todos, orden fecha desc)
POST   /api/v1/eventos            (admin)
PUT    /api/v1/eventos/{id}       (admin)
DELETE /api/v1/eventos/{id}       (admin)
POST   /api/v1/eventos/imagen     (admin; multipart, campo "imagen" → { "url": "..." })
```

Cuerpo de un evento:

```json
{ "titulo", "descripcion", "lugar", "fecha": "YYYY-MM-DD",
  "hora": "HH:mm", "publicado": true, "imagenes": ["https://.../storage/eventos/x.jpg"] }
```

Asistente:

```
GET    /api/v1/asistente/mensajes  (auth; más reciente primero; paginado)
POST   /api/v1/asistente/chat      (auth) body { "mensaje": "..." }
                                   → { "respuesta", "mensaje_id", "created_at" }
DELETE /api/v1/asistente/mensajes  (auth; limpia el historial del usuario)
```

### 2.4 Estado de la base de datos (RESUELTO)

El error al crear eventos y al usar el asistente era:

```
SQLSTATE[42P01]: Undefined table: relation "eventos" / "asistente_mensajes" does not exist
```

Faltaba **ejecutar las migraciones** (las tablas no existían). Ya quedó aplicado
sobre la base de datos PostgreSQL `Backend`:

- Tablas creadas: `eventos`, `evento_imagenes`, `asistente_mensajes`.
- Migraciones registradas en la tabla `migrations`.
- Verificado que el asistente puede consultar categorías, productos, ventas,
  top de productos, insumos, gastos y eventos sin errores.

En el entorno de desarrollo no hay PHP instalado, así que las tablas se
aplicaron con el script SQL de respaldo:

- `database/sql/2026_10_13_eventos_asistente.sql`

Si prefieres regenerarlas en otra máquina con Laravel:

```bash
cd /home/ana/Documentos/backend-kaffa
php artisan migrate            # crea las 3 tablas
php artisan storage:link       # imprescindible para servir /storage/eventos/...
```

> `storage:link` sólo hace falta para **ver las imágenes** de los eventos; sin él
> el evento igual se crea, pero la foto no se muestra.

### 2.5 Correcciones de código tras la prueba

- `app/Services/AsistenteService.php`: la tabla `categorias` no tiene columna
  `activo`; se quitó ese filtro (habría producido
  `column "activo" does not exist`).
- `app/Http/Controllers/EventoController.php`: `descripcion` ahora es opcional y
  se normaliza `null → ''` (la columna es `NOT NULL`), evitando un 422/500 si el
  evento no lleva descripción.

### 2.6 Asistente consciente del rol y sin "listas de preguntas"

Problema reportado: *el asistente se mezclaba entre cliente, admin y barista y
respondía con toda clase de preguntas antes de contestar lo solicitado.*

Cambios:

- **Backend (`AsistenteService` / `AsistenteController`):** ahora recibe el rol
  (`cliente`, `barista` o `admin`) y responde distinto en cada caso.
  - `cliente`: menú, medios de pago, cómo pedir, eventos, sede.
  - `barista`: ventas del día, productos más vendidos, inventario y pedidos
    (`pedidosEstado()`), sin acceso a gastos/predicciones.
  - `admin`: todo lo anterior **más** gastos, predicciones y recomendaciones.
  - El *fallback* (cuando no reconoce la intención) ya **no** imprime la lista
    completa de preguntas; ofrece una sola frase con 2‑4 ideas afines.
  - Los saludos también son breves y según el rol.
- **App Android:** la conversación dejó de mezclarse entre usuarios.
  - `AsistenteViewModel.sincronizarSesion()` limpia y recarga el historial cuando
    cambia la sesión (antes el ViewModel vivía a nivel de Activity y conservaba
    los mensajes del usuario anterior).
  - `AppNavigation` incrementa un `sessionKey` en cada login y lo pasa al
    asistente; `AsistenteFlotante` también recibe el `rol`.
  - Las sugerencias rápidas (`sugerenciasPorRol`) ahora son distintas para
    cliente, barista y admin.


---

## 3. Frontend web (React) — sin cambios

Ruta del proyecto: `/home/ana/Documentos/Frontend_kaffa/REACT_clean/REACT_kaffa`

La web **ya tenía** las páginas y hooks que consumen estos endpoints, por eso no
se tocó. Archivos relevantes (sólo referencia):

- `src/pages/dashboard-admin/EventosView.jsx` — gestión de eventos (admin).
- `src/pages/dashboard-admin/AsistenteView.jsx` — chat del asistente.
- `src/pages/Eventos.jsx` y `src/hooks/useEventos.js` — eventos de la landing.
- `src/lib/api.js` — base de las llamadas.

Cuando se cree/publica un evento desde la **app Android**, aparecerá
automáticamente en la sección "Eventos" de la web.

---

## 4. App Android

Ruta del proyecto: `/home/ana/Documentos/Movil_kaffa/movil-master`
(Kotlin + Jetpack Compose, paquete `com.example.kaffacafeteria`).

### 4.1 Archivos NUEVOS

Datos (API):

- `app/src/main/java/com/example/kaffacafeteria/data/remote/dto/EventoDtos.kt`
  — `EventoDto`, `EventoImagenDto`, `EventoRequest`, `EventoResponse`, `ImagenSubidaResponse`.
- `app/src/main/java/com/example/kaffacafeteria/data/remote/dto/AsistenteDtos.kt`
  — `AsistenteMensajeDto`, `AsistenteChatRequest`, `AsistenteChatResponse`.
- `app/src/main/java/com/example/kaffacafeteria/data/remote/api/EventoApi.kt`
  — Retrofit de eventos (incluye subida multipart de imagen).
- `app/src/main/java/com/example/kaffacafeteria/data/remote/api/AsistenteApi.kt`
  — Retrofit del asistente.

Pantalla admin de Eventos:

- `app/src/main/java/com/example/kaffacafeteria/ui/admin/EventosViewModel.kt`
  — carga, crear, editar, eliminar y publicar/despublicar; sube la imagen al
  backend (copia la `Uri` a un archivo temporal y la manda como multipart).
- `app/src/main/java/com/example/kaffacafeteria/ui/admin/EventosScreen.kt`
  — lista de eventos + diálogo de alta/edición con fecha, hora, imagen y switch
  de "publicado".

Ícono flotante del asistente:

- `app/src/main/java/com/example/kaffacafeteria/ui/asistente/AsistenteViewModel.kt`
  — historial y envío de mensajes.
- `app/src/main/java/com/example/kaffacafeteria/ui/asistente/AsistenteFlotante.kt`
  — botón flotante (avatar de café) + diálogo de chat con burbujas.

### 4.2 Archivos MODIFICADOS

- `app/src/main/java/com/example/kaffacafeteria/KaffaApp.kt`
  — el `AppContainer` ahora expone `eventoApi` y `asistenteApi`.
- `app/src/main/java/com/example/kaffacafeteria/ui/admin/AdminPanelScreen.kt`
  — nueva tarjeta **"Eventos"** y parámetro `onNavigateToEventos`.
- `app/src/main/java/com/example/kaffacafeteria/ui/navigation/AppNavigation.kt`
  - Ruta `Screen.Eventos` (`admin/eventos`) y su `composable`.
  - El `NavHost` quedó dentro de un `Box` y se dibuja encima el
    `AsistenteFlotante()` cuando `isLoggedIn` es true (todos los usuarios
    autenticados, cualquier pantalla).

### 4.3 Cómo se usa

- **Crear evento (admin):** entrar al *Panel de Administración* → **Eventos** →
  botón **+** → llenar título, descripción, lugar, fecha, hora, elegir imagen y
  marcar "publicado". Al guardar se sube al backend y aparece en la web.
- **Asistente IA:** en cualquier pantalla (con sesión iniciada) hay un botón
  circular café en la esquina inferior derecha; al pulsarlo se abre el chat.
  - Admin/barista: preguntas de negocio (ventas, top productos, insumos,
    gastos, predicción).
  - Cliente: menú, formas de pago, cómo hacer un pedido y próximos eventos.

### 4.4 Configuración de red

El host del backend se toma de `util/Constants.kt` / `local.properties`:

- Emulador: `10.0.2.2`.
- Celular físico: `192.168.80.15` (cambiar si el backend corre en otra máquina,
  ver comentario en `local.properties`).

---

## 5. Verificación realizada

- **Android:** `./gradlew :app:compileDebugKotlin` → `BUILD SUCCESSFUL`
  (sin errores; sólo advertencias de deprecación, en su mayoría preexistentes).
- **Backend:** no se pudo ejecutar `php artisan` en este entorno (no hay PHP
  instalado). Los archivos quedaron escritos y revisados; falta correr
  `php artisan migrate` y `php artisan storage:link` en la máquina del backend.

---

## 6. Limitaciones / notas

- La "IA" es **local** (reglas + datos reales), no un modelo de lenguaje en la
  nube. Para usar OpenAI/Gemini/Groq hay que sustituir
  `AsistenteService::generarRespuesta()` (hay un comentario guía en el archivo).
- La app sube **una imagen** por evento; el backend soporta hasta 5 en la
  galería (la web puede usar todas).
- El asistente aparece sólo con **sesión iniciada** (el endpoint requiere token).
