package com.mycompany.controlador;

import com.mycompany.controlador.dao.DatosException;
import java.io.IOException;
import java.net.URL;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Controlador del panel principal del gerente.
 * <p>
 * Desde aquí se accede a la gestión de clientes, monitores y clases, así como al
 * cierre de sesión.
 */
public class GerenteController {

    /** Crea el controlador; lo instancia JavaFX al cargar el FXML. */
    public GerenteController() {
    }

    // Elementos de la vista del gerente
    /** Contenedor raíz de la pantalla. */
    @FXML private BorderPane raiz;
    /** Tarjeta de gestión de monitores. */
    @FXML private VBox cardMonitores;
    /** Tarjeta de gestión de clases. */
    @FXML private VBox cardClases;
    /** Tarjeta de gestión de clientes. */
    @FXML private VBox cardClientes;
    /** Imagen de la tarjeta de monitores. */
    @FXML private ImageView imgMonitores;
    /** Imagen de la tarjeta de clases. */
    @FXML private ImageView imgClases;
    /** Imagen de la tarjeta de clientes. */
    @FXML private ImageView imgClientes;

    // Evita abrir la misma ventana dos veces con un doble clic.
    /** Indica que ya se está abriendo una ventana, para ignorar un segundo clic. */
    private boolean abriendo = false;

    /**
     * Ajusta las imágenes de cada tarjeta para que se adapten al tamaño del panel.
     */
    @FXML
    private void initialize() {
        // Escalar imágenes rompiendo la dependencia circular de layout
        ajustarImagen(imgMonitores, cardMonitores);
        ajustarImagen(imgClases, cardClases);
        ajustarImagen(imgClientes, cardClientes);
    }

    /**
     * Redimensiona una imagen para que mantenga proporción dentro de su tarjeta.
     *
     * @param img vista de imagen a escalar
     * @param card contenedor sobre el que se observa el cambio de tamaño
     */
    private void ajustarImagen(final ImageView img, VBox card) {
        if (img == null || card == null) return;

        img.setPreserveRatio(true);

        // Escuchar cambios de tamaño del VBox en lugar de bindear propiedades directamente.
        // Esto evita que el ImageView infle el tamaño preferido del contenedor padre.
        card.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> obs, Number oldVal, Number newVal) {
                double anchoCard = newVal.doubleValue();
                if (anchoCard > 0) {
                    img.setFitWidth(anchoCard * 0.75);
                }
            }
        });

        card.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> obs, Number oldVal, Number newVal) {
                double altoCard = newVal.doubleValue();
                if (altoCard > 0) {
                    img.setFitHeight(altoCard * 0.55);
                }
            }
        });
    }

    /**
     * Abre la pantalla de gestión de monitores.
     */
    @FXML
    private void onGestionarMonitores() {
        abrirVentana("/com/mycompany/vista/GestorMonitor.fxml", "Tolotolo - Gestión de monitores");
    }

    /**
     * Abre la pantalla de gestión de clases.
     */
    @FXML
    private void onGestionarClases() {
         
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/mycompany/vista/GestorClase.fxml"));
            Stage login = new Stage();
            login.setTitle("Tolotolo - Gestión de clases");
            login.setScene(new Scene(root));
            login.show();

            ((Stage) raiz.getScene().getWindow()).close();
        } catch (IOException | NullPointerException e) {
            Registro.error("Error en " + getClass().getSimpleName(), e);
            new Alert(Alert.AlertType.ERROR, "No se pudo cargar GestorClases.").showAndWait();
        }
    }

    /**
     * Abre la pantalla de gestión de clientes.
     */
    @FXML
    private void onGestionarClientes() {
        abrirVentana("/com/mycompany/vista/GestorCliente.fxml", "Tolotolo - Gestión de clientes");
    }

    /**
     * Abre el visor del log de operaciones en una ventana aparte, sin cerrar el panel del gerente.
     */
    @FXML
    private void onVerLog() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/mycompany/vista/VisorLog.fxml"));
            Stage visor = new Stage();
            App.ponerIcono(visor);
            visor.setTitle("Tolotolo - Log de operaciones");
            visor.setScene(new Scene(root));
            visor.initOwner(raiz.getScene().getWindow());
            visor.show();
            Registro.info("Visor del log abierto");
        } catch (IOException | NullPointerException e) {
            Registro.error("No se pudo abrir el visor del log", e);
            aviso("No se pudo abrir el visor del log.");
        }
    }

    /**
     * Menú Ayuda → «Acerca de Tolotolo Gym»: muestra qué es la aplicación y quién la ha hecho.
     */
    @FXML
    private void onAcercaDe() {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Acerca de Tolotolo Gym");
        info.setHeaderText("Tolotolo Gym (MongoQuest)");
        info.setContentText("Gestión de clientes, monitores, clases y sesiones del gimnasio con JavaFX y MongoDB.\n\n"
                + "Grupo 5: Asier, Ekain, Iker y Ricardo.");
        info.initOwner(raiz.getScene().getWindow());
        info.showAndWait();
    }

    /**
     * Cierra la sesión del gerente y vuelve a la vista de inicio.
     */
    @FXML
    private void onCerrarSesion() {
        Registro.info("Sesión cerrada desde el panel del gerente");
        abrirVentana("/com/mycompany/vista/Login.fxml", "Tolotolo - Iniciar sesión");
    }

    /**
     * Carga una pantalla secundaria y cierra la ventana del gerente.
     *
     * @param fxml ruta del FXML de destino
     * @param titulo título de la nueva ventana
     */
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

    /**
     * Muestra una ventana de error con el mensaje indicado.
     *
     * @param mensaje detalle del problema a comunicar
     */
    private void aviso(String mensaje) {
        Alert a = new Alert(Alert.AlertType.ERROR, mensaje);
        App.ponerIcono(a);
        a.setHeaderText(null);
        a.showAndWait();
    }
}