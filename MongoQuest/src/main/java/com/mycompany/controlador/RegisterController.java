package com.mycompany.controlador;

import com.mongodb.MongoException;
import com.mycompany.controlador.dao.daoImplementacio;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import org.bson.Document;

public class RegisterController {

    @FXML private BorderPane raiz;
    @FXML private ImageView imgToroIzq;
    @FXML private ImageView imgToroDer;
    @FXML private Label lblTitulo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellidos;
    @FXML private TextField txtDni;
    @FXML private TextField txtMail;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtPassword2;
    @FXML private ComboBox<String> cmbNivel;
    @FXML private Button btnPagar;
    @FXML private Hyperlink linkLogin;

    private final daoImplementacio dao = new daoImplementacio();

    @FXML
    private void initialize() {
        cmbNivel.getItems().addAll("Normal", "Premium");
        cmbNivel.getSelectionModel().selectFirst();

        // Los toros ocupan ~80% del alto disponible (ventana - cabecera de 81 px)
        imgToroIzq.fitHeightProperty().bind(raiz.heightProperty().subtract(81).multiply(0.80));
        imgToroDer.fitHeightProperty().bind(raiz.heightProperty().subtract(81).multiply(0.80));
    }

    @FXML
    private void onPagar() {
        String nombre = txtNombre.getText().trim();
        String apellidos = txtApellidos.getText().trim();
        String dni = txtDni.getText().trim().toUpperCase();
        String mail = txtMail.getText().trim().toLowerCase();
        String pass1 = txtPassword.getText();
        String pass2 = txtPassword2.getText();

        if (nombre.isEmpty() || apellidos.isEmpty() || dni.isEmpty() || mail.isEmpty()
                || pass1.isEmpty() || pass2.isEmpty() || cmbNivel.getValue() == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Rellena todos los campos.");
            return;
        }
        if (!dni.matches("\\d{8}[A-Z]")) {
            mostrarAlerta(Alert.AlertType.WARNING, "El DNI debe tener 8 números y una letra (ej. 12345678A).");
            return;
        }
        if (!dao.mailValido(mail)) {
            mostrarAlerta(Alert.AlertType.WARNING, "Escribe un mail válido (ej. nombre@gmail.com).");
            txtMail.requestFocus();
            return;
        }
        if (!pass1.equals(pass2)) {
            mostrarAlerta(Alert.AlertType.WARNING, "Las contraseñas no coinciden.");
            return;
        }

        try {
            if (dao.existeMail(mail)) {
                mostrarAlerta(Alert.AlertType.WARNING, "Ya existe un usuario con ese mail.");
                return;
            }

            // TODO: procesar el pago antes de insertar
            Document cliente = new Document()
                    .append("dni", dni)
                    .append("nombre", nombre)
                    .append("apellido", apellidos)
                    .append("fechaAlta", new Date())
                    .append("mail", mail)
                    .append("contraseña", pass1)
                    .append("membresia", cmbNivel.getValue().toLowerCase())
                    .append("logros", new ArrayList<Document>())
                    .append("clases", new ArrayList<Document>());

            dao.insertarCliente(cliente);
            mostrarAlerta(Alert.AlertType.INFORMATION, "Registro completado. Ya puedes iniciar sesión.");
            onVolverLogin();
        } catch (MongoException e) {
            mostrarAlerta(Alert.AlertType.ERROR,
                    "No se puede conectar con MongoDB. Comprueba que esté iniciado.");
        }
    }

    @FXML
    private void onVolverLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/mycompany/vista/Login.fxml"));
            raiz.getScene().setRoot(root);
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "No se pudo abrir la ventana de Login.");
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }
}