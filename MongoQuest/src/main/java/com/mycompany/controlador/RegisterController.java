package com.mycompany.controlador;

import com.mycompany.controlador.dao.DatosException;
import com.mycompany.controlador.dao.DuplicadoException;
import com.mycompany.controlador.dao.ConexionException;
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

/**
 * Controlador de la pantalla de registro de clientes.
 */
public class RegisterController {

    /** Crea el controlador; lo instancia JavaFX al cargar el FXML. */
    public RegisterController() {
    }

    /** Contenedor raíz de la pantalla. */
    @FXML private BorderPane raiz;
    /** Imagen decorativa del toro, a la izquierda. */
    @FXML private ImageView imgToroIzq;
    /** Imagen decorativa del toro, a la derecha. */
    @FXML private ImageView imgToroDer;
    /** Campo del nombre. */
    @FXML private TextField txtNombre;
    /** Campo de los apellidos. */
    @FXML private TextField txtApellidos;
    /** Campo del DNI. */
    @FXML private TextField txtDni;
    /** Campo del correo electrónico. */
    @FXML private TextField txtMail;
    /** Campo de la contraseña. */
    @FXML private PasswordField txtPassword;
    /** Campo para repetir la contraseña. */
    @FXML private PasswordField txtPassword2;
    /** Selector del nivel de membresía (Normal o Premium). */
    @FXML private ComboBox<String> cmbNivel;

    /** Acceso a la base de datos. */
    private final daoImplementacio dao = new daoImplementacio();

    /** Rellena el selector de membresía, selecciona la primera opción y ajusta las imágenes de los toros al tamaño de la ventana. */
    @FXML
    private void initialize() {
        cmbNivel.getItems().addAll("Normal", "Premium");
        cmbNivel.getSelectionModel().selectFirst();
        imgToroIzq.fitHeightProperty().bind(raiz.heightProperty().subtract(81).multiply(0.80));
        imgToroDer.fitHeightProperty().bind(raiz.heightProperty().subtract(81).multiply(0.80));
    }

    /**
     * Valida el formulario y registra al cliente.
     *
     * <p>Comprueba que no falte ningún campo, que el DNI tenga 8 números y una letra, que el correo sea válido y no exista ya, y que las dos contraseñas coincidan. Si todo es correcto inserta el cliente en MongoDB con la fecha de alta actual y vuelve al login. Avisa si el DNI o el correo están duplicados o si no hay conexión.</p>
     */
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
        } catch (DuplicadoException e) {
            Registro.aviso("Registro rechazado, DNI o mail duplicado: " + dni + " / " + mail);
            mostrarAlerta(Alert.AlertType.WARNING, "Ya existe un usuario con ese DNI o ese mail.");
        } catch (ConexionException e) {
            Registro.error("No se pudo conectar con MongoDB durante el registro de " + mail, e);
            mostrarAlerta(Alert.AlertType.ERROR,
                    "No se puede conectar con MongoDB. Comprueba que esté iniciado.");
        } catch (DatosException e) {
            Registro.error("No se pudo guardar el registro de " + mail, e);
            mostrarAlerta(Alert.AlertType.ERROR, "No se pudo completar el registro.");
        }
    }

    /** Sustituye la pantalla de registro por la de login. */
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

    /**
     * Muestra una alerta con el icono de la aplicación y espera a que se cierre.
     *
     * @param tipo tipo de alerta
     * @param mensaje texto que se muestra
     */
    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje);
        App.ponerIcono(alerta);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }
}