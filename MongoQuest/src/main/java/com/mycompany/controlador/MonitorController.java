package com.mycompany.controlador;

import com.mycompany.controlador.dao.daoImplementacio;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import org.bson.Document;

/**
 * Controlador asociado a la vista del monitor.
 */
public class MonitorController implements Initializable {

    /** Contenedor raíz de la pantalla. */
    @FXML private BorderPane raiz;
    /** Círculo con la foto de perfil del monitor. */
    @FXML private Circle circuloFoto;
    /** Tarjeta de gestión de monitores. */
    @FXML private VBox cardMonitores;
    /** Tarjeta de gestión de clases. */
    @FXML private VBox cardClases;
    /** Tarjeta de gestión de clientes. */
    @FXML private VBox cardClientes;
    /** Imagen de la tarjeta de monitores. */
    @FXML private ImageView imgMonitores;
    /** Imagen de la tarjeta de clases. */
    @FXML private ImageView imgClases;
    /** Imagen de la tarjeta de clientes. */
    @FXML private ImageView imgClientes;

    /** Acceso a la base de datos. */
    private final daoImplementacio dao = new daoImplementacio();

    /**
     * Ajusta el tamaño de las imágenes de las tarjetas y carga la foto del monitor en sesión.
     *
     * @param location ubicación del FXML
     * @param resources recursos de la vista, si los hay
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ajustarImagen(imgMonitores, cardMonitores);
        ajustarImagen(imgClases, cardClases);
        ajustarImagen(imgClientes, cardClientes);

        if (LoginController.mailActual != null) {
            Document monitor = dao.buscarTrabajadorPorMail(LoginController.mailActual);
            if (monitor != null) {
                cargarFotoPerfil(monitor.getString("dni"));
            }
        }
    }

    /**
     * Carga en el círculo de perfil la imagen guardada para el DNI del monitor.
     *
     * @param dni DNI del monitor en sesión
     */
    private void cargarFotoPerfil(String dni) {
        if (circuloFoto == null || dni == null || dni.isEmpty()) {
            return;
        }
        Image foto = GestorImagenes.cargar(dni);
        if (foto != null && !foto.isError()) {
            circuloFoto.setFill(new ImagePattern(foto));
        }
    }

    /**
     * Enlaza el tamaño de una imagen con el de su tarjeta (85 % del ancho y 80 % del alto).
     *
     * @param img imagen a escalar
     * @param card tarjeta que la contiene
     */
    private void ajustarImagen(ImageView img, VBox card) {
        if (img == null || card == null) {
            return;
        }
        img.fitWidthProperty().bind(card.widthProperty().multiply(0.85));
        img.fitHeightProperty().bind(card.heightProperty().multiply(0.80));
    }

    /** Abre la pantalla de gestión de clases en una ventana nueva y cierra la actual. Si no se puede cargar, muestra una alerta. */
    @FXML
    private void onGestionarClases() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/mycompany/vista/GestorClase.fxml"));
            Stage login = new Stage();
            login.setTitle("Tolotolo - Gestión de clases");
            login.setScene(new Scene(root));
            login.show();

            ((Stage) raiz.getScene().getWindow()).close();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "No se pudo cargar GestorClases.").showAndWait();
        }
    }

    /** Abre la pantalla de gestión de clientes en una ventana nueva y cierra la actual. Si no se puede cargar, muestra una alerta. */
    @FXML
    private void onGestionarClientes() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/mycompany/vista/GestorCliente.fxml"));
            Stage login = new Stage();
            login.setTitle("Tolotolo - GestorCliente");
            login.setScene(new Scene(root));
            login.show();

            ((Stage) raiz.getScene().getWindow()).close();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "No se pudo cargar GestorClientes.").showAndWait();
        }
    }

    /** Abre la pantalla de login en una ventana nueva y cierra la actual. Si no se puede cargar, muestra una alerta. */
    @FXML
    private void onCerrarSesion() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/mycompany/vista/Login.fxml"));
            Stage login = new Stage();
            login.setTitle("Tolotolo - Iniciar sesión");
            login.setScene(new Scene(root));
            login.show();

            ((Stage) raiz.getScene().getWindow()).close();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "No se pudo volver al login.").showAndWait();
        }
    }
}