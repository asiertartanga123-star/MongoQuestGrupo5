package com.mycompany.controlador;

import java.net.InetSocketAddress;
import java.net.Socket;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxAssert;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;

public class LoginControllerTest extends ApplicationTest {

    // Se ejecuta antes de cada test: abre la pantalla de login.
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/com/mycompany/vista/Login.fxml"));
        stage.setScene(new Scene(root));
        stage.show();
    }

    // Después de cada test se cierran todas las ventanas que haya abierto el login.
    @AfterEach
    public void cerrarVentanas() throws Exception {
        FxToolkit.cleanupStages();
    }

    // ---------- Ayudas ----------

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
    public void elEnlaceRegistrarseAbreLaPantallaDeRegistro() {
        clickOn("#linkRegistro");

        // Si aparece el campo del nombre, se ha cargado Register.fxml.
        FxAssert.verifyThat("#txtNombre", NodeMatchers.isVisible());
        FxAssert.verifyThat("#btnPagar", NodeMatchers.isVisible());
    }

    // ---------- Tests que necesitan MongoDB con los usuarios de ejemplo ----------

    @Test
    public void conUnaContraseñaIncorrectaMuestraElAvisoYSigueEnElLogin() {
        Assumptions.assumeTrue(mongoActivo(), "MongoDB no está arrancado en localhost:27017");

        iniciarSesion("admin@admin.com", "contraseña-equivocada");

        FxAssert.verifyThat(".dialog-pane", NodeMatchers.isVisible());
        type(KeyCode.ENTER);

        // Tras cerrar el aviso, el formulario de login sigue en pantalla.
        FxAssert.verifyThat("#btnLogin", NodeMatchers.isVisible());
    }

    @Test
    public void conUnUsuarioQueNoExisteMuestraElAviso() {
        Assumptions.assumeTrue(mongoActivo(), "MongoDB no está arrancado en localhost:27017");

        iniciarSesion("nadie@noexiste.com", "1234");

        FxAssert.verifyThat(".dialog-pane", NodeMatchers.isVisible());
        type(KeyCode.ENTER);
        FxAssert.verifyThat("#btnLogin", NodeMatchers.isVisible());
    }

    @Test
    public void elGerenteCorrectoEntraAlPanelDelGerente() {
        Assumptions.assumeTrue(mongoActivo(), "MongoDB no está arrancado en localhost:27017");

        iniciarSesion("admin@admin.com", "admin");

        // Si se ve alguna tarjeta, se ha abierto Gerente.fxml.
        FxAssert.verifyThat("#cardClientes", NodeMatchers.isVisible());
        FxAssert.verifyThat("#cardMonitores", NodeMatchers.isVisible());
    }
}