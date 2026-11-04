package com.mycompany.controlador;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.io.IOException;
import java.io.InputStream;
import javafx.scene.control.Dialog;

/**
 * Punto de entrada JavaFX y responsable de mostrar la primera pantalla.
 */
public class App extends Application {

    // La escena se conserva para poder sustituir su contenido si hace falta.
    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        Font.loadFont(App.class.getResource("/com/mycompany/fonts/static/Cinzel-Regular.ttf").toExternalForm(), 12);
        Font.loadFont(App.class.getResource("/com/mycompany/fonts/static/Cinzel-Bold.ttf").toExternalForm(), 12);
        FXMLLoader loader = new FXMLLoader(App.class.getResource("/com/mycompany/vista/Login.fxml"));
        Parent root = loader.load();
        scene = new Scene(root, 640, 480);

        stage.getIcons().add(new Image(App.class.getResourceAsStream(
                "/com/mycompany/mongoquest/imagesApp/logo.png")));   // <-- icono

        stage.setTitle("Tolotolo - Iniciar sesión");
        stage.setScene(scene);
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    /**
     * Carga una vista FXML desde el paquete de recursos indicado.
     */
    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        // Delega el arranque del ciclo de vida en JavaFX.
        launch();
    }

    private static final String RUTA_ICONO = "/com/mycompany/mongoquest/imagesApp/logo.png";
    private static Image icono;

// Carga el icono una sola vez y lo reutiliza en todas las ventanas
    private static Image getIcono() {
        if (icono == null) {
            InputStream flujo = App.class.getResourceAsStream(RUTA_ICONO);
            if (flujo != null) {
                icono = new Image(flujo);
            }
        }
        return icono;
    }

// Para ventanas normales (new Stage())
    public static void ponerIcono(Stage stage) {
        Image i = getIcono();
        if (i != null && !stage.getIcons().contains(i)) {
            stage.getIcons().add(i);
        }
    }

// Para avisos y diálogos (Alert, Dialog)
    public static void ponerIcono(Dialog<?> dialogo) {
        ponerIcono((Stage) dialogo.getDialogPane().getScene().getWindow());
    }
}
