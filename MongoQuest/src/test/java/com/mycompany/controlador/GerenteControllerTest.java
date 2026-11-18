package com.mycompany.controlador;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import org.junit.jupiter.api.Assertions;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxAssert;
import org.testfx.api.FxToolkit;
import org.testfx.matcher.base.NodeMatchers;
import org.testfx.framework.junit5.ApplicationTest;

/**
 * Pruebas TestFX del panel del gerente (Gerente.fxml): las tarjetas, la navegación
 * a cada gestión, el visor del log de operaciones y el cierre de sesión.
 */
public class GerenteControllerTest extends ApplicationTest {

    /** Crea la clase de pruebas; JUnit crea una instancia para cada test. */
    public GerenteControllerTest() {
    }

    // Se ejecuta antes de cada test: abre el panel del gerente en una ventana.
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/com/mycompany/vista/Gerente.fxml"));
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

    /** Comprueba que el panel muestra las tres tarjetas. */
    @Test
    public void elPanelMuestraLasTresTarjetas() {
        FxAssert.verifyThat("#cardMonitores", NodeMatchers.isVisible());
        FxAssert.verifyThat("#cardClases", NodeMatchers.isVisible());
        FxAssert.verifyThat("#cardClientes", NodeMatchers.isVisible());
    }

    /** Comprueba que las imágenes de las tarjetas se cargan. */
    @Test
    public void lasImagenesDeLasTarjetasSeCargan() {
        FxAssert.verifyThat("#imgMonitores", NodeMatchers.isVisible());
        FxAssert.verifyThat("#imgClases", NodeMatchers.isVisible());
        FxAssert.verifyThat("#imgClientes", NodeMatchers.isVisible());
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

    /** Comprueba que pulsar clientes abre la gestión de clientes. */
    @Test
    public void pulsarClientesAbreLaGestionDeClientes() {
        clickOn("#cardClientes");
        FxAssert.verifyThat("#listClientes", NodeMatchers.isVisible());
    }

    /** Comprueba que pulsar monitores abre la gestión de monitores. */
    @Test
    public void pulsarMonitoresAbreLaGestionDeMonitores() {
        clickOn("#cardMonitores");
        FxAssert.verifyThat("#listMonitores", NodeMatchers.isVisible());
    }

    /** Comprueba que pulsar clases abre la gestión de clases. */
    @Test
    public void pulsarClasesAbreLaGestionDeClases() {
        clickOn("#cardClases");
        FxAssert.verifyThat("#listClases", NodeMatchers.isVisible());
    }

    /** Comprueba que el botón «Ver log» abre el visor con las operaciones. */
    @Test
    public void verLogAbreElVisorConLasOperaciones() {
        clickOn("#btnVerLog");
        FxAssert.verifyThat("#tablaLog", NodeMatchers.isVisible());
        Label total = lookup("#lblTotal").queryAs(Label.class);
        Assertions.assertTrue(total.getText().endsWith("operaciones"), "Texto del total: " + total.getText());
        // El panel del gerente sigue abierto detrás del visor.
        FxAssert.verifyThat("#cardClientes", NodeMatchers.isVisible());
    }

    /** Comprueba que el filtro del visor deja solo las líneas que coinciden. */
    @Test
    public void elFiltroDelVisorDejaSoloLasLineasQueCoinciden() {
        clickOn("#btnVerLog");
        clickOn("#txtFiltro").write("texto que no aparece en ningun log 12345");
        Label total = lookup("#lblTotal").queryAs(Label.class);
        Assertions.assertTrue(total.getText().startsWith("0 de "), "Texto del total: " + total.getText());
    }
}