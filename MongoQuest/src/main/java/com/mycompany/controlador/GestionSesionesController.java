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

    /** Contenedor raíz de la vista; da acceso a la escena y a la ventana. */
    @FXML private BorderPane raiz;
    /** Título del horario: nombre de la clase. Al pulsarlo se vuelve al panel de clases. */
    @FXML private Label lblTitulo;
    /** Rejilla del horario semanal de la clase. */
    @FXML private GridPane gridHorario;

    /** Clase cuyo horario se muestra; la fija {@link #setClase(Document)}. */
    private Document clase;

    /**
     * Indica qué clase se va a mostrar y pone su nombre como título.
     * Hay que llamarlo justo después de cargar el FXML.
     *
     * @param clase documento de la colección {@code Clases}
     */
    public void setClase(Document clase) {
        this.clase = clase;
        lblTitulo.setText(clase.getString("nombre"));
    }

    /** Vuelve al panel de gestión de clases en la misma ventana (se activa al pulsar el título). */
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

    /** Cierra la sesión del gerente: abre la ventana de login y cierra esta. */
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

    /**
     * Muestra un aviso de error y espera a que el usuario lo cierre.
     *
     * @param mensaje texto del aviso
     */
    private void alerta(String mensaje) {
        Alert a = new Alert(Alert.AlertType.ERROR, mensaje);
        a.setHeaderText(null);
        a.showAndWait();
    }
}
