package com.mycompany.controlador;

import com.mycompany.controlador.dao.daoImplementacio;
import java.net.URL;
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

public class ClienteController implements Initializable {

    // ---------- Lo que hay en la ventana (son los fx:id del FXML) ----------
    @FXML private BorderPane raiz;

    // Datos del cliente (recuadro de la derecha)
    @FXML private TextField txtNombre;
    @FXML private TextField txtDni;
    @FXML private TextField txtMail;
    @FXML private TextField txtMembresia;

    // Tarjetas 1 a 6   = columna izquierda (clases a las que está apuntado)
    // Tarjetas 7 a 9   = columna central (clases creadas)
    // Tarjetas 10 a 18 = columna central, ocultas hasta pulsar "Ver más clases"
    @FXML private Label lblClase1, lblClase2, lblClase3, lblClase4, lblClase5, lblClase6,
                        lblClase7, lblClase8, lblClase9, lblClase10, lblClase11, lblClase12,
                        lblClase13, lblClase14, lblClase15, lblClase16, lblClase17, lblClase18;
    @FXML private Label lblPlazas1, lblPlazas2, lblPlazas3, lblPlazas4, lblPlazas5, lblPlazas6,
                        lblPlazas7, lblPlazas8, lblPlazas9, lblPlazas10, lblPlazas11, lblPlazas12,
                        lblPlazas13, lblPlazas14, lblPlazas15, lblPlazas16, lblPlazas17, lblPlazas18;

    // Las tarjetas ocultas (el FXML las tiene con visible="false")
    @FXML private VBox tarjeta10, tarjeta11, tarjeta12, tarjeta13, tarjeta14,
                       tarjeta15, tarjeta16, tarjeta17, tarjeta18;

    @FXML private Button btnVerMasClases;

    // La tabla de logros
    @FXML private TableView<Logros> tablaLogros;
    @FXML private TableColumn<Logros, String> colLogro1, colLogro2, colLogro3, colLogro4;

    // La conexión a la base de datos
    private final daoImplementacio dao = new daoImplementacio();

    // Todas las clases creadas (las guardamos para el botón "Ver más clases")
    private List<Document> todasLasClases;

    // ---------- Al abrir la ventana ----------
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // 1. Buscamos al cliente que ha iniciado sesión
        Document cliente = dao.buscarClientePorMail(LoginController.mailActual);
        if (cliente == null) {
            return;
        }

        // 2. Datos del cliente
        txtNombre.setText(cliente.getString("nombre") + " " + cliente.getString("apellido"));
        txtDni.setText(texto(cliente.get("dni")));
        txtMail.setText(texto(cliente.get("mail")));
        txtMembresia.setText(texto(cliente.get("membresia")));

        // 3. Columna izquierda: las clases a las que está apuntado (máximo 6)
        Label[] nombres = {lblClase1, lblClase2, lblClase3, lblClase4, lblClase5, lblClase6};
        Label[] plazas = {lblPlazas1, lblPlazas2, lblPlazas3, lblPlazas4, lblPlazas5, lblPlazas6};

        List<Document> misClases = cliente.getList("clases", Document.class);
        for (int i = 0; i < misClases.size() && i < 6; i++) {
            Document clase = misClases.get(i);
            nombres[i].setText(texto(clase.get("nombre")));
            plazas[i].setText(plazasDe(clase.getObjectId("id_clase")));
        }

        // 4. Columna central: las clases creadas (las 3 primeras)
        Label[] nombresCentro = {lblClase7, lblClase8, lblClase9};
        Label[] plazasCentro = {lblPlazas7, lblPlazas8, lblPlazas9};

        todasLasClases = dao.listarClases();
        for (int i = 0; i < todasLasClases.size() && i < 3; i++) {
            Document clase = todasLasClases.get(i);
            nombresCentro[i].setText(texto(clase.get("nombre")));
            plazasCentro[i].setText(plazasDe(clase.getObjectId("_id")));
        }

        // Si no hay más de 3 clases, no hay nada que desplegar
        if (todasLasClases.size() <= 3) {
            btnVerMasClases.setDisable(true);
        }

        // 5. Tabla de logros
        colLogro1.setCellValueFactory(new PropertyValueFactory<Logros, String>("numero"));
        colLogro2.setCellValueFactory(new PropertyValueFactory<Logros, String>("nombre"));
        colLogro3.setCellValueFactory(new PropertyValueFactory<Logros, String>("fecha"));
        colLogro4.setCellValueFactory(new PropertyValueFactory<Logros, String>("semanas"));

        List<Document> logros = cliente.getList("logros", Document.class);
        for (int i = 0; i < logros.size(); i++) {
            Document logro = logros.get(i);
            tablaLogros.getItems().add(new Logros(String.valueOf(i + 1),
                    texto(logro.get("nombre")),
                    texto(logro.get("fecha")),
                    texto(logro.get("semanas"))));
        }

        // 6. Al pulsar una tarjeta de clase del centro, se puede apuntar
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

    // Hace clicable TODA la tarjeta (no solo el texto) y pone la manita al pasar el ratón
    private void ponerClick(Label nombre, Label plazas) {
        nombre.getParent().setCursor(javafx.scene.Cursor.HAND);
        nombre.getParent().setOnMouseClicked(e -> apuntar(nombre, plazas));
    }

    // ---------- Pulsar una clase: pregunta y la pone en la lista de la izquierda ----------
    private void apuntar(Label nombre, Label plazas) {
        Label[] nombresIzq = {lblClase1, lblClase2, lblClase3, lblClase4, lblClase5, lblClase6};
        Label[] plazasIzq = {lblPlazas1, lblPlazas2, lblPlazas3, lblPlazas4, lblPlazas5, lblPlazas6};

        if (nombre.getText().isEmpty()) {
            return;   // tarjeta sin clase
        }

        // ¿Ya está apuntado?
        for (int i = 0; i < 6; i++) {
            if (nombresIzq[i].getText().equals(nombre.getText())) {
                new Alert(Alert.AlertType.INFORMATION,
                        "Ya estás apuntado a " + nombre.getText()).showAndWait();
                return;
            }
        }

        // Preguntamos
        Alert pregunta = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Quieres apuntarte a " + nombre.getText() + "?", ButtonType.YES, ButtonType.NO);
        pregunta.setTitle("Apuntarse a una clase");
        pregunta.setHeaderText(null);
        Optional<ButtonType> respuesta = pregunta.showAndWait();

        if (respuesta.isPresent() && respuesta.get() == ButtonType.YES) {
            // Lo ponemos en el primer hueco libre de la izquierda
            for (int i = 0; i < 6; i++) {
                if (nombresIzq[i].getText().isEmpty()) {
                    nombresIzq[i].setText(nombre.getText());
                    plazasIzq[i].setText(plazas.getText());
                    return;
                }
            }
            new Alert(Alert.AlertType.WARNING, "Ya tienes 6 clases, no caben más.").showAndWait();
        }
    }

    // ---------- Botón "Ver más clases": enseña las tarjetas ocultas ----------
    @FXML
    private void verMasClases() {
        VBox[] tarjetas = {tarjeta10, tarjeta11, tarjeta12, tarjeta13, tarjeta14,
                           tarjeta15, tarjeta16, tarjeta17, tarjeta18};
        Label[] nombres = {lblClase10, lblClase11, lblClase12, lblClase13, lblClase14,
                           lblClase15, lblClase16, lblClase17, lblClase18};
        Label[] plazas = {lblPlazas10, lblPlazas11, lblPlazas12, lblPlazas13, lblPlazas14,
                          lblPlazas15, lblPlazas16, lblPlazas17, lblPlazas18};

        // La clase número 4 en adelante va en las tarjetas ocultas
        for (int i = 0; i < tarjetas.length && i + 3 < todasLasClases.size(); i++) {
            Document clase = todasLasClases.get(i + 3);
            nombres[i].setText(texto(clase.get("nombre")));
            plazas[i].setText(plazasDe(clase.getObjectId("_id")));

            tarjetas[i].setVisible(true);
            tarjetas[i].setManaged(true);
        }

        btnVerMasClases.setDisable(true);   // ya está todo desplegado
    }

    // ---------- Botón "cerrar sesion" ----------
    @FXML
    private void cerrarSesion() throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/com/mycompany/vista/Login.fxml"));
        Stage login = new Stage();
        login.setScene(new Scene(root));
        login.show();

        ((Stage) raiz.getScene().getWindow()).close();
    }

    // ---------- Ayudas pequeñas ----------

    // El "n/n" de una clase: apuntados / plazas máximas de su última sesión
    private String plazasDe(ObjectId idClase) {
        Document sesion = dao.ultimaSesionDeClase(idClase);
        if (sesion == null) {
            return "sin sesión";
        }
        List<Document> asistentes = sesion.getList("asistentes", Document.class);
        return asistentes.size() + "/" + sesion.getInteger("plazaMax");
    }

    // Convierte un valor en texto (si no hay valor, deja el texto vacío)
    private String texto(Object valor) {
        if (valor == null) {
            return "";
        }
        return valor.toString();
    }
}