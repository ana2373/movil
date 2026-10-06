# Credenciales de firma del APK (Kaffa Cafeteria)

> [!IMPORTANT]
> **Guarda este archivo y el keystore fuera del repositorio** (copia en Drive,
> OneDrive o un gestor de contraseñas). Sin `kaffa-release.jks` **no se puede
> publicar una actualización** de la app: Android rechaza instalar un APK firmado
> con una identidad distinta a la ya instalada en el dispositivo.

## Datos del keystore

| Dato | Valor |
|---|---|
| Archivo del keystore | `kaffa-release.jks` (en la raíz del proyecto) |
| Tipo de almacén | `PKCS12` |
| Alias de la clave | `kaffa` |
| Contraseña del almacén | `kaffaiuSyLRJXRSOR` |
| Contraseña de la clave | `kaffaiuSyLRJXRSOR` |
| Algoritmo / tamaño | RSA 2048, SHA384withRSA |
| Validez | 10000 días (~27 años) desde 2026-10-03 |
| Subject | `CN=Kaffa Cafeteria, OU=Movil Kaffa, O=Kaffa, L=Cali, C=CO` |
| Huella SHA-256 del certificado | `6d:7e:58:92:47:91:8a:69:a9:5b:28:6d:8a:ad:89:2d:f7:0c:01:85:58:a1:cc:e2:93:d5:e6:5d:1f:f6:09:a1` |

## Dónde está configurado

Las credenciales viven en `local.properties` (ignorado por `.gitignore`), que
`app/build.gradle.kts` lee para construir el `signingConfig` de release:

```properties
RELEASE_STORE_FILE=kaffa-release.jks
RELEASE_STORE_PASSWORD=kaffaiuSyLRJXRSOR
RELEASE_KEY_ALIAS=kaffa
RELEASE_KEY_PASSWORD=kaffaiuSyLRJXRSOR
```

Si `RELEASE_STORE_FILE` no apunta a un archivo existente, Gradle omite el
`signingConfig` y el release sale **sin firmar** (no se puede instalar).

## Regenerar el APK firmado

```bash
./gradlew assembleRelease
cp app/build/outputs/apk/release/app-release.apk entrega/KaffaCafeteria-v1.0-release.apk
```

Verificar la firma antes de entregar:

```bash
$ANDROID_HOME/build-tools/36.0.0/apksigner verify --print-certs \
  entrega/KaffaCafeteria-v1.0-release.apk
```

## Verificar la huella (debe coincidir con la de arriba)

```bash
keytool -list -v -keystore kaffa-release.jks -storepass kaffaiuSyLRJXRSOR
```

## Crear un keystore nuevo

Solo si el original se pierde y todavía no se publicó nada en Google Play. **Si
ya entregaste el APK, no cambies la firma**: la app instalada quedaría con firma
distinta y no se podría actualizar.

```bash
keytool -genkeypair -v -keystore kaffa-release.jks -storetype PKCS12 \
  -alias kaffa -keyalg RSA -keysize 2048 -validity 10000
```

## APK entregados

En `entrega/` (ignorada por git):

| Archivo | Descripción |
|---|---|
| `KaffaCafeteria-v1.0-release.apk` | Release firmado — **este es el que se sube a Classroom** |
| `KaffaCafeteria-v1.0-debug.apk` | Variante debug, útil si el instructor quiere ver los logs de red |

No subir `app-debug-androidTest.apk`: es el APK de los tests instrumentados, no
de la app.