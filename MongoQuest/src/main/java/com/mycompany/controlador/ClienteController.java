package com.mycompany.controlador;

import com.mycompany.controlador.dao.ConexionException;
import com.mycompany.controlador.dao.DatosException;
import com.mycompany.controlador.dao.daoImplementacio;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import modelo.Logros;
import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * Controlador asociado a la vista del cliente.
 */
public class ClienteController implements Initializable {

    /** Número máximo de clases a las que se puede apuntar un cliente. */
    static final int MAX_CLASES_APUNTADO = 6;
    /** Clases disponibles que se muestran al abrir; el resto aparece con «Ver más clases». */
    static final int CLASES_VISIBLES_AL_ABRIR = 3;

    /** Contenedor raíz de la pantalla. */
    @FXML private BorderPane raiz;
    /** Círculo donde se muestra la foto del cliente. */
    @FXML private Circle circuloFoto;

    /** Nombre y apellido del cliente (solo lectura). */
    @FXML private TextField txtNombre;
    /** DNI del cliente (solo lectura). */
    @FXML private TextField txtDni;
    /** Correo del cliente (solo lectura). */
    @FXML private TextField txtMail;
    /** Tipo de membresía del cliente (solo lectura). */
    @FXML private TextField txtMembresia;

    /** Nombres de las clases: del 1 al 6, las del cliente; del 7 en adelante, las disponibles. */
    @FXML private Label lblClase1, lblClase2, lblClase3, lblClase4, lblClase5, lblClase6,
            lblClase7, lblClase8, lblClase9, lblClase10, lblClase11, lblClase12,
            lblClase13, lblClase14, lblClase15, lblClase16, lblClase17, lblClase18;
    /** Plazas ocupadas / plazas totales de cada clase, en el mismo orden que los nombres. */
    @FXML private Label lblPlazas1, lblPlazas2, lblPlazas3, lblPlazas4, lblPlazas5, lblPlazas6,
            lblPlazas7, lblPlazas8, lblPlazas9, lblPlazas10, lblPlazas11, lblPlazas12,
            lblPlazas13, lblPlazas14, lblPlazas15, lblPlazas16, lblPlazas17, lblPlazas18;

    /** Tarjetas de clases disponibles que están ocultas hasta pulsar «Ver más clases». */
    @FXML private VBox tarjeta10, tarjeta11, tarjeta12, tarjeta13, tarjeta14,
            tarjeta15, tarjeta16, tarjeta17, tarjeta18;

    /** Botón que muestra el resto de clases disponibles. */
    @FXML private Button btnVerMasClases;

    /** Tabla con los logros del cliente. */
    @FXML private TableView<Logros> tablaLogros;
    /** Columnas de la tabla de logros: número, nombre, fecha y semanas. */
    @FXML private TableColumn<Logros, String> colLogro1, colLogro2, colLogro3, colLogro4;

    /** Acceso a los datos de MongoDB. */
    private final daoImplementacio dao;
    /** Todas las clases del gimnasio, leídas al abrir la pantalla. */
    private List<Document> todasLasClases;

    /** Crea el controlador con el acceso a datos real; es el que usa JavaFX al cargar el FXML. */
    public ClienteController() {
        this(new daoImplementacio());
    }

    /**
     * Crea el controlador con el acceso a datos indicado; se usa en los tests.
     *
     * @param dao acceso a datos que usará el controlador
     */
    public ClienteController(daoImplementacio dao) {
        this.dao = dao;
    }

    /**
     * Carga los datos del cliente al abrir la pantalla. Si MongoDB falla, la pantalla se abre
     * vacía y, en cuanto se ve, avisa del error (un aviso modal dentro de la carga del FXML
     * bloquearía la apertura).
     *
     * @param url no se usa
     * @param rb no se usa
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            cargarDatos();
        } catch (ConexionException e) {
            Registro.error("No se pudo conectar con MongoDB al abrir la pantalla del cliente", e);
            avisarTrasAbrir("No se puede conectar con MongoDB. Comprueba que esté iniciado.");
        } catch (DatosException e) {
            Registro.error("No se pudieron cargar los datos del cliente " + LoginController.mailActual, e);
            avisarTrasAbrir("No se pudieron cargar tus datos.");
        }
    }

    /**
     * Muestra un aviso de error cuando la pantalla ya está abierta.
     *
     * @param mensaje texto del aviso
     */
    private void avisarTrasAbrir(final String mensaje) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Alert a = new Alert(Alert.AlertType.ERROR, mensaje);
                a.setHeaderText(null);
                a.showAndWait();
            }
        });
    }

    /** Rellena la ficha, las clases del cliente, las clases del centro y los logros. */
    private void cargarDatos() {
        Document cliente = cargarCliente(LoginController.mailActual);
        if (cliente == null) {
            return;
        }

        String dni = texto(cliente.get("dni"));
        txtNombre.setText(nombreCompleto(cliente));
        txtDni.setText(dni);
        txtMail.setText(texto(cliente.get("mail")));
        txtMembresia.setText(texto(cliente.get("membresia")));

        cargarFotoPerfil(dni);

        Label[] nombres = {lblClase1, lblClase2, lblClase3, lblClase4, lblClase5, lblClase6};
        Label[] plazas = {lblPlazas1, lblPlazas2, lblPlazas3, lblPlazas4, lblPlazas5, lblPlazas6};

        List<Document> misClases = cliente.getList("clases", Document.class);
        if (misClases == null) {
            misClases = new ArrayList<Document>();
        }
        for (int i = 0; i < misClases.size() && i < MAX_CLASES_APUNTADO; i++) {
            Document clase = misClases.get(i);
            nombres[i].setText(texto(clase.get("nombre")));
            plazas[i].setText(plazasDe(clase.getObjectId("id_clase")));
        }

        Label[] nombresCentro = {lblClase7, lblClase8, lblClase9};
        Label[] plazasCentro = {lblPlazas7, lblPlazas8, lblPlazas9};

        todasLasClases = dao.listarClases();
        for (int i = 0; i < todasLasClases.size() && i < CLASES_VISIBLES_AL_ABRIR; i++) {
            Document clase = todasLasClases.get(i);
            nombresCentro[i].setText(texto(clase.get("nombre")));
            nombresCentro[i].setUserData(clase.getObjectId("_id"));
            plazasCentro[i].setText(plazasDe(clase.getObjectId("_id")));
        }

        if (!hayMasClasesQueLasVisibles(todasLasClases)) {
            btnVerMasClases.setDisable(true);
        }

        colLogro1.setCellValueFactory(new PropertyValueFactory<Logros, String>("numero"));
        colLogro2.setCellValueFactory(new PropertyValueFactory<Logros, String>("nombre"));
        colLogro3.setCellValueFactory(new PropertyValueFactory<Logros, String>("fecha"));
        colLogro4.setCellValueFactory(new PropertyValueFactory<Logros, String>("semanas"));

        List<Document> logros = cliente.getList("logros", Document.class);
        if (logros == null) {
            logros = new ArrayList<Document>();
        }
        tablaLogros.getItems().addAll(construirLogros(logros));

        ponerClick(lblClase7, lblPlazas7);
        ponerClick(lblClase8, lblPlazas8);
        ponerClick(lblClase9, lblPlazas9);
        ponerClick(lblClase10, lblPlazas10);
        ponerClick(lblClase11, lblPlazas11);
        ponerClick(lblClase12, lblPlazas12);
        ponerClick(lblClase13, lblPlazas13);
        ponerClick(lblClase14, lblPlazas14);
        ponerClick(lblClase15, lblPlazas15);
        ponerClick(lblClase16, lblPlazas16);
        ponerClick(lblClase17, lblPlazas17);
        ponerClick(lblClase18, lblPlazas18);
    }

    /**
     * Pone la foto del cliente en el círculo de perfil, si tiene foto.
     *
     * @param dni DNI del cliente
     */
    private void cargarFotoPerfil(String dni) {
        if (circuloFoto == null || dni == null || dni.isEmpty()) {
            return;
        }
        Image foto = GestorImagenes.cargar(dni);
        if (foto != null && !foto.isError()) {
            circuloFoto.setFill(new ImagePattern(foto));
        }
    }

    /**
     * Hace que al pulsar la tarjeta de una clase se pregunte si apuntarse a ella.
     *
     * @param nombre etiqueta con el nombre de la clase
     * @param plazas etiqueta con sus plazas
     */
    private void ponerClick(final Label nombre, final Label plazas) {
        nombre.getParent().setCursor(Cursor.HAND);
        nombre.getParent().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                apuntar(nombre, plazas);
            }
        });
    }

    /**
     * Apunta al cliente a la clase pulsada después de confirmarlo. Avisa si ya está
     * apuntado, si tiene 6 clases o si la sesión está completa, y actualiza las plazas.
     *
     * @param nombre etiqueta con el nombre de la clase
     * @param plazas etiqueta con sus plazas
     */
    private void apuntar(Label nombre, Label plazas) {
        Label[] nombresIzq = {lblClase1, lblClase2, lblClase3, lblClase4, lblClase5, lblClase6};
        Label[] plazasIzq = {lblPlazas1, lblPlazas2, lblPlazas3, lblPlazas4, lblPlazas5, lblPlazas6};

        if (nombre.getText().isEmpty()) {
            return;
        }

        String[] actuales = textosDe(nombresIzq);

        if (yaApuntado(nombre.getText(), actuales)) {
            new Alert(Alert.AlertType.INFORMATION,
                    "Ya estás apuntado a " + nombre.getText()).showAndWait();
            return;
        }

        Alert pregunta = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Quieres apuntarte a " + nombre.getText() + "?", ButtonType.YES, ButtonType.NO);
        pregunta.setTitle("Apuntarse a una clase");
        pregunta.setHeaderText(null);
        Optional<ButtonType> respuesta = pregunta.showAndWait();

        if (respuesta.isPresent() && respuesta.get() == ButtonType.YES) {
            int hueco = primerHuecoLibre(actuales);
            if (hueco == -1) {
                new Alert(Alert.AlertType.WARNING, "Ya tienes 6 clases, no caben más.").showAndWait();
                return;
            }

            ObjectId idClase = (ObjectId) nombre.getUserData();
            if (idClase == null) {
                return;
            }
            boolean guardado;
            try {
                guardado = dao.apuntarseAClase(LoginController.mailActual, idClase, nombre.getText());
            } catch (ConexionException e) {
                Registro.error("No se pudo conectar con MongoDB al apuntar a " + LoginController.mailActual
                        + " a " + nombre.getText(), e);
                new Alert(Alert.AlertType.ERROR, "No se puede conectar con MongoDB. No te has apuntado a "
                        + nombre.getText() + ".").showAndWait();
                return;
            } catch (DatosException e) {
                Registro.error("No se pudo apuntar a " + LoginController.mailActual + " a " + nombre.getText(), e);
                new Alert(Alert.AlertType.ERROR, "No se pudo apuntar a " + nombre.getText()
                        + " por un error de la base de datos.").showAndWait();
                return;
            }
            // Se vuelve a leer la sesión: el contador cambia si entró o si otro llenó la sala.
            String plazasAhora = plazasDe(idClase);
            plazas.setText(plazasAhora);
            if (!guardado) {
                new Alert(Alert.AlertType.WARNING, "No se pudo apuntar a " + nombre.getText()
                        + ": la clase está completa (" + plazasAhora + ") o hubo un error al guardar.").showAndWait();
                return;
            }

            nombresIzq[hueco].setText(nombre.getText());
            plazasIzq[hueco].setText(plazasAhora);
        }
    }

    /** Muestra las tarjetas del resto de clases disponibles y desactiva el botón. */
    @FXML
    private void verMasClases() {
        VBox[] tarjetas = {tarjeta10, tarjeta11, tarjeta12, tarjeta13, tarjeta14,
            tarjeta15, tarjeta16, tarjeta17, tarjeta18};
        Label[] nombres = {lblClase10, lblClase11, lblClase12, lblClase13, lblClase14,
            lblClase15, lblClase16, lblClase17, lblClase18};
        Label[] plazas = {lblPlazas10, lblPlazas11, lblPlazas12, lblPlazas13, lblPlazas14,
            lblPlazas15, lblPlazas16, lblPlazas17, lblPlazas18};

        for (int i = 0; i < tarjetas.length && i + CLASES_VISIBLES_AL_ABRIR < todasLasClases.size(); i++) {
            Document clase = todasLasClases.get(i + CLASES_VISIBLES_AL_ABRIR);
            nombres[i].setText(texto(clase.get("nombre")));
            nombres[i].setUserData(clase.getObjectId("_id"));
            plazas[i].setText(plazasDe(clase.getObjectId("_id")));

            tarjetas[i].setVisible(true);
            tarjetas[i].setManaged(true);
        }

        btnVerMasClases.setDisable(true);
    }

    /**
     * Cierra la sesión y vuelve a la pantalla de login.
     *
     * @throws Exception si no se puede cargar la pantalla de login
     */
    @FXML
    private void cerrarSesion() throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/com/mycompany/vista/Login.fxml"));
        Stage login = new Stage();
        login.setScene(new Scene(root));
        login.show();

        ((Stage) raiz.getScene().getWindow()).close();
    }

    /**
     * Lee el cliente de MongoDB y antes le concede los logros que le correspondan por antigüedad.
     *
     * @param mail correo del cliente
     * @return el documento del cliente, o {@code null} si no existe
     */
    public Document cargarCliente(String mail) {
        Document cliente = dao.buscarClientePorMail(mail);
        if (cliente == null) {
            return null;
        }
        dao.revisarLogros(mail);
        return dao.buscarClientePorMail(mail);
    }

    /**
     * Une nombre y apellido del cliente.
     *
     * @param cliente documento del cliente
     * @return nombre y apellido separados por un espacio
     */
    public String nombreCompleto(Document cliente) {
        return cliente.getString("nombre") + " " + cliente.getString("apellido");
    }

    /**
     * Calcula las plazas ocupadas de la última sesión de una clase.
     *
     * @param idClase identificador de la clase
     * @return texto {@code ocupadas/totales}, o {@code sin sesión} si la clase no tiene sesiones
     */
    public String plazasDe(ObjectId idClase) {
        Document sesion = dao.ultimaSesionDeClase(idClase);
        if (sesion == null) {
            return "sin sesión";
        }
        List<Document> asistentes = sesion.getList("asistentes", Document.class);
        int totalAsistentes;
        if (asistentes == null) {
            totalAsistentes = 0;
        } else {
            totalAsistentes = asistentes.size();
        }
        return totalAsistentes + "/" + sesion.getInteger("plazaMax");
    }

    /**
     * Convierte un valor leído de MongoDB en texto para mostrarlo.
     *
     * @param valor valor a mostrar
     * @return su texto, o una cadena vacía si es {@code null}
     */
    public String texto(Object valor) {
        if (valor == null) {
            return "";
        }
        return valor.toString();
    }

    /**
     * Convierte los logros guardados en el cliente en filas para la tabla.
     *
     * @param logros logros del cliente; puede ser {@code null}
     * @return una fila por logro, numeradas desde 1
     */
    public List<Logros> construirLogros(List<Document> logros) {
        List<Logros> filas = new ArrayList<Logros>();
        if (logros == null) {
            return filas;
        }
        for (int i = 0; i < logros.size(); i++) {
            Document logro = logros.get(i);
            filas.add(new Logros(String.valueOf(i + 1),
                    texto(logro.get("nombre")),
                    texto(logro.get("fecha")),
                    texto(logro.get("semanas"))));
        }
        return filas;
    }

    /**
     * Indica si hay más clases de las que se muestran al abrir.
     *
     * @param clases todas las clases
     * @return {@code true} si hay que activar «Ver más clases»
     */
    public boolean hayMasClasesQueLasVisibles(List<Document> clases) {
        return clases.size() > CLASES_VISIBLES_AL_ABRIR;
    }

    /**
     * Indica si el cliente ya está apuntado a una clase.
     *
     * @param nombreClase    nombre de la clase
     * @param clasesActuales nombres de las clases del cliente
     * @return {@code true} si la clase ya está en su lista
     */
    public boolean yaApuntado(String nombreClase, String[] clasesActuales) {
        for (int i = 0; i < clasesActuales.length; i++) {
            if (nombreClase.equals(clasesActuales[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Busca la primera posición libre en la lista de clases del cliente.
     *
     * @param clasesActuales nombres de las clases del cliente; vacío o {@code null} es un hueco
     * @return la posición libre, o {@code -1} si no queda ninguna
     */
    public int primerHuecoLibre(String[] clasesActuales) {
        for (int i = 0; i < clasesActuales.length; i++) {
            if (clasesActuales[i] == null || clasesActuales[i].isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Devuelve el texto de cada etiqueta.
     *
     * @param labels etiquetas
     * @return sus textos, en el mismo orden
     */
    private String[] textosDe(Label[] labels) {
        String[] textos = new String[labels.length];
        for (int i = 0; i < labels.length; i++) {
            textos[i] = labels[i].getText();
        }
        return textos;
    }
}