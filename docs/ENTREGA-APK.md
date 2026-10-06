# Todo lo que hice para dejar el APK listo para Classroom

Fecha: 2026-10-03
Proyecto: `/home/ana/Documentos/Movil_kaffa/movil-master` (Kaffa Cafeteria, Android + Compose + Retrofit)

Este documento está pensado para que lo copies y lo entregues junto al APK.
Los archivos de código ya están modificados en el proyecto: **no tienes que
aplicar nada a mano**, solo leer y, si quieres, reejecutar los comandos.

---

## 1. Resumen

El APK que había (`app/build/outputs/apk/debug/app-debug.apk`, 36 MB) servía, pero
el build de release salía **sin firmar**, y un APK sin firmar no se puede instalar.
Además la IP del backend estaba fija dentro del código Kotlin.

Lo que hice:

1. Creé un keystore de release y configuré la firma en Gradle.
2. Dejé el host del backend configurable desde `local.properties`.
3. Compilé debug y release, verifiqué las firmas.
4. Dejé los APKs en `entrega/` con nombres claros.
5. Documenté las credenciales en `docs/CREDENCIALES-FIRMA.md` y en el README.

---

## 2. Archivos modificados o creados

| Archivo | Acción | Qué hace |
|---|---|---|
| `app/build.gradle.kts` | Modificado | Lee credenciales de firma y `KAFFA_API_HOST` desde `local.properties`; añade `signingConfigs` para release |
| `app/src/main/java/com/example/kaffacafeteria/util/Constants.kt` | Modificado | El host del backend sale de `BuildConfig.API_HOST` en vez de estar fijo en el código |
| `local.properties` | Modificado (ignorado por git) | Credenciales de firma + `KAFFA_API_HOST` comentado |
| `.gitignore` | Modificado | Ignora `*.jks`, `*.keystore` y `/entrega` |
| `README.md` | Modificado | Secciones de firma, configuración del backend y entrega |
| `kaffa-release.jks` | Creado (ignorado por git) | Keystore de firma de la app |
| `docs/CREDENCIALES-FIRMA.md` | Creado | Datos de la clave de firma y cómo verificarlos |
| `entrega/KaffaCafeteria-v1.0-release.apk` | Creado | **El que se sube a Classroom** |
| `entrega/KaffaCafeteria-v1.0-debug.apk` | Creado | Alternativa con logs de red |

---

## 3. Credenciales de la firma (no se pierden)

| Dato | Valor |
|---|---|
| Keystore | `kaffa-release.jks` (raíz del proyecto) |
| Tipo | PKCS12 |
| Alias | `kaffa` |
| Contraseña del almacén | `kaffaiuSyLRJXRSOR` |
| Contraseña de la clave | `kaffaiuSyLRJXRSOR` |
| Algoritmo | RSA 2048, SHA384withRSA |
| Validez | 10000 días (~27 años) |
| Subject | `CN=Kaffa Cafeteria, OU=Movil Kaffa, O=Kaffa, L=Cali, C=CO` |
| Huella SHA-256 | `6d7e589247918a69a95b286d8aad892df70c018558a1cce293d5e65d1fc609a1` |

> Copia `kaffa-release.jks` y esta contraseña a Drive o a un gestor de
> contraseñas. Sin ese keystore no se puede publicar una actualización de la app:
> Android rechaza un APK firmado con una identidad distinta a la instalada.

Detalle completo en `docs/CREDENCIALES-FIRMA.md`.

---

## 4. Cambios en el código

### 4.1 `app/build.gradle.kts`

Bloque nuevo al inicio del archivo (antes de `android {`):

```kotlin
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// Credenciales de firma del release. Se leen de local.properties (ignorado por
// git) para no subir el keystore ni las contraseñas al repositorio.
val signingProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

// Host del backend. Si KAFFA_API_HOST está vacío en local.properties se usa el
// valor por defecto de Constants.kt (10.0.2.2 en emulador, IP de la PC en
// celular físico).
val apiHost: String = signingProps.getProperty("KAFFA_API_HOST").orEmpty()
```

Dentro de `defaultConfig`, una línea nueva:

```kotlin
        buildConfigField("String", "API_HOST", "\"$apiHost\"")
```

Y el bloque `signingConfigs` + `buildTypes` quedó así:

```kotlin
    val releaseStoreFile = signingProps.getProperty("RELEASE_STORE_FILE")
        ?.let { rootProject.file(it) }
        ?.takeIf { it.exists() }

    signingConfigs {
        if (releaseStoreFile != null) {
            create("release") {
                storeFile = releaseStoreFile
                storePassword = signingProps.getProperty("RELEASE_STORE_PASSWORD")
                keyAlias = signingProps.getProperty("RELEASE_KEY_ALIAS")
                keyPassword = signingProps.getProperty("RELEASE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            if (releaseStoreFile != null) {
                signingConfig = signingConfigs.getByName("release")
            }
            optimization {
                enable = false
            }
        }
    }
```

Lógica: si el keystore existe, se firma; si no, el build sigue funcionando pero
sale sin firmar (útil en CI, inválido para entregar).

### 4.2 `app/src/main/java/com/example/kaffacafeteria/util/Constants.kt`

Archivo completo:

```kotlin
package com.example.kaffacafeteria.util

import android.os.Build
import com.example.kaffacafeteria.BuildConfig

object Constants {
    private val isEmulator: Boolean =
        Build.FINGERPRINT.startsWith("generic") ||
            Build.FINGERPRINT.contains("emulator") ||
            Build.MODEL.contains("Emulator") ||
            Build.MODEL.contains("Android SDK built for") ||
            Build.MANUFACTURER.contains("Genymotion") ||
            Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic") ||
            Build.PRODUCT.contains("sdk")

    private const val HOST_EMULATOR = "10.0.2.2"
    private const val HOST_PHYSICAL = "192.168.80.15"
    private const val PORT = 8000

    /** Anfitrión del backend. Configurable con KAFFA_API_HOST en local.properties. */
    private val host: String = BuildConfig.API_HOST.ifBlank {
        if (isEmulator) HOST_EMULATOR else HOST_PHYSICAL
    }

    val BASE_URL: String = "http://$host:$PORT/api/v1/"
    val IMAGE_BASE_URL: String = "http://$host:$PORT/"

    const val PREF_NAME = "kaffa_prefs"
    const val TOKEN_KEY = "auth_token"
    const val USER_DATA_KEY = "user_data"
    const val PAGE_SIZE = 15
}
```

Lo único que cambia respecto al original es que `host` ahora sale de
`BuildConfig.API_HOST` en vez de estar escrito a mano.

### 4.3 `local.properties` (ignorado por git)

```properties
sdk.dir=/home/ana/Android/Sdk

# Firma del APK de release (keystore local, NO versionado).
# Ver README > Compilación.
RELEASE_STORE_FILE=kaffa-release.jks
RELEASE_STORE_PASSWORD=kaffaiuSyLRJXRSOR
RELEASE_KEY_ALIAS=kaffa
RELEASE_KEY_PASSWORD=kaffaiuSyLRJXRSOR

# Host del backend que se compila dentro del APK. Vacío = detección automática
# (10.0.2.2 en emulador, 192.168.80.15 en celular físico). Cámbialo si quien
# instala el APK corre el backend en otra máquina.
# Ver README > Configuración del backend.
#KAFFA_API_HOST=192.168.80.15
```

### 4.4 `.gitignore`

Al final del archivo:

```
# Keystore de firma del release (nunca subir al repositorio)
*.jks
*.keystore
/entrega
```

---

## 5. Comandos que ejecuté

Generar el keystore:

```bash
keytool -genkeypair -v \
  -keystore kaffa-release.jks \
  -storetype PKCS12 \
  -alias kaffa \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -storepass '<CONTRASEÑA>' -keypass '<CONTRASEÑA>' \
  -dname "CN=Kaffa Cafeteria, OU=Movil Kaffa, O=Kaffa, L=Cali, C=CO"
```

Compilar (nota: `java` no está en el PATH de esta máquina, hay que exportarlo):

```bash
export JAVA_HOME=/home/ana/.gradle/jdks/eclipse_adoptium-21-amd64-linux.2
export PATH="$JAVA_HOME/bin:$PATH"

./gradlew assembleDebug assembleRelease
./gradlew testDebugUnitTest
```

Copiar los APKs a la carpeta de entrega y verificar la firma:

```bash
rm -f entrega/*.apk
cp app/build/outputs/apk/debug/app-debug.apk   entrega/KaffaCafeteria-v1.0-debug.apk
cp app/build/outputs/apk/release/app-release.apk entrega/KaffaCafeteria-v1.0-release.apk

~/Android/Sdk/build-tools/36.0.0/apksigner verify entrega/KaffaCafeteria-v1.0-release.apk
```

Resultado de la verificación (ambos OK):

```
Signer #1 certificate DN: CN=Kaffa Cafeteria, OU=Movil Kaffa, O=Kaffa, L=Cali, C=CO
Signer #1 certificate SHA-256 digest: 6d7e589247918a69a95b286d8aad892df70c018558a1cce293d5e65d1fc609a1
```

---

## 6. APKs entregados

En `entrega/`:

| Archivo | Tamaño | SHA-256 |
|---|---|---|
| `KaffaCafeteria-v1.0-release.apk` | 24 MB | `60b1441e1f157ef8c81f58d25b2f3a11a6672c4614bb3bcfe9e835bc64a7dfe7` |
| `KaffaCafeteria-v1.0-debug.apk` | 35 MB | `7e89783692203bd6298498512f5e2d6af9aae96c43a246595e03665c985e0b95` |

**Sube `KaffaCafeteria-v1.0-release.apk` a Classroom.**

No subas `app-debug-androidTest.apk`: ese es el APK de los tests instrumentados,
no el de la app.

---

## 7. Advertencia importante para el instructor

La app no tiene el backend embebido: al arrancar, `KaffaApp.kt` crea Retrofit con
`http://<host>:8000/api/v1/`. Quien instale el APK debe:

1. Estar en la misma red Wi-Fi que la máquina que corre el backend.
2. Tener el backend levantado en el puerto `8000`.
3. Que esa máquina tenga la IP `192.168.80.15` (o la que se haya compilado).

Si el instructor corre el backend en otra IP, hay que cambiar `KAFFA_API_HOST`
en `local.properties` y recompilar:

```bash
export JAVA_HOME=/home/ana/.gradle/jdks/eclipse_adoptium-21-amd64-linux.2
./gradlew assembleRelease
cp app/build/outputs/apk/release/app-release.apk entrega/KaffaCafeteria-v1.0-release.apk
```

El manifest tiene `android:usesCleartextTraffic="true"`, por eso funciona con
HTTP sin HTTPS.

---

## 8. Estado de la verificación

| Comprobación | Resultado |
|---|---|
| `./gradlew assembleDebug` | BUILD SUCCESSFUL |
| `./gradlew assembleRelease` | BUILD SUCCESSFUL |
| `./gradlew testDebugUnitTest` | BUILD SUCCESSFUL |
| `apksigner verify` (release) | Firma válida |
| `apksigner verify` (debug) | Firma válida |

Los `test` del proyecto son las plantillas por defecto de Android Studio
(`ExampleUnitTest` y `ExampleInstrumentedTest`): no cubren lógica de negocio,
así que su éxito no dice mucho sobre la app.

---

## 9. Qué NO cambié y por qué

- **No añadí un campo "servidor" en el login.** Permitir cambiar el backend en
  caliente exigiría reconstruir Retrofit (se crea una sola vez en
  `Application.onCreate`) y además Coil carga las imágenes con su propio cliente
  HTTP, así que habría que tocar las dos capas. Es una funcionalidad que el
  instructor no pidió y se vería rara en una entrega académica.
- **No activé R8/ProGuard.** El release ya venía con `optimization { enable =
  false }` en el repo; lo dejé igual.
- **No toqué la lógica de la app** (pantallas, repositorios, autenticación).
  Los cambios son solo de empaquetado y firma.