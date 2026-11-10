package com.mycompany.controlador;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.DialogPane;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxAssert;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;
import org.testfx.util.WaitForAsyncUtils;

public class LoginControllerTest extends ApplicationTest {

    // Se ejecuta antes de cada test: abre la pantalla de login.
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/com/mycompany/vista/Login.fxml"));
        stage.setScene(new Scene(root));
        stage.show();
    }

    // Después de cada test: se cierran los avisos que hayan quedado abiertos y todas las ventanas.
    @AfterEach
    public void cerrarVentanas() throws Exception {
        cerrarAvisosAbiertos();
        FxToolkit.cleanupStages();
    }

    // ---------- Ayudas ----------

    // Cierra cualquier Alert o Dialog que siga en pantalla (cleanupStages no los cierra).
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

    // Devuelve true si MongoDB responde en localhost:27017.
    private boolean mongoActivo() {
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress("localhost", 27017), 500);
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            try {
                socket.close();
            } catch (Exception e) {
                // no hace falta hacer nada
            }
        }
    }

    // Espera hasta 5 segundos a que aparezca un elemento (cargar una pantalla o consultar MongoDB tarda).
    private void esperar(final String selector) throws Exception {
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, new Callable<Boolean>() {
            @Override
            public Boolean call() {
                return lookup(selector).tryQuery().isPresent();
            }
        });
    }

    // Rellena el mail y la contraseña y pulsa "Iniciar sesión".
    private void iniciarSesion(String mail, String contraseña) {
        clickOn("#txtUsuario").write(mail);
        clickOn("#txtPassword").write(contraseña);
        clickOn("#btnLogin");
    }

    // ---------- Tests que no necesitan MongoDB ----------

    @Test
    public void alAbrirLaPantallaSeVenLosCamposYLosBotones() {
        FxAssert.verifyThat("#txtUsuario", NodeMatchers.isVisible());
        FxAssert.verifyThat("#txtPassword", NodeMatchers.isVisible());
        FxAssert.verifyThat("#btnLogin", NodeMatchers.isVisible());
        FxAssert.verifyThat("#linkRegistro", NodeMatchers.isVisible());
    }

    @Test
    public void losCamposEmpiezanVaciosYLaContraseñaEstaOculta() {
        TextField usuario = lookup("#txtUsuario").query();
        Assertions.assertEquals("", usuario.getText());

        Object campoPassword = lookup("#txtPassword").query();
        Assertions.assertTrue(campoPassword instanceof PasswordField,
                "El campo de la contraseña debe ser un PasswordField");
    }

    @Test
    public void iniciarSesionConLosCamposVaciosMuestraUnAviso() {
        clickOn("#btnLogin");

        // Sale "Escribe el usuario y la contraseña." y no se consulta la base de datos.
        FxAssert.verifyThat(".dialog-pane", NodeMatchers.isVisible());
        type(KeyCode.ENTER);
    }

    @Test
    public void iniciarSesionSoloConElMailMuestraUnAviso() {
        clickOn("#txtUsuario").write("admin@admin.com");
        clickOn("#btnLogin");

        FxAssert.verifyThat(".dialog-pane", NodeMatchers.isVisible());
        type(KeyCode.ENTER);
    }

    @Test
    public void pulsarEnterConLosCamposVaciosTambienMuestraElAviso() {
        // El botón de login es el botón por defecto, así que Enter equivale a pulsarlo.
        clickOn("#txtUsuario");
        type(KeyCode.ENTER);

        FxAssert.verifyThat(".dialog-pane", NodeMatchers.isVisible());
        type(KeyCode.ENTER);
    }

    @Test
    public void elEnlaceRegistrarseAbreLaPantallaDeRegistro() throws Exception {
        clickOn("#linkRegistro");

        // Se espera a que Register.fxml termine de cargar y se vea el campo del nombre.
        esperar("#txtNombre");
        FxAssert.verifyThat("#txtNombre", NodeMatchers.isVisible());
        FxAssert.verifyThat("#btnPagar", NodeMatchers.isVisible());
    }

    // ---------- Tests que necesitan MongoDB con los usuarios de ejemplo ----------

    @Test
    public void conUnaContraseñaIncorrectaMuestraElAvisoYSigueEnElLogin() throws Exception {
        Assumptions.assumeTrue(mongoActivo(), "MongoDB no está arrancado en localhost:27017");

        iniciarSesion("admin@admin.com", "contraseña-equivocada");

        esperar(".dialog-pane");
        FxAssert.verifyThat(".dialog-pane", NodeMatchers.isVisible());
        type(KeyCode.ENTER);

        // Tras cerrar el aviso, el formulario de login sigue en pantalla.
        FxAssert.verifyThat("#btnLogin", NodeMatchers.isVisible());
    }

    @Test
    public void conUnUsuarioQueNoExisteMuestraElAviso() throws Exception {
        Assumptions.assumeTrue(mongoActivo(), "MongoDB no está arrancado en localhost:27017");

        iniciarSesion("nadie@noexiste.com", "1234");

        esperar(".dialog-pane");
        FxAssert.verifyThat(".dialog-pane", NodeMatchers.isVisible());
        type(KeyCode.ENTER);
        FxAssert.verifyThat("#btnLogin", NodeMatchers.isVisible());
    }

    @Test
    public void elGerenteCorrectoEntraAlPanelDelGerente() throws Exception {
        Assumptions.assumeTrue(mongoActivo(), "MongoDB no está arrancado en localhost:27017");

        iniciarSesion("admin@admin.com", "admin");

        // Se espera a que el panel del gerente termine de abrirse.
        esperar("#cardClientes");
        FxAssert.verifyThat("#cardClientes", NodeMatchers.isVisible());
        FxAssert.verifyThat("#cardMonitores", NodeMatchers.isVisible());
    }
}