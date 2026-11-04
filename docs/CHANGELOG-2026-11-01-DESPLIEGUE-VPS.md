# Despliegue de la app móvil contra el VPS — Kaffa

**Fecha:** 2026-11-01  
**Autor:** Ana  
**Proyecto:** `movil-master` (KaffaCafeteria, Android + Kotlin + Jetpack Compose)  
**Artefacto entregado:** Android App Bundle (`.aab`) firmado

---

## 1. Resumen

El backend y el frontend ya estaban desplegados en el VPS. El problema era que la
app móvil compila la URL del backend **dentro del paquete**: por defecto apuntaba
a `http://127.0.0.1:8000` (equipo local), así que el APK/AAB no habría funcionado
contra el servidor.

Lo que hice:

1. Verifiqué que el backend responde públicamente en `http://kaffasena.tech/api/v1`.
2. Dejé el **puerto** del backend configurable (antes estaba fijo en `8000`).
3. Configuré `local.properties` con `kaffasena.tech` en el puerto `80`.
4. Corregí dos errores de compilación que impedían generar el release.
5. Compilé y firmé el **AAB** y un **APK** equivalente para pruebas.
6. Verifiqué la firma, el host compilado y la accesibilidad del backend.

---

## 2. Diagnóstico del backend en el VPS

Comprobé los endpoints con `curl` antes de recompilar:

| Prueba                                        | Resultado                           |
| --------------------------------------------- | ----------------------------------- |
| `GET http://kaffasena.tech/`                  | `200` (frontend desplegado)         |
| `GET http://kaffasena.tech/api/v1/categorias` | `200` (JSON de Laravel)             |
| `GET http://kaffasena.tech/docs`              | `200` (lo responde el frontend)     |
| `GET https://kaffasena.tech/`                 | sin respuesta (HTTPS no habilitado) |
| `GET http://kaffasena.tech:8000/api/v1/`      | timeout (puerto 8000 no expuesto)   |

Conclusión: el backend se consume por **HTTP en el puerto 80**, bajo la ruta
`/api/v1` (el mismo origen que el frontend). No se debe usar el puerto `8000`
desde Internet.

---

## 3. Cambios en el código

### 3.1 `app/build.gradle.kts` — puerto configurable

Agregué la lectura de `KAFFA_API_PORT` y su inyección en `BuildConfig`, igual que
ya se hacía con `KAFFA_API_HOST`:

```kotlin
val apiHost: String = signingProps.getProperty("KAFFA_API_HOST").orEmpty()
val apiPort: String = signingProps.getProperty("KAFFA_API_PORT").orEmpty()
```

```kotlin
buildConfigField("String", "API_HOST", "\"$apiHost\"")
buildConfigField("String", "API_PORT", "\"$apiPort\"")
```

### 3.2 `util/Constants.kt` — host y puerto desde la configuración

Antes el puerto era una constante fija (`PORT = 8000`). Ahora se lee de
`BuildConfig.API_PORT` y, si el puerto es `80`, se omite en la URL para no dejar
`http://kaffasena.tech:80/...`:

```kotlin
private const val DEFAULT_PORT = 8000

private val host: String = BuildConfig.API_HOST.ifBlank {
    if (isEmulator) HOST_EMULATOR else HOST_PHYSICAL
}

private val port: String = BuildConfig.API_PORT.ifBlank { DEFAULT_PORT.toString() }

private val authority: String = if (port == "80") host else "$host:$port"

val BASE_URL: String = "http://$authority/api/v1/"
val IMAGE_BASE_URL: String = "http://$authority/"
```

Con esto, en producción la app resuelve:

```
BASE_URL       = http://kaffasena.tech/api/v1/
IMAGE_BASE_URL = http://kaffasena.tech/
```

### 3.3 `local.properties` — apuntar al VPS (archivo no versionado)

```properties
KAFFA_API_HOST=kaffasena.tech
KAFFA_API_PORT=80
```

> `local.properties` está en `.gitignore` por contener rutas, credenciales de
> firma y ahora también el host/puerto del backend. Al reconstruir en otra
> máquina hay que volver a crear estas líneas.

### 3.4 Correcciones necesarias para compilar

El árbol de trabajo tenía dos errores que impedían generar cualquier release;
los corregí para poder continuar:

| Archivo | Error | Corrección |
|---|---|---|
| `ui/barista/BaristaPanelScreen.kt` | `Unresolved reference 'CoffeeBrown'` | Agregué `import com.example.kaffacafeteria.ui.theme.CoffeeBrown` |
| `ui/admin/AdminPanelScreen.kt` | Se pasaba `Collection<Int>` a un parámetro `List<Int>` | Agregué `.toList()` al calcular `rolesToSend` |

---

## 4. Compilación y firma

El `java` del sistema no estaba en el `PATH`, así que usé el JBR incluido:

```bash
export JAVA_HOME=/app/extra/jbr
export ANDROID_HOME=/home/ana/Android/Sdk
export ANDROID_SDK_ROOT=/home/ana/Android/Sdk
export PATH="$JAVA_HOME/bin:$PATH"

./gradlew bundleRelease --no-daemon   # genera el AAB firmado
./gradlew assembleRelease --no-daemon # genera un APK equivalente para pruebas
```

El keystore y las contraseñas ya estaban configurados en `local.properties`
(documentados en `docs/CREDENCIALES-FIRMA.md`), por lo que ambos artefactos
salieron firmados con la identidad `CN=Kaffa Cafeteria`.

---

## 5. Artefactos generados

Copié los resultados a `entrega/` con nombres claros:

| Archivo | Tamaño | SHA-256 | Uso |
|---|---|---|---|
| `entrega/KaffaCafeteria-v1.0-release.aab` | 23 MB | `3e65000061d799ab491e9c86c3d3eae79b929d6349b74307fa9b83b71b3a03cd` | **Entrega / Play Store** |
| `entrega/KaffaCafeteria-v1.0-vps.apk` | 24 MB | `bcf30706439a44b40b3f45bf40b2bb48a7884432eaee58dc03ef899da8d06bc5` | Instalación/pruebas en teléfono |

Rutas originales de Gradle:

```
app/build/outputs/bundle/release/app-release.aab
app/build/outputs/apk/release/app-release.apk
```

---

## 6. Verificaciones realizadas

| Comprobación | Comando | Resultado |
|---|---|---|
| Host compilado dentro del paquete | `grep API_HOST BuildConfig.java` | `kaffasena.tech` / `80` |
| Firma del AAB | `jarsigner -verify app-release.aab` | `jar verified.` |
| Firma del APK | `apksigner verify --print-certs ...` | `CN=Kaffa Cafeteria` |
| Paquete y versión | `aapt2 dump badging ...` | `com.example.kaffacafeteria`, `versionCode 1`, `versionName 1.0` |
| Backend accesible | `curl http://kaffasena.tech/api/v1/categorias` | `200` con JSON |

---

## 7. Advertencias y notas

1. **El `.aab` no se instala directamente en un teléfono.** Es el formato que
   pide la entrega/Play Store; para probarlo en un dispositivo usé el APK o
   habría que convertirlo con `bundletool`.
2. **El backend responde por HTTP, no HTTPS.** El manifiesto mantiene
   `android:usesCleartextTraffic="true"` y por eso funciona. Lo ideal a futuro es
   publicar el backend con TLS (por ejemplo, Certbot + Nginx) y cambiar el
   esquema a `https`.
3. **La URL queda compilada en el paquete.** Si el dominio o el puerto cambian,
   hay que editar `local.properties` y volver a compilar; no se puede cambiar
   desde la app.
4. **Versión sin incrementar.** Se mantuvo `versionCode 1` / `versionName 1.0`.
   Antes de subir una actualización a Play Store hay que incrementar
   `versionCode`.
5. **La firma local debe conservarse.** Si se pierde `kaffa-release.jks` no se
   podrán publicar actualizaciones con la misma identidad.

---

## 8. Recomendaciones pendientes

- Habilitar HTTPS en `kaffasena.tech` y migrar `BASE_URL`/`IMAGE_BASE_URL` a
  `https` para eliminar el tráfico en claro.
- Configurar **Play App Signing** si el AAB se va a publicar en Google Play.
- Mover el host/puerto a variables de entorno de CI (o a un archivo de
  configuración versionado sin secretos) para que el build no dependa de un
  `local.properties` manual.
- Generar el APK instalable desde el AAB con `bundletool` si se quiere validar
  exactamente el mismo artefacto que se entrega.

---

## 9. Actualización posterior: popup de promociones al entrar

Restauré el popup automático de la promoción del momento al abrir la app
(`ui/home/HomeScreen.kt`): se muestra en cuanto `PromocionesViewModel` carga
la promo destacada, junto con el próximo evento si existe. El cierre con "X"
sigue moviendo el evento al carrusel, pero las promociones ya no dependen de
`dismissedPromoIds`, por lo que vuelven a aparecer en cada arranque.

Los artefactos de `entrega/` se regeneraron con este cambio (ver tabla de la
sección 5).

---

**Última actualización:** 2026-11-01
