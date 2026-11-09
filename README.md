# MongoQuestGrupo5

Aplicación de escritorio para la gestión de un gimnasio, desarrollada en Java
con JavaFX y MongoDB. El nombre que aparece en la interfaz es **Tolotolo**.

Este documento describe el estado actual del código y de los recursos del
proyecto. Las funcionalidades descritas como pendientes no están terminadas en
la versión actual.

## Tecnologías

- Java 11.
- Maven.
- JavaFX 13 (`javafx-controls` y `javafx-fxml`).
- MongoDB Java Sync Driver 5.2.0.
- MongoDB local, por defecto en `mongodb://localhost:27017`.

## Ejecutar

1. Instalar un JDK compatible con Java 11 y Maven.
2. Iniciar MongoDB local en el puerto `27017`.
3. Desde el directorio `MongoQuest`, ejecutar:

   ```text
   mvn clean javafx:run
   ```

   Tambien se puede iniciar con `iniciar.bat` desde la raiz del proyecto. El
   script localiza `MongoQuest` de forma relativa a su propia ubicacion.

La clase de entrada es `com.mycompany.controlador.App`. La aplicación abre la
pantalla de inicio de sesión.

## Estructura del proyecto

```text
MongoQuest/
├── pom.xml
└── src/main/
    ├── java/
    │   ├── com/mycompany/controlador/   # Aplicación y controladores JavaFX
    │   ├── com/mycompany/controlador/dao/ # Contrato y acceso a MongoDB
    │   └── modelo/                       # Modelos del dominio
    └── resources/
        ├── com/mycompany/vista/          # Pantallas FXML
        └── com/mycompany/mongoquest/imagesApp/ # Imágenes de la interfaz
```

### Capas principales

- **Interfaz:** las vistas FXML se encuentran en `src/main/resources/com/mycompany/vista`.
- **Controladores:** `App` inicia la aplicación; `LoginController` autentica y
  enruta por rol; `GerenteController` gestiona la navegación del panel del
  gerente; `GestionClientesController` y `GestionMonitorController` contienen
  las pantallas de administración de clientes y monitores.
- **Persistencia:** la interfaz `dao` define operaciones de autenticación y
  gestión. `daoImplementacio` implementa esas operaciones usando MongoDB y
  `org.bson.Document`.
- **Modelos:** `cliente`, `Trabajadores`, `Clases`, `Salas`, `Sesiones`,
  `Direccion`, `Horas`, y los enumerados `Membresia` y `Tipo`.

## Funcionalidad disponible

### Inicio de sesión

La pantalla `Login.fxml` valida que se introduzcan usuario y contraseña,
consulta MongoDB y abre una pantalla según el rol devuelto:

- `gerente` → panel `Gerente.fxml`.
- `monitor` → pantalla `Monitor.fxml`.
- `cliente` → pantalla `Cliente.fxml`.

Si las credenciales no coinciden, se muestra un aviso. Si MongoDB no está
disponible, la aplicación informa de que no puede conectarse.

### Panel del gerente

El panel permite navegar a la gestión de monitores y clientes y volver al inicio
de sesión. También presenta una opción para gestionar clases; actualmente su
controlador apunta a `GestorClases.fxml`, pero ese recurso no está presente en
el proyecto.

### Gestión de clientes

La pantalla carga la colección `Clientes` y permite:

- Consultar y seleccionar clientes.
- Dar de alta un cliente con DNI, nombre, apellidos, correo, contraseña y tipo
  de membresía (`normal` o `premium`).
- Editar nombre, apellidos, DNI, correo y membresía.
- Dar de baja un cliente con confirmación.
- Volver al panel del gerente o cerrar sesión.

Se comprueba que los campos obligatorios estén completos y que el correo tenga
un formato básico. Los errores de conexión y las claves duplicadas se muestran
con avisos.

### Gestión de monitores

La pantalla lista trabajadores cuyo campo `tipo` es `monitor` y permite:

- Consultar y seleccionar monitores.
- Dar de alta un monitor con datos personales, contraseña y dirección.
- Editar nombre, apellidos, DNI, correo y dirección.
- Dar de baja un monitor con confirmación.
- Volver al panel del gerente o cerrar sesión.

La dirección se presenta y edita como calle, código postal y ciudad; en MongoDB
se guarda dentro de un arreglo `direccion`.

## Base de datos

La implementación usa la base de datos `Tolotolo` y no configura la URI desde
un archivo externo: se conecta a `mongodb://localhost:27017`.

### Colección `Trabajadores`

Los controladores y el DAO utilizan documentos con campos como:

| Campo | Uso |
|---|---|
| `nombre`, `apellido`, `dni` | Datos personales |
| `mail`, `contraseña` | Credenciales; el acceso compara ambos campos |
| `tipo` | Rol del trabajador; la aplicación espera `gerente` o `monitor` en minúsculas |
| `fechaAlta` | Fecha de alta, almacenada como fecha BSON |
| `direccion` | Arreglo de documentos con `calle`, `cp` y `ciudad` |
| `clases` | Arreglo inicializado al dar de alta un monitor |

### Colección `Clientes`

Los documentos de cliente utilizan:

| Campo | Uso |
|---|---|
| `nombre`, `apellido`, `dni` | Datos personales |
| `mail`, `contraseña` | Credenciales; el acceso compara ambos campos |
| `membresia` | Tipo de membresía, actualmente `normal` o `premium` |
| `fechaAlta` | Fecha de alta, almacenada como fecha BSON |
| `logros`, `clases` | Arreglos inicializados al dar de alta un cliente |

El login busca primero en `Trabajadores` y después en `Clientes`. La contraseña
se guarda y compara como texto sin cifrar en el código actual. No se definen en
el repositorio datos de ejemplo, índices, migraciones ni inicialización
automática de la base de datos.

> **Importante:** una descripción anterior de este README mencionaba los campos
> `usuario` y `password`; no coincide con la implementación actual. El código
> consulta `mail` y `contraseña`.

## Modelos del dominio

- `cliente`: datos de cliente, fecha de alta, membresía y clases asociadas.
- `Trabajadores`: datos del trabajador, tipo y clases asociadas.
- `Tipo`: roles del modelo (`GERENTE`, `MONITOR`).
- `Membresia`: valores `NORMAL` y `PREMIUM`.
- `Clases`: identificador, nombre, horarios y sala.
- `Horas`: día y horas de inicio y fin.
- `Salas`: identificador, nombre y aforo máximo.
- `Sesiones`: clase, fecha, monitor, aforo y asistentes; incluye operaciones
  para comprobar plazas, apuntar y desapuntar clientes.
- `Direccion`: calle, código postal y ciudad.

Los controladores de administración trabajan directamente con documentos BSON;
los modelos no están conectados a todas las operaciones de persistencia.

## Estado actual y trabajo pendiente

- La gestión de clientes y monitores está conectada al DAO de MongoDB.
- `ClienteController` y `MonitorController` son actualmente controladores
  vacíos; sus vistas no implementan todavía una experiencia funcional para cada
  rol.
- La opción de gestión de clases no puede abrirse porque falta
  `GestorClases.fxml` (y no hay controlador de clases en el código actual).
- La conexión a MongoDB está fijada a localhost; no hay configuración de
  entorno para cambiar servidor o credenciales.
- No se encontraron pruebas automatizadas en el árbol de fuentes revisado.

## Cambios locales aún no confirmados

En el árbol de trabajo actual hay cambios de recursos que todavía no forman
parte del último commit:

- Las imágenes se han movido de `images` a `imagesApp`.
- Las vistas FXML de login y de gestión/panel del gerente se han actualizado
  para referenciar las nuevas rutas. En el login también se usa la imagen
  `toro_derecha.png` para el toro del lado derecho.

Estas notas describen el estado visible del árbol de trabajo; no implican que
esos cambios estén confirmados en Git.
