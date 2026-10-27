package com.mycompany.controlador;

import com.mycompany.controlador.dao.daoImplementacio;
import com.mongodb.MongoException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;

    private final daoImplementacio dao = new daoImplementacio();

    @FXML
    private void onIniciarSesion() {
        String usuario = txtUsuario.getText().trim();
        String password = txtPassword.getText();

        if (usuario.isEmpty() || password.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Escribe el usuario y la contraseña.");
            return;
        }

        try {
            if (dao.validarLogin(usuario, password)) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Has iniciado sesión correctamente.");
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Usuario o contraseña incorrectos.");
            }
        } catch (MongoException e) {
            mostrarAlerta(Alert.AlertType.ERROR,
                    "No se puede conectar con MongoDB. Comprueba que esté iniciado.");
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }
}