# Registro de Cambios - Kaffa v1.1

**Fecha:** 2026-10-28  
**Autor:** Equipo de Desarrollo  
**Versión:** 1.1

---

## Resumen

Se documentan los cambios realizados en la aplicación Kaffa para mejorar la experiencia de usuario en los tres roles, con base en las correcciones solicitadas.

---

## 1. Cambios Implementados

### 1.1 App Móvil - Cliente (HomeScreen)

| Cambio | Descripción | Archivos Afectados |
| :--- | :--- | :--- |
| **Eventos como anuncio emergente pequeño** | Los eventos ahora aparecen en una ventana emergente compacta tipo anuncio, similar al diseño de promociones. | `ui/home/HomeScreen.kt`, `ui/components/PromocionPopup.kt` (nuevo) |
| **Promociones solo en carrito** | Las promociones ya no aparecen como popup al entrar al Home. Se mantienen para aplicarse directamente en el carrito con descuento. | `ui/home/HomeScreen.kt`, `data/local/CartStore.kt` |
| **Sistema de descarte de popups** | Se implementó `PromoPopupDismissStore` para recordar qué anuncios han sido cerrados con la "X". | `data/local/PromoPopupDismissStore.kt` (nuevo), `KaffaApp.kt` |
| **Carrusel de eventos** | Se agregó un carrusel de "Próximos eventos" en el Home. Los eventos descartados con "X" aparecen ahí para poder consultarlos nuevamente. | `ui/home/HomeScreen.kt`, `ui/home/EventosPublicosViewModel.kt` (nuevo) |
| **Botón para reabrir eventos** | Se agregó un botón con ícono de evento en la cabecera del Home para volver a mostrar el anuncio si existe un evento no descartado. | `ui/home/HomeScreen.kt` |

### 1.2 App Móvil - Promociones en Carrito

| Cambio | Descripción | Archivos Afectados |
| :--- | :--- | :--- |
| **Aplicación de promociones al carrito** | Las promociones se aplican con descuento al agregar productos al carrito. Se muestra el precio tachado, descuento y ahorro. | `data/local/CartStore.kt`, `ui/pos/POSScreen.kt`, `ui/pos/POSViewModel.kt` |
| **Gestión desde panel admin** | Se mejoró la interfaz de administración de promociones. | `ui/admin/PromocionesScreen.kt`, `ui/admin/PromocionesViewModel.kt` |

### 1.3 App Móvil - Navegación y Configuración

| Cambio | Descripción | Archivos Afectados |
| :--- | :--- | :--- |
| **Limpieza del Splash** | Se simplificó la pantalla Splash, removiendo rotación de promociones. | `ui/splash/SplashScreen.kt`, `ui/splash/SplashViewModel.kt` |
| **Ajustes de navegación** | Se actualizaron rutas y flujo de navegación. | `ui/navigation/AppNavigation.kt` |
| **Constantes actualizadas** | Ajustes en configuración. | `util/Constants.kt` |

---

## 2. Cambios Pendientes (Por Implementar)

Estos son los cambios que aún faltan por aplicar según tu solicitud:

| Cambio | Descripción | Estado |
| :--- | :--- | :--- |
| **Menú lateral izquierdo en Panel Barista** | Implementar `ModalNavigationDrawer` con acceso a: Órdenes/Pedidos, Caja de cobro, Inventario/Insumos, Reportar inventario/Mermas, Chat, Perfil, Cerrar sesión. Esto permitirá acceder a todas las funciones aunque haya muchos pedidos en el Kanban. | **Pendiente** |
| **Scroll al carrusel al cerrar anuncio** | Al presionar la "X" en el popup de evento, además de marcarlo como descartado, hacer scroll automático hacia el carrusel de eventos ya existente en el Home (sin crear un carrusel nuevo). | **Pendiente** |
| **Corregir alta de Baristas desde Admin** | Revisar y corregir el flujo para agregar baristas desde el panel de administrador. Verificar envío correcto de roles y validación. | **Pendiente** |

---

## 3. Notas Técnicas

- **Archivos nuevos sin rastrear:** Los archivos con `??` en git status contienen gran parte de la funcionalidad de eventos/promociones.
- **No se crean carruseles nuevos:** Se reutiliza el carrusel de eventos ya existente en `HomeScreen` (`EventoCarousel`).
- **Persistencia de descartados:** Se usa `DataStore` vía `PromoPopupDismissStore` para recordar anuncios cerrados.

---

**Última actualización:** 2026-10-28
