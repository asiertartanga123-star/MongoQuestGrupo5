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
import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * Controlador asociado a la vista del cliente.
 */
public class ClienteController implements Initializable {

    static final int MAX_CLASES_APUNTADO = 6;
    static final int CLASES_VISIBLES_AL_ABRIR = 3;

    @FXML private BorderPane raiz;

    @FXML private TextField txtNombre;
    @FXML private TextField txtDni;
    @FXML private TextField txtMail;
    @FXML private TextField txtMembresia;

    @FXML private Label lblClase1, lblClase2, lblClase3, lblClase4, lblClase5, lblClase6,
            lblClase7, lblClase8, lblClase9, lblClase10, lblClase11, lblClase12,
            lblClase13, lblClase14, lblClase15, lblClase16, lblClase17, lblClase18;
    @FXML private Label lblPlazas1, lblPlazas2, lblPlazas3, lblPlazas4, lblPlazas5, lblPlazas6,
            lblPlazas7, lblPlazas8, lblPlazas9, lblPlazas10, lblPlazas11, lblPlazas12,
            lblPlazas13, lblPlazas14, lblPlazas15, lblPlazas16, lblPlazas17, lblPlazas18;

    @FXML private VBox tarjeta10, tarjeta11, tarjeta12, tarjeta13, tarjeta14,
            tarjeta15, tarjeta16, tarjeta17, tarjeta18;

    @FXML private Button btnVerMasClases;

    @FXML private TableView<Logros> tablaLogros;
    @FXML private TableColumn<Logros, String> colLogro1, colLogro2, colLogro3, colLogro4;

    private final daoImplementacio dao;
    private List<Document> todasLasClases;

    public ClienteController() {
        this(new daoImplementacio());
    }

    ClienteController(daoImplementacio dao) {
        this.dao = dao;
    }

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

    private void ponerClick(Label nombre, Label plazas) {
        nombre.getParent().setCursor(javafx.scene.Cursor.HAND);
        nombre.getParent().setOnMouseClicked(e -> apuntar(nombre, plazas));
    }

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

    @FXML
    private void cerrarSesion() throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/com/mycompany/vista/Login.fxml"));
        Stage login = new Stage();
        login.setScene(new Scene(root));
        login.show();

        ((Stage) raiz.getScene().getWindow()).close();
    }

    Document cargarCliente(String mail) {
        Document cliente = dao.buscarClientePorMail(mail);
        if (cliente == null) {
            return null;
        }
        dao.revisarLogros(mail);
        return dao.buscarClientePorMail(mail);
    }

    String nombreCompleto(Document cliente) {
        return cliente.getString("nombre") + " " + cliente.getString("apellido");
    }

    String plazasDe(ObjectId idClase) {
        Document sesion = dao.ultimaSesionDeClase(idClase);
        if (sesion == null) {
            return "sin sesión";
        }
        List<Document> asistentes = sesion.getList("asistentes", Document.class);
        int totalAsistentes = asistentes == null ? 0 : asistentes.size();
        return totalAsistentes + "/" + sesion.getInteger("plazaMax");
    }

    String texto(Object valor) {
        if (valor == null) {
            return "";
        }
        return valor.toString();
    }

    List<Logros> construirLogros(List<Document> logros) {
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

    boolean hayMasClasesQueLasVisibles(List<Document> clases) {
        return clases.size() > CLASES_VISIBLES_AL_ABRIR;
    }

    boolean yaApuntado(String nombreClase, String[] clasesActuales) {
        for (String actual : clasesActuales) {
            if (nombreClase.equals(actual)) {
                return true;
            }
        }
        return false;
    }

    int primerHuecoLibre(String[] clasesActuales) {
        for (int i = 0; i < clasesActuales.length; i++) {
            if (clasesActuales[i] == null || clasesActuales[i].isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    private String[] textosDe(Label[] labels) {
        String[] textos = new String[labels.length];
        for (int i = 0; i < labels.length; i++) {
            textos[i] = labels[i].getText();
        }
        return textos;
    }
}
