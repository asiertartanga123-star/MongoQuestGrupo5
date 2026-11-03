package com.mycompany.controlador;

import java.io.IOException;
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

    // Elementos de la vista del gerente utilizados para ajustar imágenes y navegar.
    @FXML private BorderPane raiz;
    @FXML private VBox cardMonitores;
    @FXML private VBox cardClases;
    @FXML private VBox cardClientes;
    @FXML private ImageView imgMonitores;
    @FXML private ImageView imgClases;
    @FXML private ImageView imgClientes;

    @FXML
    private void initialize() {
        // Las imágenes se escalan respecto al tamaño real de cada tarjeta.
        ajustarImagen(imgMonitores, cardMonitores);
        ajustarImagen(imgClases, cardClases);
        ajustarImagen(imgClientes, cardClientes);
    }

    private void ajustarImagen(ImageView img, VBox card) {
        // Mantiene la proporción de las imágenes y deja margen dentro de la tarjeta.
        img.fitWidthProperty().bind(card.widthProperty().multiply(0.85));
        img.fitHeightProperty().bind(card.heightProperty().multiply(0.80));
    }

    @FXML
    private void onGestionarMonitores() {
        try {
            // Sustituye el panel del gerente por la pantalla de gestión de monitores.
            Parent root = FXMLLoader.load(getClass().getResource("/com/mycompany/vista/GestorMonitor.fxml"));
            Stage login = new Stage();
            login.setTitle("Tolotolo - Iniciar sesión");
            login.setScene(new Scene(root));
            login.show();

            ((Stage) raiz.getScene().getWindow()).close();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "No se pudo cargar GestorMonitor.").showAndWait();
        }  
    }

    @FXML
    private void onGestionarClases() {
        try {
            // Abre la pantalla de clases; el recurso debe existir para que esta opción funcione.
            Parent root = FXMLLoader.load(getClass().getResource("/com/mycompany/vista/GestorClases.fxml"));
            Stage login = new Stage();
            login.setTitle("Tolotolo - GestorCliente");
            login.setScene(new Scene(root));
            login.show();

            ((Stage) raiz.getScene().getWindow()).close();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "No se pudo cargar GestorClases.").showAndWait();
        }
    }

    @FXML
    private void onGestionarClientes() {
       try {
            // Sustituye el panel por la pantalla de administración de clientes.
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

    @FXML
    private void onCerrarSesion() {
        try {
            // Cierra la ventana actual y devuelve al usuario al formulario de acceso.
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