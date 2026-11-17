package com.mycompany.controlador;

import com.mycompany.controlador.dao.daoImplementacio;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import modelo.Logros;
<<<<<<< HEAD

=======
>>>>>>> Ekain
import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * Controlador asociado a la vista del cliente.
 */
public class ClienteController implements Initializable {

    /** Número máximo de clases a las que puede estar apuntado un cliente (huecos de la columna izquierda). */
    static final int MAX_CLASES_APUNTADO = 6;
    /** Clases del gimnasio visibles al abrir la pantalla; el resto aparece al pulsar el botón de ver más clases. */
    static final int CLASES_VISIBLES_AL_ABRIR = 3;

    /** Contenedor raíz de la pantalla. */
    @FXML private BorderPane raiz;

    /** Campo con el nombre completo del cliente. */
    @FXML private TextField txtNombre;
    /** Campo con el DNI del cliente. */
    @FXML private TextField txtDni;
    /** Campo con el correo del cliente. */
    @FXML private TextField txtMail;
    /** Campo con la membresía del cliente. */
    @FXML private TextField txtMembresia;

    /** Nombres de las clases. De la 1 a la 6, las clases a las que está apuntado el cliente; de la 7 a la 18, las clases disponibles en el gimnasio. */
    @FXML private Label lblClase1, lblClase2, lblClase3, lblClase4, lblClase5, lblClase6,
            lblClase7, lblClase8, lblClase9, lblClase10, lblClase11, lblClase12,
            lblClase13, lblClase14, lblClase15, lblClase16, lblClase17, lblClase18;
    /** Plazas de cada clase, con el formato asistentes/plazas máximas. Cada una corresponde a la etiqueta de nombre con el mismo número. */
    @FXML private Label lblPlazas1, lblPlazas2, lblPlazas3, lblPlazas4, lblPlazas5, lblPlazas6,
            lblPlazas7, lblPlazas8, lblPlazas9, lblPlazas10, lblPlazas11, lblPlazas12,
            lblPlazas13, lblPlazas14, lblPlazas15, lblPlazas16, lblPlazas17, lblPlazas18;

    /** Tarjetas de las clases 10 a 18, ocultas al abrir la pantalla hasta que se piden más clases. */
    @FXML private VBox tarjeta10, tarjeta11, tarjeta12, tarjeta13, tarjeta14,
            tarjeta15, tarjeta16, tarjeta17, tarjeta18;

    /** Botón para mostrar el resto de clases; se desactiva cuando ya no hay más que mostrar. */
    @FXML private Button btnVerMasClases;

    /** Tabla con los logros del cliente. */
    @FXML private TableView<Logros> tablaLogros;
    /** Columnas de la tabla de logros: número, nombre, fecha y semanas. */
    @FXML private TableColumn<Logros, String> colLogro1, colLogro2, colLogro3, colLogro4;

    /** Acceso a la base de datos. */
    private final daoImplementacio dao;
    /** Todas las clases del gimnasio, leídas al abrir la pantalla. */
    private List<Document> todasLasClases;

    /** Crea el controlador con el acceso a datos por defecto. */
    public ClienteController() {
        this(new daoImplementacio());
    }

<<<<<<< HEAD
    /**
     * Crea el controlador con un acceso a datos concreto; permite sustituirlo, por ejemplo en las pruebas.
     *
     * @param dao acceso a datos que se utilizará
     */
    ClienteController(daoImplementacio dao) {
=======
    // Lo usamos en los tests para pasarle un DAO falso (mock)
    public ClienteController(daoImplementacio dao) {
>>>>>>> Ekain
        this.dao = dao;
    }

    /**
     * Carga los datos del cliente en sesión: sus datos personales, las clases a las que está apuntado, las primeras clases del gimnasio y la tabla de logros. También deja clicable cada clase del gimnasio para poder apuntarse.
     *
     * @param url ubicación del FXML
     * @param rb recursos de la vista, si los hay
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Document cliente = cargarCliente(LoginController.mailActual);
        if (cliente == null) {
            return;
        }

        txtNombre.setText(nombreCompleto(cliente));
        txtDni.setText(texto(cliente.get("dni")));
        txtMail.setText(texto(cliente.get("mail")));
        txtMembresia.setText(texto(cliente.get("membresia")));

        Label[] nombres = {lblClase1, lblClase2, lblClase3, lblClase4, lblClase5, lblClase6};
        Label[] plazas = {lblPlazas1, lblPlazas2, lblPlazas3, lblPlazas4, lblPlazas5, lblPlazas6};

        List<Document> misClases = cliente.getList("clases", Document.class);
        if (misClases == null) {
            misClases = new ArrayList<>();
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
            logros = new ArrayList<>();
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
     * Hace clicable la tarjeta que contiene la etiqueta para apuntarse a esa clase.
     *
     * @param nombre etiqueta con el nombre de la clase
     * @param plazas etiqueta con las plazas de la clase
     */
    private void ponerClick(Label nombre, Label plazas) {
        nombre.getParent().setCursor(javafx.scene.Cursor.HAND);
        nombre.getParent().setOnMouseClicked(e -> apuntar(nombre, plazas));
    }

    /**
     * Apunta al cliente a la clase pulsada, tras pedir confirmación.
     *
     * <p>Avisa si ya está apuntado, si no le caben más clases o si no se puede guardar la inscripción.</p>
     *
     * @param nombre etiqueta con el nombre de la clase
     * @param plazas etiqueta con las plazas de la clase
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
            boolean guardado = dao.apuntarseAClase(LoginController.mailActual, idClase, nombre.getText());
            if (!guardado) {
                new Alert(Alert.AlertType.ERROR, "No se pudo guardar la inscripción.").showAndWait();
                return;
            }

            nombresIzq[hueco].setText(nombre.getText());
            plazasIzq[hueco].setText(plazas.getText());
        }
    }

    /** Muestra las tarjetas con el resto de clases del gimnasio y desactiva el botón. */
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
     * Abre la pantalla de login en una ventana nueva y cierra la actual.
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

<<<<<<< HEAD
    /**
     * Busca al cliente, revisa y concede los logros que le correspondan y vuelve a leerlo para devolver sus datos actualizados.
     *
     * @param mail correo del cliente
     * @return documento del cliente, o {@code null} si no existe
     */
    Document cargarCliente(String mail) {
=======
    // =====================================================================
    //  LÓGICA SIN VENTANA (sin Labels ni Alerts): es lo que se prueba en los tests
    // =====================================================================
    // Busca al cliente; si existe, revisa sus logros y lo vuelve a leer ya actualizado
    public Document cargarCliente(String mail) {
>>>>>>> Ekain
        Document cliente = dao.buscarClientePorMail(mail);
        if (cliente == null) {
            return null;
        }
        dao.revisarLogros(mail);
        return dao.buscarClientePorMail(mail);
    }

<<<<<<< HEAD
    /**
     * Une el nombre y el apellido del cliente.
     *
     * @param cliente documento del cliente
     * @return nombre completo
     */
    String nombreCompleto(Document cliente) {
        return cliente.getString("nombre") + " " + cliente.getString("apellido");
    }

    /**
     * Calcula el texto de plazas de la última sesión de una clase.
     *
     * @param idClase identificador de la clase
     * @return texto con el formato {@code asistentes/plazas}, o {@code "sin sesión"} si la clase no tiene sesiones
     */
    String plazasDe(ObjectId idClase) {
=======
    // "Nombre Apellido"
    public String nombreCompleto(Document cliente) {
        return cliente.getString("nombre") + " " + cliente.getString("apellido");
    }

    // El "n/n" de una clase: apuntados / plazas máximas de su última sesión
    public String plazasDe(ObjectId idClase) {
>>>>>>> Ekain
        Document sesion = dao.ultimaSesionDeClase(idClase);
        if (sesion == null) {
            return "sin sesión";
        }
        List<Document> asistentes = sesion.getList("asistentes", Document.class);
        int totalAsistentes = asistentes == null ? 0 : asistentes.size();
        return totalAsistentes + "/" + sesion.getInteger("plazaMax");
    }

<<<<<<< HEAD
    /**
     * Convierte un valor en texto sin fallar con nulos.
     *
     * @param valor valor a convertir; puede ser {@code null}
     * @return el valor como texto, o una cadena vacía si es {@code null}
     */
    String texto(Object valor) {
=======
    // Convierte un valor en texto (si no hay valor, deja el texto vacío)
    public String texto(Object valor) {
>>>>>>> Ekain
        if (valor == null) {
            return "";
        }
        return valor.toString();
    }

<<<<<<< HEAD
    /**
     * Convierte los logros guardados en MongoDB en filas para la tabla, numerándolos desde 1.
     *
     * @param logros lista de documentos de logros; puede ser {@code null}
     * @return lista de filas de la tabla (vacía si no hay logros)
     */
    List<Logros> construirLogros(List<Document> logros) {
=======
    // Convierte los logros de Mongo en filas para la tabla (numeradas desde 1)
    public List<Logros> construirLogros(List<Document> logros) {
>>>>>>> Ekain
        List<Logros> filas = new ArrayList<>();
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

<<<<<<< HEAD
    /**
     * Indica si hay más clases de las que se muestran al abrir la pantalla.
     *
     * @param clases todas las clases del gimnasio
     * @return {@code true} si hay clases por mostrar
     */
    boolean hayMasClasesQueLasVisibles(List<Document> clases) {
        return clases.size() > CLASES_VISIBLES_AL_ABRIR;
    }

    /**
     * Comprueba si el cliente ya está apuntado a una clase.
     *
     * @param nombreClase nombre de la clase
     * @param clasesActuales nombres de las clases a las que está apuntado
     * @return {@code true} si la clase ya figura en la lista
     */
    boolean yaApuntado(String nombreClase, String[] clasesActuales) {
=======
    // ¿Hay más clases que las que se ven al abrir la ventana?
    public boolean hayMasClasesQueLasVisibles(List<Document> clases) {
        return clases.size() > CLASES_VISIBLES_AL_ABRIR;
    }

    // ¿Ya está esa clase en la lista de la izquierda?
    public boolean yaApuntado(String nombreClase, String[] clasesActuales) {
>>>>>>> Ekain
        for (String actual : clasesActuales) {
            if (nombreClase.equals(actual)) {
                return true;
            }
        }
        return false;
    }

<<<<<<< HEAD
    /**
     * Busca el primer hueco libre entre las clases del cliente.
     *
     * @param clasesActuales nombres de las clases a las que está apuntado; un valor vacío o nulo es un hueco
     * @return posición del primer hueco, o -1 si no queda ninguno
     */
    int primerHuecoLibre(String[] clasesActuales) {
=======
    // Posición del primer hueco libre de la lista de la izquierda (-1 si está llena)
    public int primerHuecoLibre(String[] clasesActuales) {
>>>>>>> Ekain
        for (int i = 0; i < clasesActuales.length; i++) {
            if (clasesActuales[i] == null || clasesActuales[i].isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Lee el texto de cada etiqueta.
     *
     * @param labels etiquetas a leer
     * @return textos de las etiquetas, en el mismo orden
     */
    private String[] textosDe(Label[] labels) {
        String[] textos = new String[labels.length];
        for (int i = 0; i < labels.length; i++) {
            textos[i] = labels[i].getText();
        }
        return textos; 
    }
}
