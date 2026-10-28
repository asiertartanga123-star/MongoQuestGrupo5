package com.mycompany.controlador;

import com.mycompany.controlador.dao.daoImplementacio;
import com.mongodb.MongoException;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;

    private final daoImplementacio dao = new daoImplementacio();

    @FXML
    private void onIniciarSesion() {
        String mail = txtUsuario.getText().trim();
        String contraseña = txtPassword.getText().trim();

        if (mail.isEmpty() || contraseña.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Escribe el usuario y la contraseña.");
            return;
        }

        try {
            String rol = dao.validarLogin(mail, contraseña);

            if (rol == null) {
                mostrarAlerta(Alert.AlertType.ERROR, "Usuario o contraseña incorrectos.");
                return;
            }

            switch (rol) {
                case "gerente":
                    abrirVentana("/com/mycompany/vista/Gerente.fxml", "Tolotolo - Gerente");
                    break;
                case "monitor":
                    abrirVentana("/com/mycompany/vista/Monitor.fxml", "Tolotolo - Monitor");
                    break;
                case "cliente":
                    abrirVentana("/com/mycompany/vista/Cliente.fxml", "Tolotolo - Cliente");
                    break;
                default:
                    mostrarAlerta(Alert.AlertType.ERROR, "Tipo de usuario desconocido: " + rol);
            }
        } catch (MongoException e) {
            mostrarAlerta(Alert.AlertType.ERROR,
                    "No se puede conectar con MongoDB. Comprueba que esté iniciado.");
        }
    }

    private void abrirVentana(String fxml, String titulo) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage nueva = new Stage();
            nueva.setTitle(titulo);
            nueva.setScene(new Scene(root));
            nueva.show();

            // cierra la ventana de login
            ((Stage) txtUsuario.getScene().getWindow()).close();
        } catch (IOException | NullPointerException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "No se pudo abrir la ventana " + fxml);
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }
}