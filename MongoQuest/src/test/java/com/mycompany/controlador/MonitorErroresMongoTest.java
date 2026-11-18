package com.mycompany.controlador;

import com.mycompany.controlador.dao.ConexionException;
import com.mycompany.controlador.dao.DatosException;
import com.mycompany.controlador.dao.daoImplementacio;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxAssert;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;

/**
 * Pruebas TestFX de cómo trata el panel del monitor los errores de MongoDB al abrirse.
 * <p>
 * El panel solo lee de MongoDB los datos del monitor para su foto. Con un acceso a datos falso
 * que falla, se comprueba que el panel se abre igual, sin avisos, y que se puede seguir usando.
 */
public class MonitorErroresMongoTest extends ApplicationTest {

    /** Error que lanzará {@code buscarTrabajadorPorMail}; {@code null} = devuelve un monitor. */
    private RuntimeException fallo;
    /** Ventana que prepara TestFX; cada test carga en ella el panel. */
    private Stage ventana;
    /** Correo de la sesión antes del test, para dejarlo como estaba. */
    private String mailAntes;

    /** Crea la clase de pruebas; JUnit crea una instancia para cada test. */
    public MonitorErroresMongoTest() {
    }

    /**
     * Antes de cada test: guarda la ventana y pone un correo de monitor en la sesión.
     *
     * @param stage ventana que prepara TestFX
     */
    @Override
    public void start(Stage stage) {
        ventana = stage;
        mailAntes = LoginController.mailActual;
        LoginController.mailActual = "monitor.prueba@prueba.com";
    }

    /**
     * Después de cada test: cierra los avisos abiertos y las ventanas, y restaura la sesión.
     *
     * @throws Exception si TestFX no puede cerrar una ventana
     */
    @AfterEach
    public void cerrarVentanas() throws Exception {
        List<Window> ventanas = new ArrayList<Window>(listWindows());
        for (final Window w : ventanas) {
            if (w.isShowing() && w.getScene() != null && w.getScene().getRoot() instanceof DialogPane) {
                interact(new Runnable() {
                    @Override
                    public void run() {
                        w.hide();
                    }
                });
            }
        }
        FxToolkit.cleanupStages();
        LoginController.mailActual = mailAntes;
    }

    /** Abre el panel del monitor con el acceso a datos falso. */
    private void abrir() {
        final daoImplementacio falso = new daoImplementacio() {
            @Override
            public Document buscarTrabajadorPorMail(String mail) {
                if (fallo != null) {
                    throw fallo;
                }
                return new Document("mail", mail).append("dni", "11111111H").append("tipo", "monitor");
            }
        };
        interact(new Runnable() {
            @Override
            public void run() {
                try {
                    FXMLLoader cargador = new FXMLLoader(getClass().getResource("/com/mycompany/vista/Monitor.fxml"));
                    cargador.setControllerFactory(new Callback<Class<?>, Object>() {
                        @Override
                        public Object call(Class<?> tipo) {
                            return new MonitorController(falso);
                        }
                    });
                    Parent root = cargador.load();
                    ventana.setScene(new Scene(root));
                    ventana.show();
                } catch (Exception e) {
                    throw new IllegalStateException("No se pudo abrir Monitor.fxml", e);
                }
            }
        });
    }

    /** Comprueba que sin conexión con MongoDB (ConexionException) el panel se abre igual y sin avisos. */
    @Test
    public void sinConexionElPanelSeAbreIgual() {
        fallo = new ConexionException("buscarTrabajadorPorMail", new RuntimeException("Timed out"));

        abrir();

        FxAssert.verifyThat("#cardClases", NodeMatchers.isVisible());
        FxAssert.verifyThat("#cardClientes", NodeMatchers.isVisible());
        Assertions.assertTrue(lookup(".dialog-pane").queryAll().isEmpty(), "No debería salir ningún aviso");
    }

    /** Comprueba que con un error de MongoDB al abrir, el menú del panel sigue funcionando (abre Clases). */
    @Test
    public void conUnErrorAlAbrirElMenuSigueFuncionando() {
        fallo = new DatosException("buscarTrabajadorPorMail", "lectura fallida", new RuntimeException("read error"));
        abrir();

        clickOn("Gestión");
        clickOn("Clases");

        FxAssert.verifyThat("#listClases", NodeMatchers.isVisible());
    }

    /** Comprueba que sin errores el panel se abre con sus dos tarjetas. */
    @Test
    public void sinErroresElPanelSeAbre() {
        abrir();

        FxAssert.verifyThat("#cardClases", NodeMatchers.isVisible());
        Assertions.assertTrue(lookup(".dialog-pane").queryAll().isEmpty());
    }
}
