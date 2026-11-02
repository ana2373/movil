# Manual de usuario — App móvil Kaffa

Guía de uso de la aplicación Android **KaffaCafeteria** para los tres roles:
**Cliente**, **Barista** y **Administrador**.

- **Fecha:** 2026-10-19
- **Versión de la app:** 1.0

---

## Tabla de contenido

1. [¿Qué es Kaffa?](#1-qué-es-kaffa)
2. [Requisitos](#2-requisitos)
3. [Instalación y primer acceso](#3-instalación-y-primer-acceso)
   - 3.1 [Iniciar sesión](#31-iniciar-sesión)
   - 3.2 [Crear una cuenta](#32-crear-una-cuenta-clientes)
   - 3.3 [Verificar el correo](#33-verificar-el-correo)
   - 3.4 [¿Olvidaste tu contraseña?](#34-olvidaste-tu-contraseña)
4. [La pantalla de bienvenida y las promociones](#4-la-pantalla-de-bienvenida-y-las-promociones)
5. [Uso como Cliente](#5-uso-como-cliente)
   - 5.1 [Explorar el menú](#51-explorar-el-menú)
   - 5.2 [Carrito y compra](#52-carrito-y-compra)
   - 5.3 [Métodos de pago](#53-métodos-de-pago)
   - 5.4 [Mis pedidos](#54-mis-pedidos)
   - 5.5 [Eventos](#55-eventos)
   - 5.6 [Chat](#56-chat)
   - 5.7 [Perfil y modo oscuro](#57-perfil-y-modo-oscuro)
6. [Uso como Barista](#6-uso-como-barista)
   - 6.1 [Panel de pedidos (Kanban)](#61-panel-de-pedidos-kanban)
   - 6.2 [Turno](#62-turno)
   - 6.3 [Caja](#63-caja)
   - 6.4 [Punto de venta (POS)](#64-punto-de-venta-pos)
   - 6.5 [Reportar inventario](#65-reportar-inventario)
7. [Uso como Administrador](#7-uso-como-administrador)
   - 7.1 [Panel y métricas](#71-panel-y-métricas)
   - 7.2 [Usuarios](#72-usuarios)
   - 7.3 [Categorías y productos](#73-categorías-y-productos)
   - 7.4 [Métodos de pago](#74-métodos-de-pago)
   - 7.5 [Promociones](#75-promociones)
   - 7.6 [Eventos](#76-eventos)
   - 7.7 [Inventario, compras y gastos](#77-inventario-compras-y-gastos)
   - 7.8 [Turnos](#78-turnos)
   - 7.9 [Reportes](#79-reportes)
8. [El asistente KAFFA](#8-el-asistente-kaffa)
9. [Solución de problemas](#9-solución-de-problemas)
10. [Preguntas frecuentes (FAQ)](#10-preguntas-frecuentes-faq)
11. [Privacidad y datos](#11-privacidad-y-datos)
12. [Glosario](#12-glosario)

---

## 1. ¿Qué es Kaffa?

Aplicación móvil de la cafetería Kaffa para consultar el menú, hacer pedidos,
gestionar el punto de venta, la caja, el inventario y administrar toda la
operación del negocio. Su funcionamiento depende de tres roles:

| Rol | Puede hacer |
|-----|-------------|
| **Cliente** | Ver el menú, pedir, pagar, seguir sus pedidos y chatear |
| **Barista** | Atender pedidos, cobrar, manejar su caja, su turno y reportar inventario |
| **Administrador** | Todo lo anterior más usuarios, catálogo, promociones, eventos, inventario, finanzas y reportes |

---

## 2. Requisitos

- Teléfono o emulador **Android 7.0 (API 24)** o superior.
- Conexión a la **misma red Wi-Fi** en la que corre el servidor de Kaffa.
- El **backend de Kaffa** debe estar encendido (la app apunta a su dirección).

> Si la app muestra "Error de conexión", verifica que el backend esté activo y
> que el celular esté en la red correcta.

---

## 3. Instalación y primer acceso

1. Instala el archivo `KaffaCafeteria-v1.0-release.apk` (o la versión `debug`
   para pruebas). Puede pedirte permitir "instalar apps de origen desconocido".
2. Abre la app: verás la pantalla de bienvenida con el **logo de Kaffa**.
3. Pulsa **Entrar** (o espera unos segundos a que continue sola).
4. Inicia sesión con tu correo y contraseña, o pulsa **Registrarse**.

### 3.1 Iniciar sesión

- Ingresa **correo** y **contraseña** y pulsa **Iniciar sesión**.
- Según tu rol, la app te lleva al panel correspondiente:
  - **Cliente** → inicio con el menú.
  - **Barista** → panel de pedidos.
  - **Administrador** → panel de administración.

### 3.2 Crear una cuenta (Clientes)

1. En el inicio de sesión pulsa **Registrarse**.
2. Completa nombre, correo y contraseña.
3. La contraseña debe tener **mínimo 8 caracteres**, con **mayúscula,
   minúscula, número y símbolo**.
4. Al registrarte, **no entras directamente**: la app te lleva a la pantalla
   "Revisa tu correo" porque la cuenta debe verificarse primero.

### 3.3 Verificar el correo

1. Revisa la bandeja de entrada del correo con el que te registraste.
2. Abre el enlace de verificación que te envió Kaffa.
3. Vuelve a la app e inicia sesión.

> Si no llegó el correo, usa el botón **Reenviar correo de verificación** que
> aparece tras intentar iniciar sesión con una cuenta sin verificar, o el de la
> pantalla "Revisa tu correo".

### 3.4 ¿Olvidaste tu contraseña?

1. En el inicio de sesión pulsa **¿Olvidaste tu contraseña?**.
2. Escribe tu correo y envíalo. Por seguridad, el mensaje es el mismo exista o
   no la cuenta.
3. Revisa tu correo y abre el enlace **"Restablecer contraseña"**; se abrirá la
   app directamente en la pantalla de **nueva contraseña** (ya trae tu token y
   correo).
4. Escribe la nueva contraseña (mismas reglas de complejidad) y confirma.
5. Si el enlace ya expiró, la app te ofrece **solicitar uno nuevo**.

---

## 4. La pantalla de bienvenida y las promociones

Al **abrir la app** verás la pantalla de bienvenida con el **logo de Kaffa**.
Se retira sola en unos segundos (o pulsa **Entrar** para continuar).

Las promociones las crea el administrador (con color, imagen, fecha de
vencimiento, productos y descuento) y **no se muestran en el splash**: aparecen
como una **pantalla emergente pequeña**:

- Se abre sola al entrar al **Inicio** y al **Carrito**.
- Muestra la **promoción del momento** (nombre, descripción, imágenes, cuántos
  productos incluye y el precio final) y el **próximo evento** justo debajo.
- Se cierra con la **X**. Para volver a verla, pulsa el icono de **etiqueta
  (🏷)** de la cabecera.
- Si hay varias promociones vigentes, **cada vez que abres la app avanza a la
  siguiente** (rotación que recuerda cuál mostró por última vez).

### Comprar la promoción

- Pulsa **"Comprar promo y pagar en el carrito"**: los productos de la
  promoción se agregan al carrito **con el precio de la promoción** (aparece
  tachado el precio normal y el descuento) y se abre el **Carrito**.
- En el carrito verás una insignia con el nombre de la promoción y lo que
  ahorras. Con **Quitar** vuelves al precio normal.
- Ahí eliges el método de pago y pulsas **Cobrar**.

---

## 5. Uso como Cliente

Tras iniciar sesión, tu pantalla de inicio incluye un saludo, un buscador, el
menú por categorías, una sección de **Destacados** que rota automáticamente,
**Próximos eventos** y la barra inferior con **Inicio, Pedidos, Carrito,
Productos y Chat**.

### 5.1 Explorar el menú

1. Usa el **buscador** para encontrar un producto por nombre.
2. Usa los **filtros por categoría** para ver sólo bebidas, postres, etc.
3. Toca un producto para ver su **detalle** (nombre, descripción, precio,
   imagen) y su disponibilidad.

### 5.2 Carrito y compra

1. Añade productos al carrito con **Comprar** o desde el detalle.
2. Abre el **Carrito** (barra inferior) para revisar el contenido.
3. Ajusta cantidades con **+ / −** o elimina un ítem.
4. Pulsa **Proceder al pago** y elige el método de pago.
5. Confirma el pedido. Verás el pedido creado en **Mis pedidos** con estado
   **Pendiente**.

### 5.3 Métodos de pago

- **Efectivo:** pagas al recoger; no requiere nada más.
- **Transferencia (método virtual):** debes **adjuntar el comprobante**
  (foto o captura de la transferencia) antes de confirmar. Sin comprobante el
  pedido no se acepta.

### 5.4 Mis pedidos

- Abre **Pedidos** en la barra inferior.
- Filtra por estado: **Todas, Pendiente, Pagado, En preparación, Entregado**.
- Toca un pedido para ver el **detalle**: productos, pagos, comprobante y total.
- El estado avanza a medida que el barista lo prepara:
  `Pendiente → Pagado → En preparación → Entregado`.

### 5.5 Eventos

- En el inicio, la sección **Próximos eventos** muestra los eventos publicados
  por la cafetería.
- Toca una tarjeta para ver el detalle: imagen, fecha y hora, lugar y
  descripción.

### 5.6 Chat

- Abre **Chat** en la barra inferior.
- Elige un **contacto** y escribe tu mensaje.
- El hilo se actualiza automáticamente cada pocos segundos.
- Los mensajes que recibes se marcan como **leídos** al abrir la conversación.

### 5.7 Perfil y modo oscuro

- Desde tu perfil puedes **editar tus datos** y **cambiar tu foto**.
- Activa el **modo oscuro** con el interruptor del perfil; la preferencia queda
  guardada para los próximos usos.

---

## 6. Uso como Barista

Al iniciar sesión como barista, la app abre el **Panel de pedidos**.

### 6.1 Panel de pedidos (Kanban)

Los pedidos se organizan en tres columnas:

- **Por atender:** pendientes y pagados.
- **En preparación:** en proceso.
- **Terminados:** entregados o cancelados.

- Pulsa **Preparando** en una tarjeta para moverla a "En preparación".
- Pulsa **Entregado** para finalizar el pedido.
- El tablero se **actualiza solo** cada 8 segundos.

Accesos rápidos del panel: **Inventario**, **Reportar inventario**, **Chat** y
**Caja de cobro**.

### 6.2 Turno

Para poder operar (vender, registrar gastos, etc.) necesitas un **turno activo**
y una **caja abierta**:

- **Sin turno:** la app te indica que no tienes turno asignado; pide al
  administrador que te asigne uno.
- **Con turno pero sin caja:** puedes (y debes) **abrir caja** para comenzar.
- **Con turno y caja:** puedes operar con normalidad.

El estado de tu turno se refresca automáticamente.

### 6.3 Caja

- **Abrir caja:** introduce el **saldo inicial** (el efectivo que tienes al
  empezar).
- **Cerrar caja:** introduce el **saldo final** (el efectivo contado al
  terminar).
- La pantalla muestra quién abrió la caja, fechas, ingresos, egresos y el
  **saldo actual**, además del **historial de cajas**.

### 6.4 Punto de venta (POS)

- Abre **Vender** desde tu inicio.
- Filtra por categoría o busca productos y tócalos para **añadirlos al carrito**.
- Revisa el carrito y pulsa **Cobrar**.
- Elige el método de pago (si es virtual, adjunta el comprobante) y confirma.
- El carrito se limpia y el pedido aparece en el panel.

### 6.5 Reportar inventario

- Desde el panel, pulsa **Reportar inventario**.
- Registra el insumo, la cantidad y el motivo de la pérdida/consumo.
- El reporte queda visible para el administrador en **Mermas**.

---

## 7. Uso como Administrador

Al iniciar sesión, la app abre el **Panel de Administración**.

### 7.1 Panel y métricas

El panel muestra indicadores y accesos a todos los módulos:

- **Ingresos de hoy**, **pedidos de hoy**, **clientes registrados** y
  **ticket promedio**.
- Gráfica del **balance de los últimos 7 días**.
- **Productos más vendidos** (top 5).

### 7.2 Usuarios

- Ver la lista de usuarios con sus roles.
- **Crear, editar y eliminar** usuarios.
- Asignar roles con las casillas: `admin`, `barista`, `cliente`.
- La tarjeta **Agregar Barista** crea un usuario con el rol ya seleccionado.

### 7.3 Categorías y productos

- **Categorías:** crear y eliminar.
- **Productos:** crear y editar (nombre, descripción, precio, categoría e
  imagen).
- **Recetas:** asociar **insumos** a un producto y quitarlos cuando ya no
  apliquen.

### 7.4 Métodos de pago

- Crear, editar y eliminar métodos de pago.
- Marca **virtual** para los que requieren comprobante (por ejemplo,
  transferencia). Los virtuales aparecen marcados como "(virtual)" al cobrar.

### 7.5 Promociones

- Crea promociones con **nombre**, **descripción**, **tipo** (Descuento, 2x1,
  Combo, Personalizado), **color**, **imagen** y **fecha de vencimiento**
  (opcional).
- Elige **qué productos incluye** la promoción (si no eliges ninguno, se usan
  los destacados del menú: café, pan y postres).
- Define el **descuento** (0-100 %) que se aplica a esos productos cuando el
  cliente compra la promoción en el carrito.
- Actívalas o desactívalas con el interruptor.
- Las promociones activas y no vencidas se muestran rotando en una pantalla
  emergente al entrar al Inicio y al Carrito de los clientes.

> Las promociones se guardan **en cada dispositivo**; no se sincronizan entre
> teléfonos.

### 7.6 Eventos

- **Crear/editar/eliminar** eventos con portada, fecha y hora, lugar y
  descripción.
- Cambia entre **Publicado** y **Borrador** directamente en la lista.
- Sólo los **publicados** se muestran a los clientes en "Próximos eventos".

### 7.7 Inventario, compras y gastos

- **Insumos:** ver stock actual y mínimo; gestionar insumos.
- **Mermas:** registro de pérdidas y consumos (incluye los reportados por
  baristas).
- **Compras:** registrar compras a proveedores (factura, fecha, total y pagos).
- **Gastos:** registrar gastos generales.
- **Proveedores:** base de datos de proveedores (contacto, NIT, convenio).

### 7.8 Turnos

- Asignar **turnos** a los baristas (fecha, tipo de turno y baristas
  asignados). Sin turno, el barista no puede operar.

### 7.9 Reportes

Genera reportes estadísticos y de inventario:

| Reporte | Contenido |
|---------|-----------|
| **Diario** | Ventas y pedidos de hoy; ticket promedio |
| **Semanal** | Últimos 7 días, ventas por día |
| **Mensual** | Mes en curso, ventas por día |
| **Inventario** | Insumos, stock actual/mínimo y estado (`BAJO STOCK` / `Normal`) |

Los reportes incluyen productos más vendidos y se generan en **PDF**:

- **Abrir**, **compartir** o **descargar** (a la carpeta Descargas).
- Los reportes quedan **archivados** en la sección de reportes archivados para
  reabrirlos sin recalcular.
- Sólo un administrador puede **eliminar** reportes archivados.

---

## 8. El asistente KAFFA

El **asistente** es un botón flotante que aparece en toda la app cuando inicias
sesión. Al pulsarlo se abre un chat con respuestas inteligentes.

- **Cliente:** te ayuda con productos, cómo hacer un pedido y eventos.
- **Barista:** te muestra ventas de hoy, pedidos pendientes e insumos.
- **Administrador:** te da ventas de la semana, predicciones e insumos por
  reponer.

El asistente mantiene el historial de tu conversación y lo reinicia al cambiar
de usuario. También puedes **vaciar la conversación** desde su propia ventana.

---

## 9. Solución de problemas

| Problema | Qué hacer |
|----------|-----------|
| "Error de conexión" en todas las pantallas | Verifica que el backend esté encendido y que el teléfono esté en la misma Wi-Fi |
| No puedo iniciar sesión con mi cuenta nueva | Tu correo no está verificado: usa **Reenviar correo de verificación** |
| El enlace de recuperación no funciona | Puede haber expirado: solicita uno nuevo desde la app |
| Mi transferencia fue rechazada | Adjunta el **comprobante** antes de confirmar |
| No hay productos en el POS | El administrador debe crear productos y métodos de pago |
| "No tienes turno activo" (barista) | Pide al administrador que te asigne un turno |
| "Abre la caja" (barista) | Ve a **Caja** y registra el saldo inicial para operar |
| La promoción mostrada no cambia | Deben existir varias promociones activas y no vencidas |
| No aparece la pantalla de promociones | Ninguna promoción está activa o ya venció; el administrador debe activarla |
| No veo eventos | El administrador no ha publicado eventos |

---

## 10. Preguntas frecuentes (FAQ)

**¿Necesito internet?**
Sí. La app consume un servidor Kaffa; sin conexión a la red del backend no
funcionará.

**¿Puedo tener varias cuentas?**
Sí, cierras sesión y entras con otra. El asistente reinicia su historial al
cambiar de usuario.

**¿La app guarda mis datos de tarjeta?**
No. Sólo registra pagos en efectivo o transferencias con comprobante.

**¿Dónde quedan los reportes PDF?**
Se archivan dentro de la app y puedes descargarlos a la carpeta **Descargas**
del teléfono.

**¿Las promociones son iguales en todos los teléfonos?**
No. Cada dispositivo guarda su copia local de las promociones, por lo que la
rotación puede variar entre teléfonos.

**¿Cómo cierro sesión?**
Desde tu perfil, con la opción **Cerrar sesión**. Si tu sesión expira, la app
te devuelve automáticamente al inicio de sesión.

---

## 11. Privacidad y datos

- Las contraseñas **viajan y se almacenan de forma segura** en el servidor; la
  app sólo guarda un **token de sesión** para mantenerte conectado.
- Las imágenes que adjuntas (comprobantes, fotos de perfil, eventos) se suben
  al servidor para su uso en el negocio.
- El **modo oscuro** y las **promociones locales** se guardan en el propio
  dispositivo.

---

## 12. Glosario

| Término | Significado |
|---------|-------------|
| **Carrito** | Lista de productos que estás por comprar |
| **Comprobante** | Imagen que demuestra una transferencia |
| **Kanban** | Tablero con columnas por estado del pedido |
| **Merma** | Pérdida o consumo de insumos |
| **POS** | Punto de venta (pantalla de cobro del barista) |
| **Promoción** | Oferta que aparece en una pantalla emergente al entrar al Inicio o al Carrito |
| **Ticket promedio** | Valor medio de cada pedido |
| **Turno** | Jornada asignada a un barista |
| **Método virtual** | Pago que exige comprobante (transferencia) |

---

*Fin del manual de usuario.*
