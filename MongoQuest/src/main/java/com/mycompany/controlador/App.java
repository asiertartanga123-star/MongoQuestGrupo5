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

    /** Crea la aplicación; la instancia JavaFX al arrancar. */
    public App() {
    }

    /**
     * Escena principal de la aplicación.
     *
     * <p>Se mantiene en un campo estático para poder cambiarla dinámicamente al
     * navegar entre vistas sin recrear la ventana principal.</p>
     */
    private static Scene scene;

    /**
     * Ruta del icono de la aplicación dentro del classpath.
     */
    private static final String RUTA_ICONO = "/com/mycompany/mongoquest/imagesApp/logo.png";

    /**
     * Icono reutilizado para todas las ventanas y diálogos de la app.
     */
    private static Image icono;

    /**
     * Inicializa la aplicación JavaFX y carga la ventana de inicio de sesión.
     *
     * @param stage ventana principal que se mostrará al arrancar
     * @throws IOException si no se puede cargar el FXML o el recurso asociado
     */
    @Override
    public void start(Stage stage) throws IOException {
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

    /**
     * Se ejecuta cuando la aplicación JavaFX se cierra.
     */
    @Override
    public void stop() {
        Registro.info("Aplicación cerrada");
    }

    /**
     * Carga una fuente de texto de la aplicación si existe en recursos.
     *
     * @param ruta ruta del recurso de fuente dentro del classpath
     */
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

    /**
     * Cambia la vista actual por el contenido del FXML indicado.
     *
     * @param fxml nombre del recurso FXML sin extensión
     * @throws IOException si no existe el archivo FXML
     */
    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    /**
     * Carga una vista FXML desde el paquete de recursos indicado.
     *
     * @param fxml nombre del recurso FXML sin extensión
     * @return nodo raíz cargado desde el archivo FXML
     * @throws IOException si el archivo no existe o falla la carga
     */
    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    /**
     * Método principal que lanza la aplicación JavaFX.
     *
     * @param args argumentos de consola
     */
    public static void main(String[] args) {
        // Arranca JavaFX, que llama a start().
        launch();
    }

    // ---------- Icono de las ventanas ----------

    /**
     * Recupera el icono de la aplicación reutilizándolo entre ventanas.
     *
     * @return icono cargado desde recursos o {@code null} si no existe
     */
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

    /**
     * Asigna el icono de la aplicación a una nueva ventana JavaFX.
     *
     * @param stage ventana a la que se asignará el icono
     */
    public static void ponerIcono(Stage stage) {
        Image i = getIcono();
        if (i != null && !stage.getIcons().contains(i)) {
            stage.getIcons().add(i);
        }
    }

    /**
     * Asigna el mismo icono a un diálogo o ventana modal.
     *
     * @param dialogo cuadro de diálogo JavaFX al que se le asigna el icono
     */
    public static void ponerIcono(Dialog<?> dialogo) {
        ponerIcono((Stage) dialogo.getDialogPane().getScene().getWindow());
    }
}