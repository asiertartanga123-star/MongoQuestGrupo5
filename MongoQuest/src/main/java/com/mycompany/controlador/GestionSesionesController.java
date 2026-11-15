package com.mycompany.controlador;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.bson.Document;

/**
 * Horario semanal de sesiones de una clase (GestorSesiones.fxml).
 * Lo abre "Gestionar sesiones" desde el panel de clases, que llama a setClase().
 * ponytail: por ahora solo navegación y título; pintar y crear sesiones en los huecos va después.
 */
public class GestionSesionesController {

    @FXML private BorderPane raiz;
    @FXML private Label lblTitulo;
    @FXML private GridPane gridHorario;

    private Document clase;

    public void setClase(Document clase) {
        this.clase = clase;
        lblTitulo.setText(clase.getString("nombre"));
    }

    // Pulsar el título vuelve al panel de clases
    @FXML
    private void onVolver() {
        try {
            Scene escena = raiz.getScene();
            escena.setRoot(FXMLLoader.load(getClass().getResource("/com/mycompany/vista/GestorClase.fxml")));
            ((Stage) escena.getWindow()).setTitle("Tolotolo - Gestión de clases");
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            alerta("No se pudo volver al panel de clases.");
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
            alerta("No se pudo volver al login.");
        }
    }

    private void alerta(String mensaje) {
        Alert a = new Alert(Alert.AlertType.ERROR, mensaje);
        a.setHeaderText(null);
        a.showAndWait();
    }
}
