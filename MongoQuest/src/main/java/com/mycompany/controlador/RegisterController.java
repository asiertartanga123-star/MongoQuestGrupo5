package com.mycompany.controlador;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

public class RegisterController {

    @FXML private BorderPane raiz;
    @FXML private ImageView imgToroIzq;
    @FXML private ImageView imgToroDer;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellidos;
    @FXML private TextField txtMail;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtPassword2;
    @FXML private ComboBox<String> cmbNivel;

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
        String mail = txtMail.getText().trim();
        String pass1 = txtPassword.getText();
        String pass2 = txtPassword2.getText();

        if (nombre.isEmpty() || apellidos.isEmpty() || mail.isEmpty() || pass1.isEmpty() || pass2.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Rellena todos los campos.");
            return;
        }
        if (!pass1.equals(pass2)) {
            mostrarAlerta(Alert.AlertType.WARNING, "Las contraseñas no coinciden.");
            return;
        }

        // TODO: procesar el pago e insertar el cliente con dao.insertarCliente(...)
        // (nivel elegido: cmbNivel.getValue())
        mostrarAlerta(Alert.AlertType.INFORMATION, "Validación correcta (pendiente de guardar en MongoDB).");
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