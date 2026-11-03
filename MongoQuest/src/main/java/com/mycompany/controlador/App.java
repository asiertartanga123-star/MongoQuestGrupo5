package com.mycompany.controlador;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/** Punto de entrada JavaFX y responsable de mostrar la primera pantalla. */
public class App extends Application {

    // La escena se conserva para poder sustituir su contenido si hace falta.
    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        // Carga el formulario de acceso y prepara la ventana principal.
        FXMLLoader loader = new FXMLLoader(App.class.getResource("/com/mycompany/vista/Login.fxml"));
        Parent root = loader.load();
        scene = new Scene(root, 640, 480);
        stage.setTitle("Tolotolo - Iniciar sesión");
        stage.setScene(scene);
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    /** Carga una vista FXML desde el paquete de recursos indicado. */
    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        // Delega el arranque del ciclo de vida en JavaFX.
        launch();
    }

}