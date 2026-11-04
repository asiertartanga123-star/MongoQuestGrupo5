package com.mycompany.controlador;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        Font.loadFont(App.class.getResource("/com/mycompany/fonts/static/Cinzel-Regular.ttf").toExternalForm(), 12);
        Font.loadFont(App.class.getResource("/com/mycompany/fonts/static/Cinzel-Bold.ttf").toExternalForm(), 12);
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

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

}