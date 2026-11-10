package com.mycompany.controlador;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxAssert;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;
import org.testfx.util.WaitForAsyncUtils;

public class GerenteControllerTest extends ApplicationTest {

    // Se ejecuta antes de cada test: abre el panel del gerente en una ventana.
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/com/mycompany/vista/Gerente.fxml"));
        stage.setScene(new Scene(root));
        stage.show();
    }

    // Cierra todas las ventanas y avisos que haya dejado abiertos el test.
    @AfterEach
    public void cerrarVentanas() throws Exception {
        FxToolkit.cleanupStages();
    }

    // Espera hasta 5 segundos a que aparezca un elemento en pantalla.
    private void esperar(final String selector) throws Exception {
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, new Callable<Boolean>() {
            @Override
            public Boolean call() {
                return lookup(selector).tryQuery().isPresent();
            }
        });
    }

    @Test
    public void elPanelMuestraLasTresTarjetas() {
        FxAssert.verifyThat("#cardMonitores", NodeMatchers.isVisible());
        FxAssert.verifyThat("#cardClases", NodeMatchers.isVisible());
        FxAssert.verifyThat("#cardClientes", NodeMatchers.isVisible());
    }

    @Test
    public void lasImagenesDeLasTarjetasSeCargan() {
        FxAssert.verifyThat("#imgMonitores", NodeMatchers.isVisible());
        FxAssert.verifyThat("#imgClases", NodeMatchers.isVisible());
        FxAssert.verifyThat("#imgClientes", NodeMatchers.isVisible());
    }

    @Test
    public void pulsarClasesSinPantallaMuestraUnAvisoYSeQuedaEnElPanel() throws Exception {
        // GestorClases.fxml todavía no existe: debe salir el aviso de error.
        clickOn("#cardClases");
        esperar(".dialog-pane");
        FxAssert.verifyThat(".dialog-pane", NodeMatchers.isVisible());

        // Se cierra el aviso con Enter y el panel del gerente sigue abierto.
        type(KeyCode.ENTER);
        FxAssert.verifyThat("#cardClases", NodeMatchers.isVisible());
    }

    @Test
    public void cerrarSesionAbreElLogin() throws Exception {
        clickOn("#btnCerrarSesion");
        // Se espera a que el login termine de cargar y se vea el campo del mail.
        esperar("#txtUsuario");
        FxAssert.verifyThat("#txtUsuario", NodeMatchers.isVisible());
    }
}