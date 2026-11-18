# MongoQuest (Tolotolo Gym) — Diseño de la interfaz: aspecto y comportamiento

Documento de diseño de la UI para la rúbrica DIN4. Todo lo que aparece aquí está sacado del código fuente actual:

- Vistas: `src/main/resources/com/mycompany/vista/*.fxml`
- Controladores: `src/main/java/com/mycompany/controlador/*Controller.java`, `App`, `Registro`, `GestorImagenes`
- Capa de datos: `src/main/java/com/mycompany/controlador/dao/` (interfaz `dao`, implementación `daoImplementacio`, excepciones `DatosException`, `DuplicadoException`, `ValidacionException`, `ConexionException`)

Convenciones:

- `dao` es siempre una instancia de `daoImplementacio` creada en el propio controlador (`new daoImplementacio()`); la conexión a MongoDB es única y compartida (ver «Tratamientos comunes»).
- Las excepciones de datos son no comprobadas: `DatosException extends RuntimeException`; `DuplicadoException`, `ConexionException` y `ValidacionException` extienden `DatosException`. Por eso los `catch` van de la más concreta a la general.
- `alerta(tipo, texto)` / `mostrarAlerta(tipo, texto)` / `aviso(texto)`: métodos privados de cada controlador que crean un `javafx.scene.control.Alert`, le ponen el icono con `App.ponerIcono(Dialog)`, quitan la cabecera (`setHeaderText(null)`) y lo muestran con `showAndWait()`. Cuando el código crea el `Alert` directamente (`new Alert(...).showAndWait()`) se indica, porque entonces no lleva icono.
- Navegación: «ventana nueva» = `FXMLLoader.load(URL)` + `new Stage()` + `setScene(new Scene(root))` + `show()` y cierre de la ventana actual con `((Stage) nodo.getScene().getWindow()).close()`. «Misma ventana» = `Scene.setRoot(root)`.
- Solo se documentan los tratamientos que programa el código; no se describen las respuestas por defecto de los controles.

---

## 1. Login

| | |
|---|---|
| FXML | `Login.fxml` |
| Controlador | `LoginController` |
| La abre | `App.start(Stage)` al arrancar (`new FXMLLoader(App.class.getResource("/com/mycompany/vista/Login.fxml")).load()`, `Scene` de 640×480, título «Tolotolo - Iniciar sesión»); `RegisterController.onVolverLogin()` (misma ventana); los «cerrar sesión» de Gerente, Monitor, Cliente, Gestión de clientes, Gestión de monitores, Gestión de clases y Horario (ventana nueva) |
| Rol | Cualquiera (sin sesión) |

**Aspecto.** `BorderPane` (`raiz`) con el título `lblTitulo : Label` («Tolotolo gym»), dos imágenes decorativas laterales `imgToroIzq`, `imgToroDer : ImageView` y, en el centro, el formulario: `txtUsuario : TextField` (correo), `txtPassword : PasswordField`, `btnLogin : Button` («Iniciar sesión») y `linkRegistro : Hyperlink` («Registrarse»).

**Estado inicial (`initialize()`).** No consulta la base de datos. Enlaza el alto de las dos imágenes al de la ventana: `imgToroIzq.fitHeightProperty().bind(raiz.heightProperty().subtract(81).multiply(0.85))` (igual para `imgToroDer`).

**Estado de sesión que publica.** Dos campos estáticos que leen las demás pantallas: `LoginController.mailActual` (correo del usuario) y `LoginController.rolActual` (`"gerente"`, `"monitor"` o `"cliente"`).

| Control (fx:id : tipo) | Evento (nombre técnico) | Método | Tratamiento |
|---|---|---|---|
| `btnLogin : Button` | `ActionEvent (onAction)` | `onIniciarSesion()` | 1) Lee `txtUsuario` y `txtPassword` con `trim()`. Si alguno está vacío → `Alert WARNING` «Escribe el usuario y la contraseña.» y termina. 2) `dao.validarLogin(mail, contraseña)`: busca primero en `Trabajadores` (devuelve su campo `tipo`) y después en `Clientes` (devuelve `"cliente"`). 3) Si devuelve `null` → `Registro.aviso("Login fallido para " + mail)` y `Alert ERROR` «Usuario o contraseña incorrectos.». 4) Si hay rol: guarda `mailActual` y `rolActual`, `Registro.setUsuario(mail)`, `Registro.info("Login correcto: …")` y abre en ventana nueva, cerrando la de login, `Gerente.fxml` («Tolotolo - Gerente»), `Monitor.fxml` («Tolotolo - Monitor») o `Cliente.fxml` («Tolotolo - Cliente») según el rol (método privado `abrirVentana(fxml, titulo)`, que también llama a `App.ponerIcono(Stage)`). Rol desconocido → `Registro.aviso(...)` y `Alert ERROR` «Tipo de usuario desconocido: <rol>». 5) `catch (DatosException)` (incluye `ConexionException`) → `Registro.error(...)` y `Alert ERROR` «No se puede conectar con MongoDB. Comprueba que esté iniciado.». 6) Si falla la carga del FXML (`IOException | NullPointerException` en `abrirVentana`) → `Registro.error(...)` y `Alert ERROR` «No se pudo abrir la ventana <ruta fxml>». |
| `linkRegistro : Hyperlink` | `ActionEvent (onAction)` | `onRegistrarse()` | `FXMLLoader.load(".../Register.fxml")` y `raiz.getScene().setRoot(root)` (misma ventana); `Registro.info("Pantalla abierta: registro")`. `catch (IOException \| NullPointerException)` → `Registro.error(...)` y `Alert ERROR` «No se pudo abrir la ventana de registro.». |

---

## 2. Registro

| | |
|---|---|
| FXML | `Register.fxml` |
| Controlador | `RegisterController` |
| La abre | `LoginController.onRegistrarse()` (misma ventana, `Scene.setRoot`) |
| Rol | Cliente nuevo, sin sesión |

**Aspecto.** `BorderPane` (`raiz`) con `lblTitulo : Label` («TOLOTOLOGYM»), imágenes laterales `imgToroIzq`, `imgToroDer : ImageView` y un formulario central: `txtNombre`, `txtApellidos`, `txtDni`, `txtMail : TextField`, `txtPassword`, `txtPassword2 : PasswordField` («Repite la contraseña»), `cmbNivel : ComboBox<String>` (membresía), `btnPagar : Button` («Pagar») y `linkLogin : Hyperlink` («Login»).

**Estado inicial (`initialize()`).** `cmbNivel.getItems().addAll("Normal", "Premium")` y `cmbNivel.getSelectionModel().selectFirst()`; alto de las imágenes enlazado al de `raiz` (`subtract(81).multiply(0.80)`). No consulta la base de datos.

| Control (fx:id : tipo) | Evento (nombre técnico) | Método | Tratamiento |
|---|---|---|---|
| `btnPagar : Button` | `ActionEvent (onAction)` | `onPagar()` | Normaliza: DNI `trim().toUpperCase()`, mail `trim().toLowerCase()`. Validaciones en orden (cada una termina el método): 1) Algún campo vacío o `cmbNivel.getValue() == null` → `Alert WARNING` «Rellena todos los campos.». 2) DNI que no cumple `\d{8}[A-Z]` → `Registro.aviso(...)` + `Alert WARNING` «El DNI debe tener 8 números y una letra (ej. 12345678A).». 3) `!dao.mailValido(mail)` (expresión regular del DAO) → `Registro.aviso(...)` + `Alert WARNING` «Escribe un mail válido (ej. nombre@gmail.com).» y `txtMail.requestFocus()`. 4) Contraseñas distintas → `Registro.aviso(...)` + `Alert WARNING` «Las contraseñas no coinciden.». 5) `dao.existeMail(mail)` (busca en `Clientes` y `Trabajadores`) → `Registro.aviso(...)` + `Alert WARNING` «Ya existe un usuario con ese mail.». Si todo es correcto construye un `org.bson.Document` {`dni`, `nombre`, `apellido`, `fechaAlta: new Date()`, `mail`, `contraseña`, `membresia` (en minúsculas), `logros: []`, `clases: []`} y llama a `dao.insertarCliente(Document)`; `Registro.info("Registro completado: …")`, `Alert INFORMATION` «Registro completado. Ya puedes iniciar sesión.» y vuelve al login con `onVolverLogin()`. Excepciones: `DuplicadoException` → `Registro.aviso(...)` + `Alert WARNING` «Ya existe un usuario con ese DNI o ese mail.»; `ConexionException` → `Registro.error(...)` + `Alert ERROR` «No se puede conectar con MongoDB. Comprueba que esté iniciado.»; `DatosException` → `Registro.error(...)` + `Alert ERROR` «No se pudo completar el registro.». |
| `linkLogin : Hyperlink` | `ActionEvent (onAction)` | `onVolverLogin()` | `FXMLLoader.load(".../Login.fxml")` y `raiz.getScene().setRoot(root)` (misma ventana); `Registro.info("Vuelta al login desde el registro")`. `catch (IOException \| NullPointerException)` → `Registro.error(...)` + `Alert ERROR` «No se pudo abrir la ventana de Login.». |

---

## 3. Panel del gerente

| | |
|---|---|
| FXML | `Gerente.fxml` |
| Controlador | `GerenteController` |
| La abre | `LoginController.onIniciarSesion()` con rol `gerente` (ventana nueva «Tolotolo - Gerente»); `onVolver()` de Gestión de clientes, de monitores, de clases y del Horario cuando `rolActual` no es `monitor` (misma ventana) |
| Rol | Gerente |

**Aspecto.** `BorderPane` (`raiz`) con barra superior: `lblTitulo : Label` («TOLOTOLOGYM»), `btnVerLog : Button` («Ver log») y `btnCerrarSesion : Button` («Cerrar Sesión»). En el centro, tres tarjetas `VBox` que funcionan como botones, cada una con su imagen: `cardMonitores` (`imgMonitores`), `cardClases` (`imgClases`) y `cardClientes` (`imgClientes`), todas `ImageView`.

**Estado inicial (`initialize()`).** Sin acceso a datos. Para cada tarjeta, `ajustarImagen(img, card)` pone `setPreserveRatio(true)` y registra dos `ChangeListener<Number>`: sobre `card.widthProperty()` (la imagen pasa a `fitWidth = ancho × 0,75`) y sobre `card.heightProperty()` (`fitHeight = alto × 0,55`). El campo `abriendo` (boolean) empieza a `false`.

| Control (fx:id : tipo) | Evento (nombre técnico) | Método | Tratamiento |
|---|---|---|---|
| `cardMonitores : VBox` | `MouseEvent (onMouseClicked)` | `onGestionarMonitores()` | `abrirVentana(".../GestorMonitor.fxml", "Tolotolo - Gestión de monitores")`: si `abriendo` es `true` no hace nada (evita doble clic); lo pone a `true`; si `getClass().getResource(fxml)` es `null` → `Registro.aviso(...)` + `Alert ERROR` «No se encuentra la pantalla <fxml>»; si no, `FXMLLoader.load(URL)`, `Stage` nuevo con `App.ponerIcono(Stage)`, `show()`, `Registro.info("Pantalla abierta: <título>")` y cierra la ventana del gerente. `catch (IOException \| NullPointerException)` → `abriendo = false`, `Registro.error(...)` + `Alert ERROR` «No se pudo abrir la pantalla <fxml>». |
| `cardClases : VBox` | `MouseEvent (onMouseClicked)` | `onGestionarClases()` | No usa `abrirVentana`: `FXMLLoader.load(".../GestorClase.fxml")`, `Stage` nuevo «Tolotolo - Gestión de clases» (sin icono), `show()` y cierra la ventana del gerente. `catch (IOException \| NullPointerException)` → `Registro.error("Error en GerenteController", e)` + `new Alert(ERROR, "No se pudo cargar GestorClases.").showAndWait()`. |
| `cardClientes : VBox` | `MouseEvent (onMouseClicked)` | `onGestionarClientes()` | `abrirVentana(".../GestorCliente.fxml", "Tolotolo - Gestión de clientes")` (mismo tratamiento que `cardMonitores`). |
| `btnVerLog : Button` | `ActionEvent (onAction)` | `onVerLog()` | `FXMLLoader.load(".../VisorLog.fxml")`, `Stage` nuevo «Tolotolo - Log de operaciones» con `App.ponerIcono`, `visor.initOwner(raiz.getScene().getWindow())` y `show()`; el panel del gerente NO se cierra. `Registro.info("Visor del log abierto")`. `catch (IOException \| NullPointerException)` → `Registro.error(...)` + `Alert ERROR` «No se pudo abrir el visor del log.». |
| `btnCerrarSesion : Button` | `ActionEvent (onAction)` | `onCerrarSesion()` | `Registro.info("Sesión cerrada desde el panel del gerente")` y `abrirVentana(".../Login.fxml", "Tolotolo - Iniciar sesión")` (ventana nueva y cierre del panel). |

---

## 4. Panel del monitor

| | |
|---|---|
| FXML | `Monitor.fxml` |
| Controlador | `MonitorController` (implementa `javafx.fxml.Initializable`) |
| La abre | `LoginController.onIniciarSesion()` con rol `monitor` (ventana nueva «Tolotolo - Monitor»); `onVolver()` de Gestión de clientes, de monitores, de clases y del Horario cuando `rolActual` es `monitor` (misma ventana) |
| Rol | Monitor |

**Aspecto.** `BorderPane` (`raiz`) con barra superior `lblTitulo : Label` («TOLOTOLOGYM») y `btnCerrarSesion : Button` («cerrar sesion»); en el centro dos tarjetas `VBox`: `cardClases` (`imgClases : ImageView`) y `cardClientes` (`imgClientes : ImageView`). El monitor no tiene acceso a la gestión de monitores ni al log.

**Estado inicial (`initialize(URL, ResourceBundle)`).** `ajustarImagen(img, card)` enlaza `img.fitWidthProperty()` a `card.widthProperty().multiply(0.85)` y `fitHeightProperty()` a `card.heightProperty().multiply(0.80)`. Si `LoginController.mailActual` no es `null`, llama a `dao.buscarTrabajadorPorMail(mailActual)` y, si existe, a `cargarFotoPerfil(dni)`, que usaría `GestorImagenes.cargar(dni)` y `circuloFoto.setFill(new ImagePattern(foto))`. El panel del monitor no muestra foto: `Monitor.fxml` no incluye el círculo `circuloFoto` y `cargarFotoPerfil` sale sin hacer nada cuando es `null`.

| Control (fx:id : tipo) | Evento (nombre técnico) | Método | Tratamiento |
|---|---|---|---|
| `cardClases : VBox` | `MouseEvent (onMouseClicked)` | `onGestionarClases()` | `FXMLLoader.load(".../GestorClase.fxml")`, `Stage` nuevo «Tolotolo - Gestión de clases», `show()` y cierre del panel. `catch (IOException \| NullPointerException)` → `Registro.error("Error en MonitorController", e)` + `new Alert(ERROR, "No se pudo cargar GestorClases.")`. |
| `cardClientes : VBox` | `MouseEvent (onMouseClicked)` | `onGestionarClientes()` | `FXMLLoader.load(".../GestorCliente.fxml")`, `Stage` nuevo «Tolotolo - GestorCliente», `show()` y cierre del panel. `catch` → `Registro.error(...)` + `new Alert(ERROR, "No se pudo cargar GestorClientes.")`. |
| `btnCerrarSesion : Button` | `ActionEvent (onAction)` | `onCerrarSesion()` | `FXMLLoader.load(".../Login.fxml")`, `Stage` nuevo «Tolotolo - Iniciar sesión», `show()` y cierre del panel. `catch` → `Registro.error(...)` + `new Alert(ERROR, "No se pudo volver al login.")`. No escribe línea de cierre de sesión en el log. |

---

## 5. Cliente

| | |
|---|---|
| FXML | `Cliente.fxml` |
| Controlador | `ClienteController` (implementa `Initializable`; tiene un segundo constructor `ClienteController(daoImplementacio)` para los tests) |
| La abre | `LoginController.onIniciarSesion()` con rol `cliente` (ventana nueva «Tolotolo - Cliente») |
| Rol | Cliente |

**Aspecto.** `BorderPane` (`raiz`). Arriba: `imgLogo : ImageView`, `lblGimnasio : Label` («TOLOTOLOGYM»), `circuloFoto : Circle` y `btnCerrarSesion : Button` («cerrar sesion»). Izquierda: seis tarjetas fijas con las clases del cliente (`lblClase1`–`lblClase6`, `lblPlazas1`–`lblPlazas6 : Label`). Centro (`vboxCentro : VBox`): tres tarjetas visibles de clases disponibles (`lblClase7`–`lblClase9`, `lblPlazas7`–`lblPlazas9`), nueve tarjetas ocultas `tarjeta10`–`tarjeta18 : VBox` (`visible="false" managed="false"`, con `lblClase10`–`18` y `lblPlazas10`–`18`) y `btnVerMasClases : Button` («Ver más clases»). Derecha: ficha de solo lectura (`txtNombre`, `txtDni`, `txtMail`, `txtMembresia : TextField`, `editable="false"`) y `tablaLogros : TableView<Logros>` con `colLogro1` («Nº»), `colLogro2` («Nombre»), `colLogro3` («Fecha»), `colLogro4` («Sem.») de tipo `TableColumn<Logros, String>`.

**Estado inicial (`initialize(URL, ResourceBundle)`).**
1. `cargarCliente(LoginController.mailActual)`: `dao.buscarClientePorMail(mail)`; si existe, `dao.revisarLogros(mail)` (concede «Constante», «Imparable» y «Leyenda» a las 4, 8 y 12 semanas desde `fechaAlta` con `dao.darLogro(...)`) y vuelve a leer el cliente con `dao.buscarClientePorMail(mail)`. Si el cliente no existe, `initialize` termina y la pantalla queda vacía.
2. Rellena `txtNombre` (nombre + apellido), `txtDni`, `txtMail`, `txtMembresia`.
3. `cargarFotoPerfil(dni)` → `GestorImagenes.cargar(dni)` y `circuloFoto.setFill(new ImagePattern(foto))`; si el cliente no tiene foto, el círculo se queda gris.
4. Clases del cliente (lista `clases` del documento, máximo `MAX_CLASES_APUNTADO = 6`): nombre en `lblClaseN` y plazas en `lblPlazasN` con `plazasDe(id_clase)`, que llama a `dao.ultimaSesionDeClase(ObjectId)` y escribe «ocupadas/plazaMax» o «sin sesión».
5. `todasLasClases = dao.listarClases()`; las tres primeras (`CLASES_VISIBLES_AL_ABRIR = 3`) se ponen en `lblClase7`–`9` con su `_id` guardado en `Label.setUserData(ObjectId)` y sus plazas.
6. Si hay 3 clases o menos (`hayMasClasesQueLasVisibles`), `btnVerMasClases.setDisable(true)`.
7. `tablaLogros`: `setCellValueFactory(new PropertyValueFactory<Logros,String>("numero" | "nombre" | "fecha" | "semanas"))` y filas de `construirLogros(logros)` (clase del modelo `modelo.Logros`, numeradas desde 1).
8. `ponerClick(lblClaseN, lblPlazasN)` para N = 7…18: sobre el contenedor de la etiqueta (`nombre.getParent()`) pone `setCursor(Cursor.HAND)` y `setOnMouseClicked(EventHandler<MouseEvent>)` que llama a `apuntar(nombre, plazas)`.

No hay `try/catch` en `initialize`: un fallo de MongoDB (`DatosException`) no se trata aquí.

| Control (fx:id : tipo) | Evento (nombre técnico) | Método | Tratamiento |
|---|---|---|---|
| Tarjetas de clases disponibles (padre `VBox` de `lblClase7`–`lblClase18`) | `MouseEvent` (`setOnMouseClicked` por código) | `apuntar(Label, Label)` | 1) Si la etiqueta está vacía, no hace nada. 2) Si el nombre ya está entre `lblClase1`–`6` (`yaApuntado`) → `new Alert(INFORMATION, "Ya estás apuntado a <clase>")`. 3) Confirmación: `Alert CONFIRMATION` título «Apuntarse a una clase», texto «¿Quieres apuntarte a <clase>?», botones `ButtonType.YES` / `ButtonType.NO`. Con NO o cierre, termina. 4) Con YES: `primerHuecoLibre(...)`; si es `-1` → `new Alert(WARNING, "Ya tienes 6 clases, no caben más.")`. 5) Toma el `ObjectId` de `getUserData()` (si es `null`, termina) y llama a `dao.apuntarseAClase(LoginController.mailActual, idClase, nombre)`, que añade al cliente a `asistentes` de la última sesión solo si queda plaza y no estaba (un único `updateOne` con `$expr`) y después añade la clase a su lista `clases`; si esto falla, devuelve la plaza (compensación). 6) Relee las plazas con `plazasDe(idClase)` y actualiza la etiqueta pulsada. 7) Si devolvió `false` → `new Alert(WARNING, "No se pudo apuntar a <clase>: la clase está completa (<plazas>) o hubo un error al guardar.")`. Si fue bien, escribe la clase y las plazas en el primer hueco libre de la columna izquierda. |
| `btnVerMasClases : Button` | `ActionEvent (onAction)` | `verMasClases()` | Recorre `todasLasClases` a partir de la cuarta y, para cada una (hasta 9), rellena `lblClase10`–`18` (con `setUserData(_id)`) y `lblPlazas10`–`18` (`plazasDe` → `dao.ultimaSesionDeClase`), y muestra su tarjeta con `setVisible(true)` y `setManaged(true)`. Al final `btnVerMasClases.setDisable(true)`. |
| `btnCerrarSesion : Button` | `ActionEvent (onAction)` | `cerrarSesion()` | `FXMLLoader.load(".../Login.fxml")`, `Stage` nuevo (sin título ni icono), `show()` y cierre de la ventana del cliente. Declara `throws Exception`: no captura errores ni escribe en el log. |

---

## 6. Gestión de clientes

| | |
|---|---|
| FXML | `GestorCliente.fxml` |
| Controlador | `GestionClientesController` |
| La abre | `GerenteController.onGestionarClientes()` (ventana nueva «Tolotolo - Gestión de clientes») y `MonitorController.onGestionarClientes()` (ventana nueva «Tolotolo - GestorCliente») |
| Rol | Gerente y monitor. El único tratamiento que depende del rol es `onVolver()` (vuelve a `Monitor.fxml` o a `Gerente.fxml` según `LoginController.rolActual`) |

**Aspecto.** `BorderPane` (`raiz`). Barra superior: `lblTitulo : Label` («TOLOTOLOGYM», pulsable) y `btnCerrarSesion : Button`. Izquierda: `listClientes : ListView<Document>` y `btnAlta : Button` («Dar de alta»). Derecha, ficha del cliente: `lblNombreCompleto : Label`, `panelFoto : StackPane` con `imgFoto : ImageView`, `txtNombre`, `txtApellidos`, `txtDni`, `txtMail : TextField`, `txtAlta : TextField` (`editable="false"`), `choMembresia : ChoiceBox<String>`, `btnGuardar : Button` («Guardar datos») y `btnBaja : Button` («Dar de baja»); los dos botones vienen con `disable="true"` en el FXML.

**Estado inicial (`initialize()`).**
- `choMembresia.getItems().addAll("normal", "premium")`.
- `listClientes.setCellFactory(Callback)` con la celda propia `CeldaCliente extends ListCell<Document>` (`updateItem`: «nombre apellido» y, en otra línea, «dni · membresía»).
- `ChangeListener<Document>` sobre `listClientes.getSelectionModel().selectedItemProperty()` → `mostrarCliente(elegido)`.
- `imgFoto.fitWidthProperty()` / `fitHeightProperty()` enlazados al tamaño de `panelFoto` menos 4 px.
- `Registro.info("Pantalla abierta: gestión de clientes")` y `cargarClientes()`: `dao.listarClientes()` → `listClientes.getItems().setAll(...)` y `Registro.info("Clientes cargados: N")`; `catch (DatosException)` → `Registro.error(...)` + `Alert ERROR` «No se puede conectar con MongoDB.».
- `btnGuardar` y `btnBaja` quedan desactivados hasta que haya selección.

| Control (fx:id : tipo) | Evento (nombre técnico) | Método | Tratamiento |
|---|---|---|---|
| `listClientes : ListView<Document>` | `ChangeListener` sobre `selectionModel.selectedItemProperty()` | `mostrarCliente(Document)` | Con `null`: `lblNombreCompleto` vuelve a «Nombre y apellidos cliente», vacía los campos, `choMembresia.setValue(null)`, quita la foto y desactiva `btnGuardar` y `btnBaja`. Con cliente: rellena nombre, apellidos, DNI, mail y membresía; `fechaAlta` (BSON `Date`) se convierte a `LocalDate` con `ZoneId.systemDefault()` y se escribe en `txtAlta` con `DateTimeFormatter` «dd/MM/yyyy»; foto con `GestorImagenes.cargar(dni)`; activa `btnGuardar` y `btnBaja`. |
| `btnGuardar : Button` | `ActionEvent (onAction)` | `onGuardarDatos()` | Sin selección, termina. Validaciones: 1) algún campo vacío o membresía `null` → `Alert WARNING` «Rellena todos los campos.»; 2) mail que no cumple `^.+@.+\..+$` → `Alert WARNING` «El mail no es válido.». Guarda: `dao.actualizarCliente(ObjectId, nombre, apellido, dni, mail, membresia)` y `GestorImagenes.renombrar(dniOriginal, dni)` (la foto sigue al DNI nuevo); actualiza el `Document` en memoria, `listClientes.refresh()`, `lblNombreCompleto`; `Registro.info("Cliente modificado: …")` y `Alert INFORMATION` «Datos guardados.». Excepciones: `DuplicadoException` → `Registro.aviso(...)` + `Alert ERROR` «Ya existe otro cliente con ese DNI o ese mail.»; `ConexionException` → `Registro.error(...)` + `Alert ERROR` «No se puede conectar con MongoDB.»; `DatosException` → `Registro.error(...)` + `Alert ERROR` «No se pudieron guardar los datos.»; `IOException` (foto) → `Registro.error(...)` + `Alert ERROR` «Datos guardados, pero no se pudo renombrar la foto.». |
| `btnAlta : Button` | `ActionEvent (onAction)` | `onDarDeAlta()` | Abre un diálogo creado por código (subtabla 6.1). Con Cancelar o cierre, termina. Validaciones: algún campo vacío → `Alert WARNING` «Rellena todos los campos.»; mail que no cumple `^.+@.+\..+$` → `Alert WARNING` «El mail no es válido.». Construye el `Document` {`dni`, `nombre`, `apellido`, `fechaAlta: new Date()`, `mail`, `contraseña`, `membresia`, `logros: []`, `clases: []`} y llama a `dao.insertarCliente(Document)`; lo añade a `listClientes`, lo selecciona (`getSelectionModel().select`, que dispara `mostrarCliente`) y `scrollTo`; `Registro.info("Cliente dado de alta: …")`. Excepciones: `DuplicadoException` → `Registro.aviso(...)` + `Alert ERROR` «Ya existe un cliente con ese DNI o ese mail.»; `ConexionException` → `Registro.error(...)` + `Alert ERROR` «No se puede conectar con MongoDB.»; `DatosException` → `Registro.error(...)` + `Alert ERROR` «No se pudo guardar el cliente.». |
| `btnBaja : Button` | `ActionEvent (onAction)` | `onDarDeBaja()` | Sin selección, termina. `Alert CONFIRMATION` «¿Seguro que quieres dar de baja a <nombre> <apellido>?» con `ButtonType.YES` / `ButtonType.NO`. Con YES: `dao.eliminarCliente(ObjectId)` (en transacción: quita al cliente de `asistentes` de todas las sesiones y borra el documento), `Registro.info("Cliente dado de baja: …")`, `GestorImagenes.borrar(dni)`, `cargarClientes()` y `mostrarCliente(null)`. Excepciones: `DatosException` → `Registro.error(...)` + `Alert ERROR` «No se pudo dar de baja al cliente.»; `IOException` (foto) → `Registro.error(...)` + `Alert ERROR` «Cliente dado de baja, pero no se pudo borrar su foto.». |
| `panelFoto : StackPane` | `MouseEvent (onMouseClicked)` | `onElegirFoto()` | Sin selección → `Alert WARNING` «Elige primero un cliente de la lista.». Abre `javafx.stage.FileChooser` («Elegir foto», filtro `ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg")`) con `showOpenDialog(ventana)`; si se cancela, termina. `GestorImagenes.guardar(dni, File)`, `Registro.info("Foto actualizada del cliente …")` y vuelve a mostrar la foto. `catch (IOException)` → `Registro.error(...)` + `Alert ERROR` «No se pudo guardar la imagen.». |
| `lblTitulo : Label` | `MouseEvent (onMouseClicked)` | `onVolver()` | Según `LoginController.rolActual`: `"monitor"` → `Monitor.fxml` («Tolotolo - Monitor»); cualquier otro → `Gerente.fxml` («Tolotolo - Gerente»). Si `getResource` devuelve `null` → `Registro.aviso(...)` + `Alert ERROR` «No se encuentra el FXML <ruta>». Si no, `FXMLLoader.load(URL)`, `escena.setRoot(root)` (misma ventana), `Stage.setTitle(...)` y `Registro.info("Vuelta al panel principal desde la gestión de clientes")`. `catch (Exception)` → `Registro.error(...)` + `Alert ERROR` «No se pudo volver: <mensaje>». |
| `btnCerrarSesion : Button` | `ActionEvent (onAction)` | `onCerrarSesion()` | `FXMLLoader.load(".../Login.fxml")`, `Stage` nuevo «Tolotolo - Iniciar sesión» con icono, `show()`, `Registro.info("Sesión cerrada desde la gestión de clientes")` y cierra la ventana. `catch (IOException \| NullPointerException)` → `Registro.error(...)` + `Alert ERROR` «No se pudo volver al login.». |

**6.1. Diálogo «Dar de alta cliente»** (`Dialog<ButtonType>` creado en `onDarDeAlta()`, con `App.ponerIcono(Dialog)` y contenido `GridPane`).

| Control (tipo) | Evento | Tratamiento |
|---|---|---|
| `fDni`, `fNombre`, `fApellido`, `fMail : TextField`; `fPass : PasswordField` | — (se leen al aceptar) | Se recogen con `getText().trim()` y se validan como se indica en `btnAlta`. |
| `fMembresia : ChoiceBox<String>` | — | Opciones «normal» y «premium»; valor inicial «normal». |
| `ButtonType.OK` | resultado de `Dialog.showAndWait()` | Continúa con la validación y el alta. |
| `ButtonType.CANCEL` (o cerrar la ventana: `orElse(ButtonType.CANCEL)`) | resultado de `Dialog.showAndWait()` | Termina sin tocar la base de datos. |

---

## 7. Gestión de monitores

| | |
|---|---|
| FXML | `GestorMonitor.fxml` |
| Controlador | `GestionMonitorController` |
| La abre | `GerenteController.onGestionarMonitores()` (ventana nueva «Tolotolo - Gestión de monitores») |
| Rol | Gerente (el panel del monitor no tiene acceso) |

**Aspecto.** Igual que Gestión de clientes, con `listMonitores : ListView<Document>` a la izquierda y, en la ficha, `txtDireccion : TextField` (prompt «Calle, CP, Ciudad») en lugar del selector de membresía. Controles: `lblTitulo`, `btnCerrarSesion`, `btnAlta`, `lblNombreCompleto`, `panelFoto : StackPane`, `imgFoto : ImageView`, `txtNombre`, `txtApellidos`, `txtDni`, `txtMail`, `txtAlta` (`editable="false"`), `txtDireccion`, `btnGuardar` y `btnBaja` (ambos `disable="true"` en el FXML).

**Estado inicial (`initialize()`).**
- `listMonitores.setCellFactory(Callback)` con `CeldaMonitor extends ListCell<Document>` («nombre apellido» y, debajo, «dni · mail»).
- `ChangeListener<Document>` sobre `listMonitores.getSelectionModel().selectedItemProperty()` → `mostrarMonitor(elegido)`.
- Foto enlazada al tamaño de `panelFoto`.
- `Registro.info("Pantalla abierta: gestión de monitores")` y `cargarMonitores()`: `dao.listarMonitores()` (trabajadores con `tipo = "monitor"`), `Registro.info("Monitores cargados: N")`; `catch (DatosException)` → `Registro.error(...)` + `Alert ERROR` «No se puede conectar con MongoDB.».

| Control (fx:id : tipo) | Evento (nombre técnico) | Método | Tratamiento |
|---|---|---|---|
| `listMonitores : ListView<Document>` | `ChangeListener` sobre `selectionModel.selectedItemProperty()` | `mostrarMonitor(Document)` | Con `null`: «Nombre y apellidos monitor», campos vacíos, sin foto y `btnGuardar`/`btnBaja` desactivados. Con monitor: rellena la ficha; la dirección se escribe con `direccionATexto(m)` (primer elemento del array `direccion`: «calle, cp, ciudad»); `fechaAlta` en «dd/MM/yyyy»; `GestorImagenes.cargar(dni)`; activa los dos botones. |
| `btnGuardar : Button` | `ActionEvent (onAction)` | `onGuardarDatos()` | Sin selección, termina. Validaciones: 1) nombre, apellidos, DNI o mail vacíos → `Alert WARNING` «Rellena todos los campos.»; 2) mail que no cumple `^.+@.+\..+$` → `Alert WARNING` «El mail no es válido.»; 3) `textoADireccion(txtDireccion)` devuelve `null` (no son tres partes separadas por comas o alguna vacía) → `Alert WARNING` «La dirección debe tener el formato: Calle, CP, Ciudad». Guarda: `dao.actualizarMonitor(ObjectId, nombre, apellido, dni, mail, List<Document> direccion)` y `GestorImagenes.renombrar(dniOriginal, dni)`; actualiza el documento en memoria, `listMonitores.refresh()`, `Registro.info("Monitor modificado: …")` y `Alert INFORMATION` «Datos guardados.». Excepciones: `DuplicadoException` → `Registro.aviso(...)` + `Alert ERROR` «Ya existe otro trabajador con ese DNI o ese mail.»; `ConexionException` → `Alert ERROR` «No se puede conectar con MongoDB.»; `DatosException` → `Alert ERROR` «No se pudieron guardar los datos.»; `IOException` → `Alert ERROR` «Datos guardados, pero no se pudo renombrar la foto.» (las tres con `Registro.error(...)`). |
| `btnAlta : Button` | `ActionEvent (onAction)` | `onDarDeAlta()` | Diálogo creado por código (subtabla 7.1). Con Cancelar, termina. Validaciones: algún campo vacío (los ocho) → `Alert WARNING` «Rellena todos los campos.»; mail no válido → `Alert WARNING` «El mail no es válido.». Construye el `Document` {`dni`, `nombre`, `apellido`, `fechaAlta: new Date()`, `mail`, `contraseña`, `direccion: [{calle, cp, ciudad}]`, `tipo: "monitor"`, `clases: []`} y llama a `dao.insertarMonitor(Document)`; lo añade, lo selecciona y hace `scrollTo`; `Registro.info("Monitor dado de alta: …")`. Excepciones: `DuplicadoException` → `Registro.aviso(...)` + `Alert ERROR` «Ya existe un trabajador con ese DNI o ese mail.»; `ConexionException` → `Alert ERROR` «No se puede conectar con MongoDB.»; `DatosException` → `Alert ERROR` «No se pudo guardar el monitor.». |
| `btnBaja : Button` | `ActionEvent (onAction)` | `onDarDeBaja()` | Sin selección, termina. `Alert CONFIRMATION` «¿Seguro que quieres dar de baja a <nombre> <apellido>?» (`YES`/`NO`). Con YES: `dao.eliminarMonitor(ObjectId)`, `Registro.info("Monitor dado de baja: …")`, `GestorImagenes.borrar(dni)`, `cargarMonitores()` y `mostrarMonitor(null)`. Excepciones: `DatosException` → `Alert ERROR` «No se pudo dar de baja al monitor.»; `IOException` → `Alert ERROR` «Monitor dado de baja, pero no se pudo borrar su foto.» (ambas con `Registro.error(...)`). |
| `panelFoto : StackPane` | `MouseEvent (onMouseClicked)` | `onElegirFoto()` | Sin selección → `Alert WARNING` «Elige primero un monitor de la lista.». `FileChooser` («Elegir foto», png/jpg/jpeg); si se cancela, termina. `GestorImagenes.guardar(dni, File)`, `Registro.info("Foto actualizada del monitor …")` y refresca la foto. `catch (IOException)` → `Registro.error(...)` + `Alert ERROR` «No se pudo guardar la imagen.». |
| `lblTitulo : Label` | `MouseEvent (onMouseClicked)` | `onVolver()` | Siempre vuelve a `Gerente.fxml` en la misma ventana (`escena.setRoot`, título «Tolotolo - Gerente») y `Registro.info("Vuelta al panel del gerente desde la gestión de monitores")`. Si no se encuentra el FXML → `Registro.aviso(...)` + `Alert ERROR` «No se encuentra el FXML del panel del gerente.»; `catch (Exception)` → `Registro.error(...)` + `Alert ERROR` «No se pudo volver: <mensaje>». |
| `btnCerrarSesion : Button` | `ActionEvent (onAction)` | `onCerrarSesion()` | Login en ventana nueva con icono, `Registro.info("Sesión cerrada desde la gestión de monitores")` y cierre de la ventana. `catch (IOException \| NullPointerException)` → `Registro.error(...)` + `Alert ERROR` «No se pudo volver al login.». |

**7.1. Diálogo «Dar de alta monitor»** (`Dialog<ButtonType>` con icono y `GridPane`).

| Control (tipo) | Evento | Tratamiento |
|---|---|---|
| `fDni`, `fNombre`, `fApellido`, `fMail`, `fCalle`, `fCp`, `fCiudad : TextField`; `fPass : PasswordField` | — (se leen al aceptar) | `getText().trim()` y validación descrita en `btnAlta`. |
| `ButtonType.OK` | resultado de `Dialog.showAndWait()` | Continúa con la validación y el alta. |
| `ButtonType.CANCEL` / cierre | resultado de `Dialog.showAndWait()` (`orElse(CANCEL)`) | Termina sin escribir en la base de datos. |

---

## 8. Gestión de clases

| | |
|---|---|
| FXML | `GestorClase.fxml` |
| Controlador | `GestionClasesController` |
| La abre | `GerenteController.onGestionarClases()` y `MonitorController.onGestionarClases()` (ventana nueva «Tolotolo - Gestión de clases»); `GestionSesionesController.onVolver()` (misma ventana) |
| Rol | Gerente y monitor; `onVolver()` depende de `LoginController.rolActual` |

**Aspecto.** `BorderPane` (`raiz`) con barra superior (`lblTitulo : Label` pulsable, `btnCerrarSesion : Button`) y tres zonas: lista `listClases : ListView<Document>` con `btnCrearClase : Button` («Crear clase»); ficha de la clase (`lblClase : Label`, `txtNombreClase : TextField`, `choSala : ChoiceBox<Document>`, `btnGestionarSesiones : Button` «Gestionar sesiones», `btnBorrarClase : Button` «Borrar») con la lista de sus sesiones `listSesiones : ListView<Document>`; ficha de la sesión (`lblSesion : Label`, `txtNombreSesion : TextField` con prompt «dd/MM/yyyy HH:mm», `choMonitor : ChoiceBox<Document>`, `txtAforo : TextField`, `listInscritos : ListView<String>`, `btnBorrarSesion : Button` «Borrar»). No hay botón de guardar: se guarda con Enter en los campos o al cambiar un desplegable.

**Estado inicial (`initialize()`).**
- Fábricas de celdas (`setCellFactory`) de `listClases` («nombre» + nombre de la sala, buscado en `choSala.getItems()`, o «sin sala») y de `listSesiones` (fecha «dd/MM/yyyy HH:mm» + «apuntados/plazaMax plazas»).
- `StringConverter<Document>` en `choSala` («nombre (aforoMaximo)») y en `choMonitor` («nombre apellido»).
- Listeners: `ChangeListener` sobre `listClases.getSelectionModel().selectedItemProperty()` → `mostrarClase`; sobre `listSesiones…selectedItemProperty()` → `mostrarSesion`; sobre `choSala…selectedItemProperty()` → `guardarClase()`; sobre `choMonitor…selectedItemProperty()` → `guardarSesion()`. Los dos últimos solo actúan si la bandera `rellenando` es `false` (la ponen a `true` `mostrarClase`/`mostrarSesion` mientras rellenan la ficha) y el valor no es `null`.
- `setOnAction(EventHandler<ActionEvent>)` por código: `txtNombreClase` → `guardarClase()`; `txtNombreSesion` y `txtAforo` → `guardarSesion()`.
- Carga de datos: `choSala.getItems().setAll(dao.listarSalas())`, `choMonitor.getItems().setAll(dao.listarMonitores())`, mapa `nombreClientes` (`_id` → «nombre apellido») con `dao.listarClientes()` y `listClases.getItems().setAll(dao.listarClases())`. `catch (DatosException)` → `Registro.error(...)` + `Alert ERROR` «No se puede conectar con MongoDB.».
- `mostrarClase(null)`: `txtNombreClase`, `choSala`, `btnGestionarSesiones`, `btnBorrarClase` desactivados; `mostrarSesion(null)`: `txtNombreSesion`, `txtAforo`, `choMonitor`, `btnBorrarSesion` desactivados.

En esta pantalla los avisos (`alerta`) no llevan icono, y los `catch` de guardar/borrar no escriben en el log del controlador (el error sí queda en el log de operaciones por `fallo(...)` del DAO).

| Control (fx:id : tipo) | Evento (nombre técnico) | Método | Tratamiento |
|---|---|---|---|
| `listClases : ListView<Document>` | `ChangeListener` sobre `selectionModel.selectedItemProperty()` | `mostrarClase(Document)` | Con `rellenando = true`: `lblClase` «Datos <nombre>» (o «Datos clase»), `txtNombreClase`, selecciona en `choSala` la sala de `salaid`; activa o desactiva los cuatro controles de la clase. Después vacía `listSesiones` y, si hay clase, `dao.listarSesiones(ObjectId)` (ordenadas por fecha ascendente); `catch (DatosException)` → `Alert ERROR` «No se pudieron cargar las sesiones.». Termina con `mostrarSesion(null)`. |
| `txtNombreClase : TextField` | `ActionEvent` (Enter; `setOnAction` por código) | `guardarClase()` | Sin clase seleccionada, termina. Nombre vacío o sala `null` → `Alert WARNING` «La clase necesita nombre y sala.». `dao.actualizarClase(ObjectId, nombre, ObjectId salaId)`, actualiza el documento, `listClases.refresh()` y `lblClase`. `catch (DatosException)` → `Alert ERROR` «No se pudo guardar la clase.». |
| `choSala : ChoiceBox<Document>` | `ChangeListener` sobre `selectionModel.selectedItemProperty()` | `guardarClase()` | Solo si `!rellenando` y sala no `null`; mismo tratamiento que la fila anterior. |
| `btnCrearClase : Button` | `ActionEvent (onAction)` | `onCrearClase()` | Diálogo `pedir("Crear clase", grid)` (subtabla 8.1). Con Cancelar, termina. Nombre vacío o sala sin elegir → `Alert WARNING` «La clase necesita nombre y sala.». `dao.insertarClase(Document {nombre, horas: [], salaid})` (el driver rellena `_id`), la añade a `listClases`, la selecciona y `scrollTo`. `catch (DatosException)` → `Alert ERROR` «No se pudo crear la clase.». |
| `btnBorrarClase : Button` | `ActionEvent (onAction)` | `onBorrarClase()` | Sin selección, termina. `Alert CONFIRMATION` «¿Borrar la clase <nombre> y todas sus sesiones (<n>)?» (`YES`/`NO`). Con YES: `dao.eliminarClase(ObjectId)` (en transacción: borra sus sesiones, la quita de la lista `clases` de los clientes y borra la clase), la quita de `listClases` y `clearSelection()` (dispara `mostrarClase(null)`). `catch (DatosException)` → `Alert ERROR` «No se pudo borrar la clase.». |
| `btnGestionarSesiones : Button` | `ActionEvent (onAction)` | `onGestionarSesiones()` | Sin selección, termina. `new FXMLLoader(".../GestorSesiones.fxml")`, `load()`, `getController()` → `GestionSesionesController.setClase(Document)`; `escena.setRoot(vista)` (misma ventana) y título «Tolotolo - Sesiones de <nombre>». `catch (IOException \| NullPointerException)` → `Registro.error(...)` + `Alert ERROR` «No se pudo abrir el horario de sesiones.». |
| `listSesiones : ListView<Document>` | `ChangeListener` sobre `selectionModel.selectedItemProperty()` | `mostrarSesion(Document)` | Con `rellenando = true`: `lblSesion` «Sesión <fecha>» (o «Datos sesion»), `txtNombreSesion` con la fecha «dd/MM/yyyy HH:mm», `txtAforo` con `plazaMax`, selecciona el monitor en `choMonitor`; rellena `listInscritos` con el nombre de cada `asistentes.clienteid` sacado del mapa `nombreClientes` («(cliente borrado)» si no está); activa o desactiva los controles de la sesión. |
| `txtNombreSesion : TextField` | `ActionEvent` (Enter; `setOnAction` por código) | `guardarSesion()` | Sin sesión o sin clase, termina. Validaciones: 1) `LocalDateTime.parse(texto, "dd/MM/yyyy HH:mm")` lanza `DateTimeParseException` → `Alert WARNING` «La fecha debe tener el formato dd/MM/yyyy HH:mm.»; 2) `validarAforo(...)`: no numérico → `Alert WARNING` «El aforo debe ser un número.»; ≤ 0 o mayor que el `aforoMaximo` de la sala → `Alert WARNING` «El aforo debe estar entre 1 y <max> (aforo de la sala).»; menor que los apuntados → `Alert WARNING` «Ya hay <n> clientes apuntados; el aforo no puede ser menor.»; 3) monitor `null` → termina sin aviso. Guarda con `dao.actualizarSesion(ObjectId, Date, int plazaMax, ObjectId monitorId)`, actualiza el documento, `listSesiones.refresh()` y `lblSesion`. `catch (DatosException)` → `Alert ERROR` «No se pudo guardar la sesión.». |
| `txtAforo : TextField` | `ActionEvent` (Enter; `setOnAction` por código) | `guardarSesion()` | Igual que la fila anterior. |
| `choMonitor : ChoiceBox<Document>` | `ChangeListener` sobre `selectionModel.selectedItemProperty()` | `guardarSesion()` | Solo si `!rellenando` y monitor no `null`; igual que `txtNombreSesion`. |
| `btnBorrarSesion : Button` | `ActionEvent (onAction)` | `onBorrarSesion()` | Sin selección, termina. `Alert CONFIRMATION` «¿Borrar la sesión del <fecha>?» (`YES`/`NO`). Con YES: `dao.eliminarSesion(ObjectId)`, la quita de `listSesiones` y `clearSelection()`. `catch (DatosException)` → `Alert ERROR` «No se pudo borrar la sesión.». |
| `lblTitulo : Label` | `MouseEvent (onMouseClicked)` | `onVolver()` | Según `LoginController.rolActual`: `"monitor"` → `Monitor.fxml` («Tolotolo - Monitor»); otro → `Gerente.fxml` («Tolotolo - Gerente»), en la misma ventana (`escena.setRoot(FXMLLoader.load(...))`). `catch (Exception)` → `Registro.error(...)` + `Alert ERROR` «No se pudo volver: <mensaje>». |
| `btnCerrarSesion : Button` | `ActionEvent (onAction)` | `onCerrarSesion()` | Login en `Stage` nuevo «Tolotolo - Iniciar sesión» (sin icono) y cierre de la ventana. `catch (IOException \| NullPointerException)` → `Registro.error(...)` + `Alert ERROR` «No se pudo volver al login.». |

**8.1. Diálogo «Crear clase»** (`Dialog<ButtonType>` del método `pedir(titulo, GridPane)`, sin icono).

| Control (tipo) | Evento | Tratamiento |
|---|---|---|
| `fNombre : TextField` | — (se lee al aceptar) | `getText().trim()`; vacío → aviso «La clase necesita nombre y sala.». |
| `fSala : ChoiceBox<Document>` | — | Mismos elementos que `choSala` (`new ChoiceBox<>(choSala.getItems())`) y su mismo conversor. |
| `ButtonType.OK` | resultado de `Dialog.showAndWait()` | `pedir` devuelve `true` y se crea la clase. |
| `ButtonType.CANCEL` / cierre | resultado de `showAndWait()` (`orElse(CANCEL)`) | `pedir` devuelve `false` y no se hace nada. |

---

## 9. Horario de sesiones

| | |
|---|---|
| FXML | `GestorSesiones.fxml` |
| Controlador | `GestionSesionesController` |
| La abre | `GestionClasesController.onGestionarSesiones()` en la misma ventana, con `FXMLLoader.getController()` y `setClase(Document)` (título «Tolotolo - Sesiones de <clase>») |
| Rol | Gerente y monitor (los que llegan a Gestión de clases) |

**Aspecto.** `BorderPane` (`raiz`) con `lblTitulo : Label` (muestra el nombre de la clase; pulsable) y `btnCerrarSesion : Button`. En el centro `gridHorario : GridPane`: columnas Lunes a Sábado y filas de 09:00 - 10:00 a 22:00 - 23:00. Cada hueco es un `StackPane` sin `fx:id`, con `styleClass="hueco-sesion"` y `userData="columna,hora"` (84 huecos: 6 días × 14 horas).

**Estado inicial.**
- `initialize()`: recorre `gridHorario.getChildren()` y, a cada nodo con la clase de estilo `hueco-sesion`, le pone `setOnMouseClicked(EventHandler<MouseEvent>)` → `onHueco(hueco)`.
- `setClase(Document clase)`: guarda la clase, `lblTitulo.setText(nombre)` y `pintar()`: cada hueco cuya franja (`dia`, `horainicio` «HH:00») está en el array `horas` de la clase se pinta en negro (`ESTILO_OCUPADO`) con una `Label` blanca con el nombre de la clase; el resto, en gris (`ESTILO_LIBRE`). No consulta la base de datos (usa el documento recibido).

| Control (fx:id : tipo) | Evento (nombre técnico) | Método | Tratamiento |
|---|---|---|---|
| Hueco libre (`StackPane`, clase `hueco-sesion`) | `MouseEvent` (`setOnMouseClicked` por código) | `onHueco(StackPane)` → `crearEn(DayOfWeek, hora, dia, texto)` | `aforoSala()` (lee `dao.listarSalas()` y toma el `aforoMaximo` de la sala de la clase); `desplegableMonitores()` (`dao.listarMonitores()`); abre el diálogo «Nueva sesión: <día> <HH:00>» (subtabla 9.1, sin «Borrar») con el aforo de la sala ya escrito. Si no se pulsa Guardar, termina. `validar(...)`: sin monitor → `Alert WARNING` «Elige un monitor.»; aforo no numérico → «El aforo debe ser un número.»; fuera de 1…aforo de sala → «El aforo debe estar entre 1 y <max> (aforo de la sala).» (todas WARNING). Si es válido: `insertarProxima(...)` calcula la fecha con `proximaFecha(ahora, día, hora)` (la siguiente ocurrencia posterior a ahora) y llama a `dao.insertarSesion(Document {claseid, fecha, plazaMax, monitor, asistentes: []})`; añade {`dia`, `horainicio`, `horafin`} a `horas` y guarda con `dao.actualizarHorasClase(ObjectId, List<Document>)`. Al final, siempre, `pintar()`. |
| Hueco ocupado (`StackPane`, clase `hueco-sesion`) | `MouseEvent` (`setOnMouseClicked` por código) | `onHueco(StackPane)` → `editarEn(DayOfWeek, hora, franja, texto)` | `sesionesFuturas(día, hora)`: `dao.listarSesiones(ObjectId)` filtradas a las posteriores a ahora de ese día y hora. Abre el diálogo «Sesión: <día> <HH:00>» (subtabla 9.1, con «Borrar») con el monitor y el aforo de la próxima sesión (o el aforo de la sala si no hay). **Borrar** → `Alert CONFIRMATION` «¿Quitar <día> <HH:00> del horario y borrar sus <n> sesiones futuras?» (`YES`/`NO`); con YES, `dao.eliminarSesion(ObjectId)` por cada sesión futura, quita la franja de `horas` y `dao.actualizarHorasClase(...)`. **Guardar** → calcula el máximo de apuntados entre las futuras y `validar(...)` (añade «Ya hay <n> clientes apuntados; el aforo no puede ser menor.»); si no quedan futuras, `insertarProxima(...)`; si quedan, `dao.actualizarSesion(ObjectId, fecha, aforo, monitorId)` en cada una. Las sesiones pasadas no se tocan. Al final, `pintar()`. |
| (los dos anteriores) | `DatosException` lanzada por cualquier llamada al DAO | `onHueco` | `catch (DatosException)` → `Registro.error(...)` + `Alert ERROR` «No se pudo guardar el horario en MongoDB.»; después `pintar()`. |
| `lblTitulo : Label` | `MouseEvent (onMouseClicked)` | `onVolver()` | `escena.setRoot(FXMLLoader.load(".../GestorClase.fxml"))` (misma ventana) y título «Tolotolo - Gestión de clases». `catch (IOException \| NullPointerException)` → `Registro.error(...)` + `Alert ERROR` «No se pudo volver al panel de clases.». |
| `btnCerrarSesion : Button` | `ActionEvent (onAction)` | `onCerrarSesion()` | Login en `Stage` nuevo «Tolotolo - Iniciar sesión» y cierre de la ventana. `catch` → `Registro.error(...)` + `Alert ERROR` «No se pudo volver al login.». |

**9.1. Diálogo de franja** (`Dialog<ButtonType>` del método `dialogoFranja(titulo, fMonitor, fAforo, conBorrar)`, contenido `GridPane`).

| Control (tipo) | Evento | Tratamiento |
|---|---|---|
| `fMonitor : ChoiceBox<Document>` | — (se lee al guardar) | Todos los monitores (`dao.listarMonitores()`), con `StringConverter` «nombre apellido»; en franja ocupada, preseleccionado el de la próxima sesión. |
| `fAforo : TextField` | — (se lee al guardar) | Aforo de la próxima sesión o, si no hay, el `aforoMaximo` de la sala. |
| `GUARDAR = new ButtonType("Guardar", ButtonData.OK_DONE)` | resultado de `Dialog.showAndWait()` | Crea la sesión (hueco libre) o aplica monitor y aforo a las sesiones futuras (hueco ocupado). |
| `BORRAR = new ButtonType("Borrar", ButtonData.LEFT)` (solo hueco ocupado) | resultado de `Dialog.showAndWait()` | Pide confirmación y quita la franja y sus sesiones futuras. |
| `ButtonType.CANCEL` / cierre | resultado de `showAndWait()` (`orElse(CANCEL)`) | No hace nada; se vuelve a pintar la rejilla. |

---

## 10. Visor del log

| | |
|---|---|
| FXML | `VisorLog.fxml` |
| Controlador | `VisorLogController` |
| La abre | `GerenteController.onVerLog()`: ventana nueva «Tolotolo - Log de operaciones» con `initOwner` = ventana del gerente, que sigue abierta |
| Rol | Gerente |

**Aspecto.** `BorderPane` con `txtFiltro : TextField` (prompt «Filtrar por usuario, operación, fecha o resultado»), `btnRecargar : Button` («Recargar»), `btnCerrar : Button` («Cerrar»), la tabla `tablaLog : TableView<String[]>` con `colFecha`, `colUsuario`, `colTipo` («Operación»), `colDetalle`, `colResultado` (`TableColumn<String[], String>`) y `lblTotal : Label` («N de M operaciones»).

**Estado inicial (`initialize()`).**
- `enlazar(columna, índice)`: `setCellValueFactory(Callback)` que devuelve `new ReadOnlyStringWrapper(fila[índice])` (0 fecha, 1 usuario, 2 tipo, 3 detalle, 4 resultado).
- `colResultado.setCellFactory(...)`: `TableCell` propia que pinta el texto en rojo (`-fx-text-fill: #c62828`) si empieza por «ERROR».
- `ChangeListener<String>` sobre `txtFiltro.textProperty()` → `filtrar()`.
- Primera carga con `onRecargar()`.

| Control (fx:id : tipo) | Evento (nombre técnico) | Método | Tratamiento |
|---|---|---|---|
| `btnRecargar : Button` | `ActionEvent (onAction)` (también se llama desde `initialize`) | `onRecargar()` | Vacía `filas`; `Registro.leerOperaciones()` (lee `datos/logs/operaciones.0.log` en UTF-8; lista vacía si no existe); cada línea no vacía se parte con `partir(linea)` (separador « \| »: fecha, usuario y tipo por delante, resultado el último y el resto es el detalle) y se invierte el orden (`Collections.reverse`) para ver primero lo más reciente. `catch (IOException)` → `Registro.error(...)` + `Alert ERROR` «No se pudo leer el log de operaciones.» (sin icono). Termina con `filtrar()`. |
| `txtFiltro : TextField` | `ChangeListener` sobre `textProperty()` | `filtrar()` | Pasa el texto a minúsculas y deja en `tablaLog` solo las filas cuyo contenido unido (`String.join(" ", fila)`) lo contiene; texto vacío = todas. Actualiza `lblTotal` con «<visibles> de <total> operaciones». |
| `btnCerrar : Button` | `ActionEvent (onAction)` | `onCerrar()` | Cierra la ventana del visor (`((Stage) btnCerrar.getScene().getWindow()).close()`). |

---

## Tratamientos comunes

### Log de la aplicación (`Registro`)

Clase de utilidades estática basada en `java.util.logging`. En su bloque `static` crea la carpeta `datos/logs` (relativa a donde se ejecuta la aplicación) con `Files.createDirectories` y tres `FileHandler` en UTF-8, en modo añadir, de 1 MB y 3 archivos rotativos (`%g`):

| Archivo | Logger | Qué guarda | Formato de cada línea |
|---|---|---|---|
| `general.%g.log` | `tolotolo` | `Registro.info(msg)` (INFO) y `Registro.aviso(msg)` (WARNING); un filtro deja fuera lo SEVERE | `yyyy-MM-dd HH:mm:ss [NIVEL] mensaje`; si hay excepción, ` -> Tipo: mensaje` |
| `errores.%g.log` | `tolotolo` | Solo `Registro.error(msg, Throwable)` (SEVERE) | `yyyy-MM-dd HH:mm:ss [SEVERE] mensaje` y la traza completa (`printStackTrace`) |
| `operaciones.%g.log` | `tolotolo.bd` (sin pasar al padre) | `Registro.operacion(tipo, detalle)` y `Registro.operacionError(tipo, detalle, causa)`, que llama la capa de datos en cada consulta | `fecha \| usuario \| tipo \| detalle \| resultado`; resultado `OK` o `ERROR -> TipoExcepción: mensaje`. El tipo es la operación y la colección (`SELECT Clientes`, `INSERT Sesiones`, `UPDATE Clientes/Sesiones`, `DELETE Clases`…). Los saltos de línea del detalle se cambian por espacios (`limpiar`) para que cada operación ocupe una línea |

El usuario de cada operación es el campo `Registro.usuario` («anónimo» hasta el login), que fija `Registro.setUsuario(mail)` en `LoginController.onIniciarSesion()`. `Registro.leerOperaciones()` devuelve las líneas de `operaciones.0.log` para el visor. `App.start` escribe «Aplicación iniciada» y `App.stop` «Aplicación cerrada».

### Errores de MongoDB → excepciones propias (`daoImplementacio.fallo(...)`)

Cada método del DAO envuelve sus llamadas al driver en `try { … } catch (MongoException e) { throw fallo(operación, detalle, e); }`. `fallo`:

1. Escribe la línea de error con `Registro.operacionError(operación, detalle, e)`.
2. Traduce la excepción del driver:

| Excepción o código de MongoDB | Excepción propia | Uso en la interfaz |
|---|---|---|
| `MongoTimeoutException` o `MongoSocketException` (servidor caído; `serverSelectionTimeoutMS=3000`) | `ConexionException` | «No se puede conectar con MongoDB…» |
| `ErrorCategory.fromErrorCode(code) == DUPLICATE_KEY` (clave duplicada en un índice único) | `DuplicadoException` | «Ya existe … con ese DNI o ese mail.» |
| código `121` (`ERROR_VALIDACION`: el documento no cumple el `$jsonSchema` de la colección) | `ValidacionException` | No hay ningún `catch` específico en los controladores: cae en el `catch (DatosException)` genérico de cada pantalla |
| cualquier otra | `DatosException` («Error en la base de datos: …») | Mensaje genérico de cada pantalla |

Las cuatro son `RuntimeException`, así que el controlador elige cuáles captura.

### Conexión

Un único `MongoClient` estático (`MongoClients.create("mongodb://localhost:27017/?serverSelectionTimeoutMS=3000")`), creado la primera vez que se usa `bd()` y cerrado con un `Runtime.addShutdownHook` al salir. Base de datos `Tolotolo`; colecciones `Clientes`, `Trabajadores`, `Clases`, `Salas` y `Sesiones`.

### Transacciones (`enTransaccion(Pasos<T>)`)

Las operaciones que tocan varias colecciones se escriben como `Pasos<T>` y se ejecutan con `enTransaccion`:

- Si el servidor admite transacciones (comprobado una vez con el comando `hello`: tiene `setName`, es decir, es un *replica set*), abre `conexion.startSession()` y ejecuta los pasos en `ClientSession.withTransaction(TransactionBody)`: todo o nada.
- Si es un servidor único, ejecuta los pasos seguidos sin transacción y deja un aviso en el log general («MongoDB sin replica set: …»).

Las usan `eliminarCliente` (quitar al cliente de `Sesiones.asistentes` y borrarlo de `Clientes`) y `eliminarClase` (borrar sus `Sesiones`, quitarla de `Clientes.clases` y borrarla de `Clases`). `apuntarseAClase` no usa transacción: reserva la plaza con un único `updateOne` condicionado (`$expr` con `$size(asistentes) < plazaMax` y que el cliente no esté ya) y, si después falla añadir la clase al cliente, devuelve la plaza con `$pull` (compensación).

### Cierre de sesión

Todas las pantallas con sesión tienen su botón «cerrar sesión», que abre `Login.fxml` en un `Stage` nuevo y cierra la ventana actual. Ninguno borra `LoginController.mailActual` / `rolActual` ni llama a `Registro.setUsuario(null)`: se sobrescriben en el siguiente login. Escriben línea en el log general Gerente («Sesión cerrada desde el panel del gerente»), Gestión de clientes y Gestión de monitores; Monitor, Cliente, Gestión de clases y Horario no la escriben. Cerrar la última ventana termina la aplicación (`App.stop()` → «Aplicación cerrada»; el *shutdown hook* cierra la conexión).

### Fotos de perfil (`GestorImagenes`)

El nombre del archivo es el DNI. `cargar(dni)` busca `datos/imagesUsu/<dni>.(png|jpg|jpeg|gif|bmp)` y, si no está, en los recursos `/com/mycompany/mongoquest/imagesUsu/`; devuelve `null` si no hay foto. `guardar(dni, File)` borra las anteriores y copia la nueva conservando su extensión. `renombrar(dniAnterior, dniNuevo)` mueve la foto si cambia el DNI. `borrar(dni)` elimina `datos/imagesUsu/<dni>.*`. Las tres últimas lanzan `IOException`, que tratan las pantallas de gestión.
