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
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
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
import org.testfx.matcher.control.ComboBoxMatchers;
import org.testfx.matcher.control.TextInputControlMatchers;
import org.testfx.util.WaitForAsyncUtils;

/**
 * Tests de la pantalla de registro con el controlador tal cual está (sin DAO falso).
 * Los tests de validación no llegan a la base de datos. El único que la necesita
 * se salta si MongoDB no está arrancado, y no inserta ningún usuario.
 */
public class RegisterControllerTest extends ApplicationTest {

    // Se ejecuta antes de cada test: abre la pantalla de registro.
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/com/mycompany/vista/Register.fxml"));
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

    // Cierra cualquier Alert que siga en pantalla (cleanupStages no los cierra).
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

    

    // Rellena el formulario. El mail se pone con interact() porque la @ puede fallar
    // con write() según la distribución del teclado.
    private void rellenar(String nombre, String apellidos, String dni,
            final String mail, String pass1, String pass2) {
        clickOn("#txtNombre").write(nombre);
        clickOn("#txtApellidos").write(apellidos);
        clickOn("#txtDni").write(dni);
        final TextField campoMail = lookup("#txtMail").query();
        interact(new Runnable() {
            @Override
            public void run() {
                campoMail.setText(mail);
            }
        });
        clickOn("#txtPassword").write(pass1);
        clickOn("#txtPassword2").write(pass2);
    }

    // Comprueba que hay un aviso, devuelve su mensaje y lo cierra.
    // Se pulsa el botón con lookupButton(ButtonType.OK) y no con clickOn("OK"),
    // porque en Windows en castellano el botón dice "Aceptar".
    private String cerrarAviso() throws Exception {
      
        FxAssert.verifyThat(".dialog-pane", NodeMatchers.isVisible());
        DialogPane dialogo = lookup(".dialog-pane").queryAs(DialogPane.class);
        String mensaje = dialogo.getContentText();
        clickOn(dialogo.lookupButton(ButtonType.OK));
        return mensaje;
    }

    // ---------- Estado inicial ----------

    @Test
    public void alAbrirLaPantallaSeVenLosCamposYLosBotones() {
        FxAssert.verifyThat("#txtNombre", NodeMatchers.isVisible());
        FxAssert.verifyThat("#txtApellidos", NodeMatchers.isVisible());
        FxAssert.verifyThat("#txtDni", NodeMatchers.isVisible());
        FxAssert.verifyThat("#txtMail", NodeMatchers.isVisible());
        FxAssert.verifyThat("#txtPassword", NodeMatchers.isVisible());
        FxAssert.verifyThat("#txtPassword2", NodeMatchers.isVisible());
        FxAssert.verifyThat("#cmbNivel", NodeMatchers.isVisible());
        FxAssert.verifyThat("#btnPagar", NodeMatchers.isVisible());
        FxAssert.verifyThat("#linkLogin", NodeMatchers.isVisible());
    }

    @Test
    public void losCamposEmpiezanVaciosYLasContraseñasEstanOcultas() {
        FxAssert.verifyThat("#txtNombre", TextInputControlMatchers.hasText(""));
        FxAssert.verifyThat("#txtApellidos", TextInputControlMatchers.hasText(""));
        FxAssert.verifyThat("#txtDni", TextInputControlMatchers.hasText(""));
        FxAssert.verifyThat("#txtMail", TextInputControlMatchers.hasText(""));

        Object pass1 = lookup("#txtPassword").query();
        Object pass2 = lookup("#txtPassword2").query();
        Assertions.assertTrue(pass1 instanceof PasswordField,
                "La contraseña debe ser un PasswordField");
        Assertions.assertTrue(pass2 instanceof PasswordField,
                "La repetición de la contraseña debe ser un PasswordField");
    }

    @Test
    public void elNivelTieneNormalYPremiumConNormalSeleccionado() {
        FxAssert.verifyThat("#cmbNivel", ComboBoxMatchers.hasItems(2));
        FxAssert.verifyThat("#cmbNivel", ComboBoxMatchers.hasSelectedItem("Normal"));
    }

    @Test
    public void sePuedeElegirElNivelPremium() {
        clickOn("#cmbNivel");
        clickOn("Premium");

        FxAssert.verifyThat("#cmbNivel", ComboBoxMatchers.hasSelectedItem("Premium"));
    }

    // ---------- Validaciones ----------

    @Test
    public void conLosCamposVaciosMuestraUnAviso() throws Exception {
        clickOn("#btnPagar");

        Assertions.assertEquals("Rellena todos los campos.", cerrarAviso());
    }

    @Test
    public void conUnCampoSinRellenarMuestraUnAviso() throws Exception {
       
        rellenar("Ana", "Lopez", "12345678A", "ana@gmail.com", "1234", "");
        clickOn("#btnPagar");

        Assertions.assertEquals("Rellena todos los campos.", cerrarAviso());
    }

    @Test
    public void conUnDniCortoMuestraUnAviso() throws Exception {
        rellenar("Ana", "Lopez", "1234", "ana@gmail.com", "1234", "1234");
        clickOn("#btnPagar");

        Assertions.assertEquals("El DNI debe tener 8 números y una letra (ej. 12345678A).",
                cerrarAviso());
    }

    @Test
    public void conUnDniSinLetraMuestraUnAviso() throws Exception {
        rellenar("Ana", "Lopez", "12345678", "ana@gmail.com", "1234", "1234");
        clickOn("#btnPagar");

        Assertions.assertEquals("El DNI debe tener 8 números y una letra (ej. 12345678A).",
                cerrarAviso());
    }

    @Test
    public void conUnMailSinArrobaMuestraUnAviso() throws Exception {
        rellenar("Ana", "Lopez", "12345678A", "wgegwg", "1234", "1234");
        clickOn("#btnPagar");

        Assertions.assertEquals("Escribe un mail válido (ej. nombre@gmail.com).", cerrarAviso());
    }

    @Test
    public void conUnMailSinExtensionMuestraUnAviso() throws Exception {
        rellenar("Ana", "Lopez", "12345678A", "ana@gmail", "1234", "1234");
        clickOn("#btnPagar");

        Assertions.assertEquals("Escribe un mail válido (ej. nombre@gmail.com).", cerrarAviso());
    }

    @Test
    public void conContraseñasDistintasMuestraUnAviso() throws Exception {
        rellenar("Ana", "Lopez", "12345678A", "ana@gmail.com", "1234", "9999");
        clickOn("#btnPagar");

        Assertions.assertEquals("Las contraseñas no coinciden.", cerrarAviso());
    }

    // ---------- Navegación ----------

    @Test
    public void elEnlaceLoginVuelveALaPantallaDeLogin() throws Exception {
        clickOn("#linkLogin");

        //esperar("#btnLogin");
        FxAssert.verifyThat("#btnLogin", NodeMatchers.isVisible());
    }

    
}