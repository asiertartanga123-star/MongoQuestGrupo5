package com.mycompany.controlador;

import java.io.IOException;
import java.net.URL;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class GerenteController {

    // Elementos de la vista del gerente
    @FXML private BorderPane raiz;
    @FXML private VBox cardMonitores;
    @FXML private VBox cardClases;
    @FXML private VBox cardClientes;
    @FXML private ImageView imgMonitores;
    @FXML private ImageView imgClases;
    @FXML private ImageView imgClientes;

    // Evita abrir la misma ventana dos veces con un doble clic.
    private boolean abriendo = false;

    @FXML
    private void initialize() {
        // Escalar imágenes rompiendo la dependencia circular de layout
        ajustarImagen(imgMonitores, cardMonitores);
        ajustarImagen(imgClases, cardClases);
        ajustarImagen(imgClientes, cardClientes);
    }

    private void ajustarImagen(ImageView img, VBox card) {
        if (img == null || card == null) return;

        img.setPreserveRatio(true);

        // Escuchar cambios de tamaño del VBox en lugar de bindear propiedades directamente.
        // Esto evita que el ImageView infle el tamaño preferido del contenedor padre.
        card.widthProperty().addListener((obs, oldVal, newVal) -> {
            double anchoCard = newVal.doubleValue();
            if (anchoCard > 0) {
                img.setFitWidth(anchoCard * 0.75);
            }
        });

        card.heightProperty().addListener((obs, oldVal, newVal) -> {
            double altoCard = newVal.doubleValue();
            if (altoCard > 0) {
                img.setFitHeight(altoCard * 0.55);
            }
        });
    }

    @FXML
    private void onGestionarMonitores() {
        abrirVentana("/com/mycompany/vista/GestorMonitor.fxml", "Tolotolo - Gestión de monitores");
    }

    @FXML
    private void onGestionarClases() {
        abrirVentana("/com/mycompany/vista/GestorClases.fxml", "Tolotolo - Gestión de clases");
    }

    @FXML
    private void onGestionarClientes() {
        abrirVentana("/com/mycompany/vista/GestorCliente.fxml", "Tolotolo - Gestión de clientes");
    }

    @FXML
    private void onCerrarSesion() {
        Registro.info("Sesión cerrada desde el panel del gerente");
        abrirVentana("/com/mycompany/vista/Login.fxml", "Tolotolo - Iniciar sesión");
    }

    // Abre una pantalla en una ventana nueva y cierra la del gerente.
    private void abrirVentana(String fxml, String titulo) {
        if (abriendo) {
            return;
        }
        abriendo = true;
        try {
            URL ruta = getClass().getResource(fxml);
            if (ruta == null) {
                abriendo = false;
                Registro.aviso("No se encuentra la pantalla " + fxml);
                aviso("No se encuentra la pantalla " + fxml);
                return;
            }

            Parent root = FXMLLoader.load(ruta);
            Stage nueva = new Stage();
            App.ponerIcono(nueva);
            nueva.setTitle(titulo);
            nueva.setScene(new Scene(root));
            nueva.show();
            Registro.info("Pantalla abierta: " + titulo);

            ((Stage) raiz.getScene().getWindow()).close();
        } catch (IOException | NullPointerException e) {
            abriendo = false;
            Registro.error("No se pudo abrir la pantalla " + fxml, e);
            aviso("No se pudo abrir la pantalla " + fxml);
        }
    }

    private void aviso(String mensaje) {
        Alert a = new Alert(Alert.AlertType.ERROR, mensaje);
        App.ponerIcono(a);
        a.setHeaderText(null);
        a.showAndWait();
    }
}