package com.mycompany.controlador;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxAssert;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;

public class GerenteControllerTest extends ApplicationTest {

    // Se ejecuta antes de cada test: abre el panel del gerente en una ventana.
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/com/mycompany/vista/Gerente.fxml"));
        stage.setScene(new Scene(root));
        stage.show();
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
    public void pulsarClasesSinPantallaMuestraUnAvisoYSeQuedaEnElPanel() {
        // GestorClases.fxml todavía no existe: debe salir el aviso de error.
        clickOn("#cardClases");
        FxAssert.verifyThat(".dialog-pane", NodeMatchers.isVisible());

        // Se cierra el aviso con Enter y el panel del gerente sigue abierto.
        type(KeyCode.ENTER);
        FxAssert.verifyThat("#cardClases", NodeMatchers.isVisible());
    }

    @Test
    public void cerrarSesionAbreElLogin() {
        clickOn("#btnCerrarSesion");
        // Si aparece el campo del mail, es que se ha abierto el login.
        FxAssert.verifyThat("#txtUsuario", NodeMatchers.isVisible());
    }
}