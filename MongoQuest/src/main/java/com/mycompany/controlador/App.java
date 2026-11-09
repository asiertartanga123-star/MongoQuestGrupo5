package com.mycompany.controlador;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Dialog;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * Punto de entrada JavaFX y responsable de mostrar la primera pantalla.
 */
public class App extends Application {

    // La escena se conserva para poder sustituir su contenido si hace falta.
    private static Scene scene;

    // Ruta del logo que se usa como icono de todas las ventanas.
    private static final String RUTA_ICONO = "/com/mycompany/mongoquest/imagesApp/logo.png";
    private static Image icono;

    @Override
    public void start(Stage stage) throws IOException {
        // Cualquier error que nadie controle queda en errores.0.log.
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread hilo, Throwable e) {
                Registro.error("Error no controlado en el hilo " + hilo.getName(), e);
            }
        });
        Registro.info("Aplicación iniciada");

        cargarFuente("/com/mycompany/fonts/static/Cinzel-Regular.ttf");
        cargarFuente("/com/mycompany/fonts/static/Cinzel-Bold.ttf");

        FXMLLoader loader = new FXMLLoader(App.class.getResource("/com/mycompany/vista/Login.fxml"));
        Parent root = loader.load();
        scene = new Scene(root, 640, 480);

        ponerIcono(stage);
        stage.setTitle("Tolotolo - Iniciar sesión");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        Registro.info("Aplicación cerrada");
    }

    // Una fuente que falte no debe impedir que la aplicación arranque.
    private static void cargarFuente(String ruta) {
        URL url = App.class.getResource(ruta);
        if (url == null) {
            Registro.aviso("No se encuentra la fuente " + ruta);
            return;
        }
        Font fuente = Font.loadFont(url.toExternalForm(), 12);
        if (fuente == null) {
            Registro.aviso("No se pudo cargar la fuente " + ruta);
        }
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

    // ---------- Icono de las ventanas ----------

    // Carga el icono una sola vez y lo reutiliza en todas las ventanas.
    private static Image getIcono() {
        if (icono == null) {
            InputStream flujo = App.class.getResourceAsStream(RUTA_ICONO);
            if (flujo != null) {
                icono = new Image(flujo);
            } else {
                Registro.aviso("No se encuentra el icono " + RUTA_ICONO);
            }
        }
        return icono;
    }

    // Para ventanas normales (new Stage()).
    public static void ponerIcono(Stage stage) {
        Image i = getIcono();
        if (i != null && !stage.getIcons().contains(i)) {
            stage.getIcons().add(i);
        }
    }

    // Para avisos y diálogos (Alert, Dialog).
    public static void ponerIcono(Dialog<?> dialogo) {
        ponerIcono((Stage) dialogo.getDialogPane().getScene().getWindow());
    }
}