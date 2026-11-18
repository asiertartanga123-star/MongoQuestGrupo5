package com.mycompany.controlador;

import com.mycompany.controlador.dao.DatosException;
import com.mycompany.controlador.dao.daoImplementacio;
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

/**
 * Controlador de la pantalla de inicio de sesión de la aplicación.
 */
public class LoginController {

    /** Crea el controlador; lo instancia JavaFX al cargar el FXML. */
    public LoginController() {
    }

    /** Contenedor raíz de la pantalla. */
    @FXML private BorderPane raiz;
    /** Imagen decorativa de la izquierda. */
    @FXML private ImageView imgToroIzq;
    /** Imagen decorativa de la derecha. */
    @FXML private ImageView imgToroDer;
    /** Campo del correo del usuario. */
    @FXML private TextField txtUsuario;
    /** Campo de la contraseña. */
    @FXML private PasswordField txtPassword;

    /** Acceso a los datos de MongoDB. */
    private final daoImplementacio dao = new daoImplementacio();
    /** Correo del usuario que ha iniciado sesión; lo leen las demás pantallas. */
    public static String mailActual;
    /** Tipo del usuario en sesión: {@code gerente}, {@code monitor} o {@code cliente}. */
    public static String rolActual;

    /** Ajusta el alto de las imágenes decorativas al alto de la ventana. */
    @FXML
    private void initialize() {
        imgToroIzq.fitHeightProperty().bind(raiz.heightProperty().subtract(81).multiply(0.85));
        imgToroDer.fitHeightProperty().bind(raiz.heightProperty().subtract(81).multiply(0.85));
    }

    /**
     * Comprueba el correo y la contraseña en MongoDB y abre la pantalla que corresponde al
     * tipo de usuario. Avisa si faltan datos, si son incorrectos o si no hay conexión.
     */
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
                Registro.aviso("Login fallido para " + mail);
                mostrarAlerta(Alert.AlertType.ERROR, "Usuario o contraseña incorrectos.");
                return;
            }
            mailActual = mail;
            rolActual = rol;
            Registro.setUsuario(mail);
            Registro.info("Login correcto: " + mail + " (" + rol + ")");

            if ("gerente".equals(rol)) {
                abrirVentana("/com/mycompany/vista/Gerente.fxml", "Tolotolo - Gerente");
            } else if ("monitor".equals(rol)) {
                abrirVentana("/com/mycompany/vista/Monitor.fxml", "Tolotolo - Monitor");
            } else if ("cliente".equals(rol)) {
                abrirVentana("/com/mycompany/vista/Cliente.fxml", "Tolotolo - Cliente");
            } else {
                Registro.aviso("Tipo de usuario desconocido para " + mail + ": " + rol);
                mostrarAlerta(Alert.AlertType.ERROR, "Tipo de usuario desconocido: " + rol);
            }
        } catch (DatosException e) {
            Registro.error("No se pudo conectar con MongoDB durante el login", e);
            mostrarAlerta(Alert.AlertType.ERROR,
                    "No se puede conectar con MongoDB. Comprueba que esté iniciado.");
        }
    }

    /** Sustituye la pantalla de login por la de registro de un cliente nuevo. */
    @FXML
    private void onRegistrarse() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/mycompany/vista/Register.fxml"));
            raiz.getScene().setRoot(root);
            Registro.info("Pantalla abierta: registro");
        } catch (IOException | NullPointerException e) {
            Registro.error("No se pudo abrir la pantalla de registro", e);
            mostrarAlerta(Alert.AlertType.ERROR, "No se pudo abrir la ventana de registro.");
        }
    }

    /**
     * Abre una pantalla en una ventana nueva y cierra la de login.
     *
     * @param fxml   ruta del FXML de la pantalla
     * @param titulo título de la ventana nueva
     */
    private void abrirVentana(String fxml, String titulo) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage nueva = new Stage();
            App.ponerIcono(nueva);
            nueva.setTitle(titulo);
            nueva.setScene(new Scene(root));
            nueva.show();
            ((Stage) txtUsuario.getScene().getWindow()).close();
        } catch (IOException | NullPointerException e) {
            Registro.error("No se pudo abrir la pantalla " + fxml, e);
            mostrarAlerta(Alert.AlertType.ERROR, "No se pudo abrir la ventana " + fxml);
        }
    }

    /**
     * Muestra un aviso al usuario y espera a que lo cierre.
     *
     * @param tipo    tipo de aviso (error, advertencia o información)
     * @param mensaje texto del aviso
     */
    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje);
        App.ponerIcono(alerta);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }
}