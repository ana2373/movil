# Manual de Usuario - Aplicación Móvil Kaffa

## 1. Introducción

**Kaffa** es la aplicación móvil oficial de la cafetería Kaffa, desarrollada para Android con Kotlin y Jetpack Compose. 

Esta aplicación ha sido diseñada pensando en la comodidad de nuestros clientes, permitiéndoles explorar el menú de la cafetería, realizar pedidos desde su dispositivo móvil, consultar sus pedidos en tiempo real y disfrutar de promociones exclusivas, todo desde la comodidad de su celular.

Con Kaffa podrás:
- Consultar el menú completo de la cafetería
- Realizar pedidos y pagarlos directamente desde la app
- Dar seguimiento a tus pedidos en tiempo real
- Aprovechar promociones y eventos especiales
- Mantener tu historial de compras

![Logo de Kaffa](./docs/Imagenes_manual/Captura%20desde%202026-10-20%2017-49-31.png)

---

## 2. Pantalla de Inicio de Sesión (Login)

Para acceder a la aplicación Kaffa, es necesario iniciar sesión con una cuenta registrada. 

### 2.1 Acceder a la pantalla de login

Al abrir la aplicación, se mostrará la pantalla de bienvenida con el logo de Kaffa. Tras unos segundos, o al presionar "Entrar", accederás a la pantalla de inicio de sesión.

![Pantalla de Inicio de Sesión](./docs/Imagenes_manual/Captura%20desde%202026-10-20%2020-51-56.png)

### 2.2 Iniciar sesión

Sigue estos pasos para acceder a tu cuenta:

1. **Ingresa tu correo electrónico** - Escribe la dirección de correo con la que te registraste.
2. **Ingresa tu contraseña** - Escribe tu contraseña de acceso.
3. **Presiona "Iniciar sesión"** - Al hacerlo, serás redirigido a la pantalla principal de la aplicación.

### 2.3 Crear una cuenta nueva

Si aún no tienes una cuenta, puedes registrarte directamente desde la pantalla de login:

1. Presiona **"Registrarse"** en la parte inferior de la pantalla de inicio de sesión.
2. Completa los campos solicitados: nombre completo, correo electrónico y contraseña.
3. Presiona **"Crear cuenta"**.
4. Revisa tu bandeja de entrada de correo electrónico. Kaffa te enviará un enlace de verificación para activar tu cuenta.
5. Abre el enlace recibido y vuelve a iniciar sesión en la aplicación.

> **Nota:** La contraseña debe contener al menos 8 caracteres, incluyendo mayúsculas, minúsculas, números y un símbolo.

![Pantalla de Registro](./docs/Imagenes_manual/Captura%20desde%202026-10-20%2020-52-05.png)

---

## 3. Pantalla Principal (Home / Dashboard)

Una vez que hayas iniciado sesión correctamente, accederás a la pantalla principal de Kaffa. Desde aquí podrás explorar todo el menú de la cafetería de manera rápida y sencilla.

![Pantalla Principal](./docs/Imagenes_manual/Captura%20desde%202026-10-20%2020-52-47.png)

### 3.1 Barra de búsqueda

En la parte superior de la pantalla principal encontrarás la **barra de búsqueda**. 

- **Buscar productos**: Escribe el nombre del producto que deseas encontrar (ej. "Café", "Capuchino", "Té").
- **Resultados instantáneos**: La aplicación filtrará automáticamente los productos que coincidan con tu búsqueda.
- **Borrar búsqueda**: Puedes limpiar el campo para volver a ver todas las categorías y productos.

![Barra de Búsqueda](./docs/Imagenes_manual/Captura%20desde%202026-10-20%2020-52-55.png)

### 3.2 Categorías

Debajo de la barra de búsqueda encontrarás las **categorías** de productos disponibles:

- Desliza horizontalmente para ver todas las categorías.
- Presiona sobre cualquier categoría para filtrar el menú y mostrar únicamente los productos de esa categoría.
- Selecciona la categoría **"Todos"** para volver a ver el menú completo.

![Categorías de Productos](./docs/Imagenes_manual/Captura%20desde%202026-10-20%2020-53-10.png)

### 3.3 Productos destacados

En la parte inferior de la pantalla principal, o en el área principal, encontrarás la sección de **Productos Destacados**.

- **Vista previa**: Cada producto muestra su imagen, nombre, precio y una breve descripción.
- **Seleccionar producto**: Presiona sobre cualquier producto para ver sus detalles, personalizarlo y agregarlo al carrito.
- **Promociones**: En ocasiones, las promociones vigentes aparecerán en forma de tarjeta emergente al ingresar al inicio. Puedes consultarlas presionando el icono de etiqueta en la cabecera.

![Productos Destacados](./docs/Imagenes_manual/Captura%20desde%202026-10-20%2020-53-17.png)

---

## 4. Proceso de Compra

Realizar un pedido en Kaffa es rápido, intuitivo y seguro. Sigue estos pasos para completar tu compra correctamente.

### 4.1 Seleccionar un producto

1. Desde la pantalla principal, busca el producto que deseas ordenar usando la barra de búsqueda o navegando por categorías.
2. Presiona sobre el producto para abrir la **pantalla de detalles del producto**.
3. Ahí podrás ver la descripción completa, precio, imagen y opciones de personalización (si aplica).

![Detalle del Producto](./docs/Imagenes_manual/Captura%20desde%202026-10-20%2020-53-23.png)

### 4.2 Personalizar y agregar al carrito

Dependiendo del producto, podrás personalizarlo antes de agregarlo al carrito:

1. **Selecciona las opciones** - Algunos productos permiten agregar extras, elegir tamaño, tipo de leche u otras opciones.
2. **Indica la cantidad** - Usa los botones + / - para ajustar la cantidad que deseas pedir.
3. **Agrega al carrito** - Presiona el botón **"Agregar al carrito"** para añadir el producto con todas sus personalizaciones.

![Agregar al Carrito](./docs/Imagenes_manual/Captura%20desde%202026-10-20%2020-53-44.png)

### 4.3 Revisar el carrito

Una vez agregados tus productos, puedes revisar tu pedido:

1. **Acceder al carrito** - Presiona el icono del carrito en la barra de navegación o en la cabecera.
2. **Ver tu pedido** - Aquí encontrarás todos los productos agregados, sus cantidades, personalizaciones, precio unitario y total.
3. **Modificar pedido** - Puedes aumentar/disminuir cantidades o eliminar productos presionando el ícono de eliminar.
4. **Aplicar promociones** - Si hay una promoción disponible, podrás aplicarla desde esta pantalla.

![Carrito de Compras](./docs/Imagenes_manual/Captura%20desde%202026-10-20%2020-53-53.png)

### 4.4 Seleccionar método de pago y finalizar compra

1. **Elegir método de pago** - En la pantalla del carrito, selecciona tu método de pago preferido (efectivo, tarjeta u otros métodos disponibles).
2. **Revisar total** - Verifica el total a pagar, incluyendo descuentos si aplica alguna promoción.
3. **Confirmar pedido** - Presiona **"Realizar pedido"** o **"Cobrar"** para finalizar la compra.
4. **Confirmación** - Al completar el pedido exitosamente, recibirás una confirmación y podrás dar seguimiento a tu pedido desde la sección **"Mis Pedidos"**.

![Finalizar Compra](./docs/Imagenes_manual/Captura%20desde%202026-10-20%2020-54-05.png)

### 4.5 Seguimiento del pedido

Desde el menú principal o desde tu perfil, puedes acceder a **"Mis Pedidos"** para:

- Ver el estado de tus pedidos en tiempo real (Recibido, En preparación, Listo para recoger, Entregado)
- Consultar el detalle de cada pedido
- Revisar tu historial de compras

![Mis Pedidos](./docs/Imagenes_manual/Captura%20desde%202026-10-20%2020-54-15.png)

---

## Consejos Útiles

- **Mantén tu sesión activa** - No es necesario iniciar sesión cada vez que uses la app.
- **Revisa las promociones** - Siempre verifica las promociones vigentes al entrar, ya que pueden tener descuentos importantes.
- **Verifica tu correo** - Si acabas de registrarte, recuerda verificar tu correo antes de intentar realizar un pedido.
- **Usa el buscador** - Si conoces el nombre del producto, el buscador te ayudará a encontrarlo más rápido.

**¡Disfruta de tu experiencia en Kaffa!**