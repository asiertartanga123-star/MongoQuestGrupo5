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
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class LoginController {

    // Elementos de la vista enlazados por sus fx:id en Login.fxml.
    @FXML private BorderPane raiz;
    @FXML private ImageView imgToroIzq;
    @FXML private ImageView imgToroDer;
    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;

    // El controlador consulta el DAO para validar las credenciales.
    private final daoImplementacio dao = new daoImplementacio();

    @FXML
    private void initialize() {
        // Ajusta la altura de las ilustraciones al espacio que queda bajo la cabecera.
        imgToroIzq.fitHeightProperty().bind(raiz.heightProperty().subtract(81).multiply(0.85));
        imgToroDer.fitHeightProperty().bind(raiz.heightProperty().subtract(81).multiply(0.85));
    }

    @FXML
    private void onIniciarSesion() {
        // Limpia espacios accidentales antes de consultar la base de datos.
        String mail = txtUsuario.getText().trim();
        String contraseña = txtPassword.getText().trim();

        if (mail.isEmpty() || contraseña.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Escribe el usuario y la contraseña.");
            return;
        }

        try {
            String rol = dao.validarLogin(mail, contraseña);
            if (rol == null) {
                Registro.aviso("Login fallido para " + mail);
                mostrarAlerta(Alert.AlertType.ERROR, "Usuario o contraseña incorrectos.");
                return;
            }
            Registro.info("Login correcto: " + mail + " (" + rol + ")");

            // Cada rol abre una vista distinta; el DAO devuelve el rol encontrado.
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
                    Registro.aviso("Tipo de usuario desconocido para " + mail + ": " + rol);
                    mostrarAlerta(Alert.AlertType.ERROR, "Tipo de usuario desconocido: " + rol);
            }
        } catch (MongoException e) {
            Registro.error("No se pudo conectar con MongoDB durante el login", e);
            mostrarAlerta(Alert.AlertType.ERROR,
                    "No se puede conectar con MongoDB. Comprueba que esté iniciado.");
        }
    }

    @FXML
    private void onRegistrarse() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/mycompany/vista/Register.fxml"));
            raiz.getScene().setRoot(root);
<<<<<<< HEAD
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
=======
            Registro.info("Pantalla abierta: registro");
        } catch (IOException | NullPointerException e) {
            Registro.error("No se pudo abrir la pantalla de registro", e);
>>>>>>> Asier
            mostrarAlerta(Alert.AlertType.ERROR, "No se pudo abrir la ventana de registro.");
        }
    }

    private void abrirVentana(String fxml, String titulo) {
        try {
            // Solo se cierra el login después de haber cargado y mostrado la nueva vista.
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage nueva = new Stage();
            App.ponerIcono(nueva);
            nueva.setTitle(titulo);
            nueva.setScene(new Scene(root));
            nueva.show();

            // cierra la ventana de login
            ((Stage) txtUsuario.getScene().getWindow()).close();
        } catch (IOException | NullPointerException e) {
            Registro.error("No se pudo abrir la pantalla " + fxml, e);
            mostrarAlerta(Alert.AlertType.ERROR, "No se pudo abrir la ventana " + fxml);
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        // Centraliza la presentación de mensajes al usuario.
        Alert alerta = new Alert(tipo, mensaje);
        App.ponerIcono(alerta);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }
}