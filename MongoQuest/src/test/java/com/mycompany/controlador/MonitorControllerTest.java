package com.mycompany.controlador;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxAssert;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;

/**
 * Pruebas TestFX del panel del monitor (Monitor.fxml): solo ve las tarjetas de clases
 * y clientes, cada una abre su gestión, y puede cerrar sesión.
 */
public class MonitorControllerTest extends ApplicationTest {

    /** Crea la clase de pruebas; JUnit crea una instancia para cada test. */
    public MonitorControllerTest() {
    }

    // Se ejecuta antes de cada test: abre el panel del monitor en una ventana.
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/com/mycompany/vista/Monitor.fxml"));
        stage.setScene(new Scene(root));
        stage.show();
    }

    /**
     * Después de cada test: se cierran los avisos que hayan quedado abiertos y todas las ventanas.
     *
     * @throws Exception si TestFX no puede abrir, esperar o cerrar una ventana
     */
    @AfterEach
    public void cerrarVentanas() throws Exception {
        cerrarAvisosAbiertos();
        FxToolkit.cleanupStages();
    }

    // ---------- Ayudas ----------

    /** Cierra cualquier Alert o Dialog que siga en pantalla (cleanupStages no los cierra). */
    private void cerrarAvisosAbiertos() {
        List<Window> ventanas = new ArrayList<Window>(listWindows());
        for (final Window w : ventanas) {
            if (w.isShowing() && w.getScene() != null
                    && w.getScene().getRoot() instanceof DialogPane) {
                interact(new Runnable() {
                    @Override
                    public void run() {
                        w.hide();
                    }
                });
            }
        }
    }

    // ---------- Tests ----------

    /** Comprueba que el panel muestra las dos tarjetas. */
    @Test
    public void elPanelMuestraLasDosTarjetas() {
        FxAssert.verifyThat("#cardClases", NodeMatchers.isVisible());
        FxAssert.verifyThat("#cardClientes", NodeMatchers.isVisible());
    }

    /** Comprueba que las imágenes de las tarjetas se cargan. */
    @Test
    public void lasImagenesDeLasTarjetasSeCargan() {
        FxAssert.verifyThat("#imgClases", NodeMatchers.isVisible());
        FxAssert.verifyThat("#imgClientes", NodeMatchers.isVisible());
    }

    /** Comprueba que el monitor no tiene acceso a la gestión de monitores. */
    @Test
    public void elMonitorNoTieneAccesoALaGestionDeMonitores() {
        Assertions.assertTrue(lookup("#cardMonitores").queryAll().isEmpty(),
                "El monitor no debería ver la tarjeta de gestión de monitores");
    }

    /** Comprueba que pulsar clases abre la gestión de clases. */
    @Test
    public void pulsarClasesAbreLaGestionDeClases() {
        clickOn("#cardClases");
        FxAssert.verifyThat("#listClases", NodeMatchers.isVisible());
    }

    /** Comprueba que pulsar clientes abre la gestión de clientes. */
    @Test
    public void pulsarClientesAbreLaGestionDeClientes() {
        clickOn("#cardClientes");
        FxAssert.verifyThat("#listClientes", NodeMatchers.isVisible());
    }

    /**
     * Comprueba que cerrar sesión abre el login.
     *
     * @throws Exception si TestFX no puede abrir, esperar o cerrar una ventana
     */
    @Test
    public void cerrarSesionAbreElLogin() throws Exception {
        clickOn("#btnCerrarSesion");
        // Se espera a que el login termine de cargar y se vea el campo del usuario
        FxAssert.verifyThat("#txtUsuario", NodeMatchers.isVisible());
    }
}