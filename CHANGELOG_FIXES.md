# Cambios realizados - Arreglo de promos/eventos y conexión

Fecha: $(date)

## Problemas corregidos

### 1) Conexión con backend
- Se ajustó `app/src/main/java/com/example/kaffacafeteria/util/Constants.kt` para usar `127.0.0.1:8000` como host en dispositivo físico/emulador según entorno, facilitando la conexión con backend local en ejecución.

### 2) Comportamiento de promociones y eventos
- **Eliminadas las ventanas emergentes (popups) de promociones** en `HomeScreen` y `POSScreen`. Ya no aparecen automáticamente al entrar o al cambiar de pantalla.
- **Los eventos permanecen como ventana emergente** cuando corresponda, pero con la nueva lógica de persistencia.
- **Al cerrar (X) una promoción o un evento desde el popup**, estos pasan a mostrarse en el carrusel correspondiente para que sigan visibles.
- **Se añade un carrusel de promociones ("Promociones")** en `HomeScreen` (arriba de destacados/eventos) mostrando las promociones activas/vigentes. Al cerrarlas desde el popup se marcan para mostrarse en carrusel.
- **Se añade gestión de estado para recordar qué promos/eventos fueron descartados del popup** usando `SharedPreferences`/DataStore local (`prefs_promo_evento`) para que al cerrarlos con X queden en el carrusel y no vuelvan a aparecer como popup.

### 3) Ajustes en componentes
- `PromocionPopup` sigue funcionando pero ahora se usa principalmente para eventos o según nueva lógica; las promociones ya no se fuerzan como popup.
- Se actualiza la lógica en `HomeViewModel`/uso en pantallas para respetar el nuevo comportamiento.

## Notas
- No se eliminaron endpoints ni lógica del backend. Solo cambios en la app móvil.
- Las promociones se siguen gestionando localmente (DataStore) como antes.

## Cambios detallados en código
- `app/src/main/java/com/example/kaffacafeteria/util/Constants.kt` - Ajustado host para conexión local
- `app/src/main/java/com/example/kaffacafeteria/data/local/PromoPopupDismissStore.kt` - Nuevo almacén para recordar promos/eventos cerrados con X
- `app/src/main/java/com/example/kaffacafeteria/KaffaApp.kt` - Agregada extensión para acceder al store
- `app/src/main/java/com/example/kaffacafeteria/ui/home/HomeScreen.kt` - Promociones ya NO aparecen como popup. Se agrega carrusel "Promociones". Eventos solo aparecen como popup si no fueron cerrados con X; al cerrarlos pasan al carrusel
- `app/src/main/java/com/example/kaffacafeteria/ui/pos/POSScreen.kt` - Mismo comportamiento: sin popup de promociones, solo eventos
