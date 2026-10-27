package com.mycompany.controlador;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML private Label lblTitulo;
    @FXML private TextField txtMail;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogin;
    @FXML private Hyperlink linkRegistro;

    @FXML
    private void onIniciarSesion() {
        String mail = txtMail.getText().trim();
        String password = txtPassword.getText();

        if (mail.isEmpty() || password.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Rellena el mail y la contraseña.");
            return;
        }

        // TODO: comprobar las credenciales contra MongoDB
        System.out.println("Login con: " + mail);
    }

    @FXML
    private void onRegistrarse() throws IOException {
     
    
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }
}