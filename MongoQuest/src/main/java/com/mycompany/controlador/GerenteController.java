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

    @FXML private BorderPane raiz;
    @FXML private VBox cardMonitores;
    @FXML private VBox cardClases;
    @FXML private VBox cardClientes;
    @FXML private ImageView imgMonitores;
    @FXML private ImageView imgClases;
    @FXML private ImageView imgClientes;

    @FXML
    private void initialize() {
        // Los toros se adaptan al tamaño de cada tarjeta
        ajustarImagen(imgMonitores, cardMonitores);
        ajustarImagen(imgClases, cardClases);
        ajustarImagen(imgClientes, cardClientes);
    }

    private void ajustarImagen(ImageView img, VBox card) {
        img.fitWidthProperty().bind(card.widthProperty().multiply(0.85));
        img.fitHeightProperty().bind(card.heightProperty().multiply(0.80));
    }

    @FXML
    private void onGestionarMonitores() {
      
        try {
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