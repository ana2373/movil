# Manual de Usuario Completo - Sistema Kaffa

**Versión:** 1.0  
**Fecha:** 2026-10-28  
**Sistemas involucrados:** Backend Laravel, Frontend React y App Móvil Android (Jetpack Compose)

---

## Tabla de Contenido

- [1. Introducción](#1-introducción)
- [2. Objetivo del Manual](#2-objetivo-del-manual)
- [3. Acceso al Sistema](#3-acceso-al-sistema)
- [4. Rol Cliente (App Móvil / Web)](#4-rol-cliente-app-móvil--web)
- [5. Rol Barista (App Móvil)](#5-rol-barista-app-móvil)
- [6. Rol Administrador (Web / App Móvil)](#6-rol-administrador-web--app-móvil)
- [7. Solución de Problemas](#7-solución-de-problemas)
- [8. Glosario](#8-glosario)

---

## 1. Introducción

**Kaffa** es un sistema integral para la gestión de una cafetería virtual que permite automatizar todo el proceso de venta: desde la consulta del menú por parte del cliente, hasta la atención de pedidos por el barista y el control administrativo del negocio.

El sistema está conformado por tres componentes que trabajan en conjunto:
- **App Móvil Android (Jetpack Compose):** Orientada a Clientes y Baristas para realizar pedidos y atenderlos desde el establecimiento.
- **Frontend Web (React):** Interfaz administrativa.
- **Backend (Laravel):** API central que gestiona usuarios, productos, pedidos, inventario, reportes y autenticación.

---

## 2. Objetivo del Manual

El presente manual tiene como objetivo guiar al usuario en el correcto uso del sistema Kaffa, detallando paso a paso las funcionalidades correspondientes a cada uno de los roles: **Cliente**, **Barista** y **Administrador**.

---

## 3. Acceso al Sistema

Para ingresar al sistema, es necesario contar con una cuenta válida asignada a uno de los tres roles.

### 3.1 Pantalla de Inicio de Sesión

![Pantalla de Inicio de Sesión](./docs/Imagenes_manual/Captura%20desde%202026-10-20%2020-51-56.png)

**Pasos para iniciar sesión:**
1. Ingrese su **correo electrónico** registrado.
2. Ingrese su **contraseña**.
3. Presione **"Iniciar sesión"**.
4. El sistema redirigirá automáticamente al panel correspondiente según el rol del usuario.

### 3.2 Registro de Clientes

![Pantalla de Registro de Cliente](./docs/Imagenes_manual/Captura%20desde%202026-10-20%2020-52-05.png)

**Pasos para registrarse:**
1. Desde la pantalla de inicio de sesión, presionar **"Registrarse"**.
2. Completar los campos: **Nombre completo**, **Correo electrónico** y **Contraseña**.
3. Presionar **"Crear cuenta"**.
4. Revisar el correo electrónico para **verificar la cuenta** mediante el enlace enviado por Kaffa.
5. Una vez verificada, podrá iniciar sesión normalmente.

> **Nota:** La contraseña debe tener al menos 8 caracteres, combinando mayúsculas, minúsculas, números y símbolos.

---

## 4. Rol Cliente (App Móvil / Web)

El rol **Cliente** permite explorar el menú, realizar pedidos, dar seguimiento a los mismos y aprovechar promociones disponibles en Kaffa.

### 4.1 Pantalla Principal

![Pantalla Principal Cliente](./docs/Imagenes_manual/Captura%20desde%202026-10-20%2020-52-47.png)

#### 4.1.1 Barra de Búsqueda
![Barra de Búsqueda de Productos](./docs/Imagenes_manual/Captura%20desde%202026-10-20%2020-52-55.png)

#### 4.1.2 Categorías (Repostería / Bebidas)
![Categorías de Productos](./docs/Imagenes_manual/Captura%20desde%202026-10-20%2020-53-10.png)

**Uso:**
1. Deslice horizontalmente para visualizar todas las categorías disponibles.
2. Seleccione una categoría (ej. **Bebidas**, **Repostería**) para filtrar los productos.
3. Seleccione **"Todos"** para regresar a la vista completa del menú.

#### 4.1.3 Promociones y Productos Destacados
![Productos Destacados y Promociones](./docs/Imagenes_manual/Captura%20desde%202026-10-20%2020-53-17.png)

### 4.2 Selección de Productos
![Detalle del Producto](./docs/Imagenes_manual/Captura%20desde%202026-10-20%2020-53-23.png)

### 4.3 Proceso de Compra / Pedido

#### Paso 1: Agregar productos al carrito
![Agregar Producto al Carrito](./docs/Imagenes_manual/Captura%20desde%202026-10-20%2020-53-44.png)

#### Paso 2: Revisar el carrito de compras
![Carrito de Compras](./docs/Imagenes_manual/Captura%20desde%202026-10-20%2020-53-53.png)

#### Paso 3: Finalizar el pedido
![Finalización del Pedido](./docs/Imagenes_manual/Captura%20desde%202026-10-20%2020-54-05.png)

#### Paso 4: Seguimiento del pedido
![Seguimiento de Pedidos del Cliente](./docs/Imagenes_manual/Captura%20desde%202026-10-20%2020-54-15.png)

| Estado | Descripción |
| :--- | :--- |
| **Recibido** | El pedido ha sido registrado exitosamente en el sistema. |
| **En preparación** | El barista ha tomado el pedido y se encuentra en proceso de elaboración. |
| **Listo para entregar / Recoger** | El pedido está listo para ser entregado al cliente. |
| **Entregado** | El pedido ha sido entregado correctamente. |

---

## 5. Rol Barista (App Móvil)

### 5.1 Acceso al Panel del Barista
![Panel del Barista - Vista de Comandas](./docs/Imagenes_manual/Captura%20desde%202026-10-20%2021-00-02.png)

### 5.2 Gestión de Pedidos (Kanban)
![Gestión de Pedidos por Estado](./docs/Imagenes_manual/Captura%20desde%202026-10-20%2021-00-23.png)

| Columna | Acción del Barista | Resultado |
| :--- | :--- | :--- |
| **Recibidos** | Visualizar nuevas comandas entrantes. | Pedido pendiente por tomar. |
| **En preparación** | Presionar "Pasar a En preparación". | Pedido en proceso de elaboración. |
| **Listo** | Presionar "Marcar como Listo". | Pedido listo para entregar. |
| **Entregados** | Histórico de pedidos entregados. | Pedidos finalizados. |

### 5.3 Detalles de la Comanda
![Detalle de Comanda del Barista](./docs/Imagenes_manual/Captura%20desde%202026-10-20%2021-00-34.png)

### 5.4 Turno, Caja y Punto de Venta (POS)
![Gestión de Turno del Barista](./docs/Imagenes_manual/Captura%20desde%202026-10-20%2021-00-50.png)

#### 5.4.1 Caja
![Apertura de Caja del Barista](./docs/Imagenes_manual/Captura%20desde%202026-10-20%2021-00-55.png)

#### 5.4.2 Punto de Venta (POS)
![Punto de Venta (POS) del Barista](./docs/Imagenes_manual/Captura%20desde%202026-10-20%2021-02-04.png)

#### 5.5 Reportar Inventario
![Reportar Inventario / Mermas](./docs/Imagenes_manual/Captura%20desde%202026-10-20%2021-02-08.png)

---

## 6. Rol Administrador (Web / App Móvil)

### 6.1 Panel de Administración
![Panel de Administración - Dashboard](./docs/Imagenes_manual/Captura%20desde%202026-10-20%2021-02-13.png)

### 6.2 Gestión de Usuarios y Roles
![Gestión de Usuarios y Roles](./docs/Imagenes_manual/Captura%20desde%202026-10-20%2021-02-20.png)

### 6.3 Gestión del Catálogo: Productos y Categorías
![Gestión de Productos y Categorías](./docs/Imagenes_manual/Captura%20desde%202026-10-20%2021-02-29.png)
![Editar y Crear Productos](./docs/Imagenes_manual/Captura%20desde%202026-10-20%2021-02-38.png)

### 6.4 Gestión de Promociones
![Gestión de Promociones](./docs/Imagenes_manual/Captura%20desde%202026-10-20%2021-02-08.png)

### 6.5 Gestión de Inventario, Compras y Gastos
![Gestion de Inventario y Compras](./docs/Screenshot_20261027_154158.png)

### 6.6 Gestión de Turnos
![Gestión de Turnos](./docs/Screenshot_20261027_154208.png)

### 6.7 Reportes y Estadísticas
![Reportes y Estadísticas del Administrador](./docs/Screenshot_20261027_154313.png)

| Reporte | Propósito |
| :--- | :--- |
| **Ventas diarias/semanales/mensuales** | Total de ingresos, número de pedidos y ticket promedio. |
| **Productos más vendidos** | Identificar los productos más solicitados. |
| **Ventas por barista** | Evaluar el desempeño por empleado. |
| **Inventario y mermas** | Control de stock y pérdidas. |
| **Gastos y compras** | Análisis de costos y rentabilidad. |

---

## 7. Solución de Problemas

| Problema | Causa Posible | Solución |
| :--- | :--- | :--- |
| No puedo iniciar sesión | Credenciales incorrectas o cuenta sin verificar | Verificar datos o correo de verificación. Usar "¿Olvidaste tu contraseña?" si es necesario. |
| Error de conexión | Sin conexión o backend apagado | Verificar conexión Wi-Fi y que el servidor esté activo. |
| No veo pedidos (Barista) | Sin turno activo | Solicitar al Administrador asignación de turno. |
| No puedo abrir caja (Barista) | Caja sin abrir | Ir a "Caja" y registrar el saldo inicial. |
| No veo promociones (Cliente) | Promoción inactiva o vencida | Consultar con el Administrador o esperar a nuevas promociones vigentes. |
| No puedo usar POS (Barista) | Sin turno activo o caja sin abrir | Asegurar turno activo y caja abierta. |

---

## 8. Glosario

| Término | Definición |
| :--- | :--- |
| **Comanda** | Pedido enviado al barista para su preparación. |
| **Kanban** | Tablero visual para gestionar pedidos por estado. |
| **POS** | Punto de Venta para cobro directo en establecimiento. |
| **Turno** | Jornada laboral asignada a un barista. |
| **Caja** | Control de efectivo, apertura/cierre y movimientos. |
| **Mermas** | Pérdidas de insumos (vencimiento, rotura, consumo interno). |
| **Promoción** | Oferta o paquete con precio especial. |
| **Extras / Personalizaciones** | Opciones adicionales al producto. |
| **Dashboard** | Panel con métricas clave del negocio. |
| **Roles** | Permisos asignados (Cliente, Barista, Administrador). |
| **Laravel** | Framework PHP del backend. |
| **React** | Framework de frontend web. |
| **Jetpack Compose** | Toolkit moderno para UI en Android. |
| **API** | Interfaz de comunicación entre componentes del sistema. |
