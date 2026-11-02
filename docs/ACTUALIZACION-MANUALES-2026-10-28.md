# Actualización de Manuales - Kaffa

**Fecha:** 2026-10-28  
**Versión:** 1.1

## Cambios Realizados en la Aplicación

### Cliente (App Móvil)
- **Eventos como anuncio pequeño:** Los eventos aparecen ahora como una ventana emergente compacta tipo anuncio.
- **Promociones en carrito:** Las promociones ya no aparecen como popup al inicio, se aplican desde el carrito.
- **Carrusel de eventos:** Se agregó sección "Próximos eventos" en el Home.
- **Descartar y volver a ver:** Al cerrar con X se marca como descartado y aparece en el carrusel. Se puede reabrir desde el botón de evento en la cabecera.

### Barista (App Móvil)
- **Pendiente:** Implementar menú lateral (Navigation Drawer) para acceder a Caja, Inventario, Reportar inventario, Chat sin scrollear.
- **Actual:** Panel Kanban con tarjetas modulares en la parte inferior.

### Administrador
- **Pendiente:** Corregir flujo de "Agregar Barista" desde el panel de administrador.

## Archivos Nuevos
- `ui/components/PromocionPopup.kt`
- `data/local/PromoPopupDismissStore.kt`
- `data/local/PromoSesion.kt`
- `domain/model/Promocion.kt`
- `ui/home/EventosPublicosViewModel.kt`

## Archivos Modificados
- `ui/home/HomeScreen.kt`
- `ui/admin/PromocionesScreen.kt`
- `ui/admin/PromocionesViewModel.kt`
- `data/local/CartStore.kt`
- `KaffaApp.kt`
- `ui/navigation/AppNavigation.kt`
- `ui/pos/POSScreen.kt`
- `ui/pos/POSViewModel.kt`
- `ui/splash/SplashScreen.kt`
- `ui/splash/SplashViewModel.kt`
