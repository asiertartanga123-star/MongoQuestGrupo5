package com.mycompany.controlador;

import com.mycompany.controlador.dao.DatosException;
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

    @FXML private BorderPane raiz;
    @FXML private Circle circuloFoto;
    @FXML private VBox cardMonitores;
    @FXML private VBox cardClases;
    @FXML private VBox cardClientes;
    @FXML private ImageView imgMonitores;
    @FXML private ImageView imgClases;
    @FXML private ImageView imgClientes;

    private final daoImplementacio dao = new daoImplementacio();

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

    private void cargarFotoPerfil(String dni) {
        if (circuloFoto == null || dni == null || dni.isEmpty()) {
            return;
        }
        Image foto = GestorImagenes.cargar(dni);
        if (foto != null && !foto.isError()) {
            circuloFoto.setFill(new ImagePattern(foto));
        }
    }

    private void ajustarImagen(ImageView img, VBox card) {
        if (img == null || card == null) {
            return;
        }
        img.fitWidthProperty().bind(card.widthProperty().multiply(0.85));
        img.fitHeightProperty().bind(card.heightProperty().multiply(0.80));
    }

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
            Registro.error("Error en " + getClass().getSimpleName(), e);
            new Alert(Alert.AlertType.ERROR, "No se pudo cargar GestorClases.").showAndWait();
        }
    }

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
            Registro.error("Error en " + getClass().getSimpleName(), e);
            new Alert(Alert.AlertType.ERROR, "No se pudo cargar GestorClientes.").showAndWait();
        }
    }

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
            Registro.error("Error en " + getClass().getSimpleName(), e);
            new Alert(Alert.AlertType.ERROR, "No se pudo volver al login.").showAndWait();
        }
    }
}