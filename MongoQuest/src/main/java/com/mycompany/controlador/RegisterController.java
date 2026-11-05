package com.mycompany.controlador;

import com.mongodb.ErrorCategory;
import com.mongodb.MongoException;
import com.mongodb.MongoWriteException;
import com.mycompany.controlador.dao.daoImplementacio;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import org.bson.Document;

public class RegisterController {

    @FXML private BorderPane raiz;
    @FXML private ImageView imgToroIzq;
    @FXML private ImageView imgToroDer;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellidos;
    @FXML private TextField txtDni;
    @FXML private TextField txtMail;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtPassword2;
    @FXML private ComboBox<String> cmbNivel;

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

        if (nombre.isEmpty() || apellidos.isEmpty() || dni.isEmpty() || mail.isEmpty() || pass1.isEmpty() || pass2.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Rellena todos los campos.");
            return;
        }
        if (!dni.matches("\\d{8}[A-Z]")) {
            Registro.aviso("Registro rechazado, DNI con formato incorrecto: " + dni);
            mostrarAlerta(Alert.AlertType.WARNING, "El DNI debe tener 8 números y una letra (ej. 12345678A).");
            return;
        }
        if (!dao.mailValido(mail)) {
            Registro.aviso("Registro rechazado, mail no válido: " + mail);
            mostrarAlerta(Alert.AlertType.WARNING, "Escribe un mail válido (ej. nombre@gmail.com).");
            txtMail.requestFocus();
            return;
        }
        if (!pass1.equals(pass2)) {
            Registro.aviso("Registro rechazado, las contraseñas no coinciden: " + mail);
            mostrarAlerta(Alert.AlertType.WARNING, "Las contraseñas no coinciden.");
            return;
        }

        try {
            if (dao.existeMail(mail)) {
                Registro.aviso("Registro rechazado, el mail ya existe: " + mail);
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
            Registro.info("Registro completado: " + mail + " (" + dni + ", "
                    + cmbNivel.getValue().toLowerCase() + ")");
            mostrarAlerta(Alert.AlertType.INFORMATION, "Registro completado. Ya puedes iniciar sesión.");
            onVolverLogin();
        } catch (MongoWriteException e) {
            if (e.getError().getCategory() == ErrorCategory.DUPLICATE_KEY) {
                Registro.aviso("Registro rechazado, DNI o mail duplicado: " + dni + " / " + mail);
                mostrarAlerta(Alert.AlertType.WARNING, "Ya existe un usuario con ese DNI o ese mail.");
            } else {
                Registro.error("No se pudo guardar el registro de " + mail, e);
                mostrarAlerta(Alert.AlertType.ERROR, "No se pudo completar el registro.");
            }
        } catch (MongoException e) {
            Registro.error("No se pudo conectar con MongoDB durante el registro de " + mail, e);
            mostrarAlerta(Alert.AlertType.ERROR,
                    "No se puede conectar con MongoDB. Comprueba que esté iniciado.");
        }
    }

    @FXML
    private void onVolverLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/mycompany/vista/Login.fxml"));
            raiz.getScene().setRoot(root);
            Registro.info("Vuelta al login desde el registro");
        } catch (IOException | NullPointerException e) {
            Registro.error("No se pudo volver al login desde el registro", e);
            mostrarAlerta(Alert.AlertType.ERROR, "No se pudo abrir la ventana de Login.");
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje);
        App.ponerIcono(alerta);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }
}