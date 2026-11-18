package com.mycompany.controlador;

import com.mycompany.controlador.dao.ConexionException;
import com.mycompany.controlador.dao.DatosException;
import com.mycompany.controlador.dao.DuplicadoException;
import com.mycompany.controlador.dao.daoImplementacio;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
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
 * Pruebas TestFX de cómo trata la pantalla de registro los errores de MongoDB.
 * <p>
 * La pantalla se carga con un acceso a datos falso (una subclase de {@link daoImplementacio})
 * que lanza la excepción que pide cada test: DNI o mail duplicado, sin conexión o un error
 * cualquiera de la base de datos. Así se prueban los {@code catch} del controlador sin
 * depender del estado de MongoDB, y ningún test escribe en la base de datos.
 */
public class RegisterErroresMongoTest extends ApplicationTest {

    /** Error que lanzará {@code insertarCliente} en el test actual; {@code null} = inserta bien. */
    private RuntimeException fallo;
    /** Respuesta de {@code existeMail} en el test actual. */
    private boolean mailYaExiste;
    /** Veces que la pantalla ha llamado a {@code insertarCliente}. */
    private int inserciones;

    /** Crea la clase de pruebas; JUnit crea una instancia para cada test. */
    public RegisterErroresMongoTest() {
    }

    /**
     * Antes de cada test: abre la pantalla de registro con el acceso a datos falso.
     *
     * @param stage ventana que prepara TestFX
     * @throws Exception si no se puede cargar el FXML
     */
    @Override
    public void start(Stage stage) throws Exception {
        final daoImplementacio falso = new daoImplementacio() {
            @Override
            public boolean existeMail(String mail) {
                return mailYaExiste;
            }

            @Override
            public void insertarCliente(Document c) {
                inserciones++;
                if (fallo != null) {
                    throw fallo;
                }
            }
        };
        FXMLLoader cargador = new FXMLLoader(getClass().getResource("/com/mycompany/vista/Register.fxml"));
        cargador.setControllerFactory(new Callback<Class<?>, Object>() {
            @Override
            public Object call(Class<?> tipo) {
                return new RegisterController(falso);
            }
        });
        Parent root = cargador.load();
        stage.setScene(new Scene(root));
        stage.show();
    }

    /**
     * Después de cada test: cierra los avisos que hayan quedado abiertos y todas las ventanas.
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
    }

    // ---------- Ayudas ----------

    /** Rellena el formulario con datos válidos escribiéndolos con el teclado y pulsa «Pagar». */
    private void registrarConDatosValidos() {
        clickOn("#txtNombre").write("Prueba");
        clickOn("#txtApellidos").write("Errores Mongo");
        clickOn("#txtDni").write("12345678Z");
        clickOn("#txtMail").write("errores.mongo@prueba.com");
        clickOn("#txtPassword").write("clave123");
        clickOn("#txtPassword2").write("clave123");
        clickOn("#btnPagar");
    }

    /**
     * Comprueba que hay un aviso, devuelve su mensaje y lo cierra con su botón Aceptar.
     *
     * @return el mensaje del aviso
     */
    private String cerrarAviso() {
        FxAssert.verifyThat(".dialog-pane", NodeMatchers.isVisible());
        DialogPane dialogo = lookup(".dialog-pane").queryAs(DialogPane.class);
        String mensaje = dialogo.getContentText();
        clickOn(dialogo.lookupButton(ButtonType.OK));
        return mensaje;
    }

    // ---------- Tests ----------

    /** Comprueba que un DNI o mail duplicado en MongoDB (DuplicadoException) avisa y no sale del registro. */
    @Test
    public void unDuplicadoEnMongoAvisaYSigueEnElRegistro() {
        fallo = new DuplicadoException("insertarCliente", new RuntimeException("E11000 duplicate key"));

        registrarConDatosValidos();

        Assertions.assertEquals("Ya existe un usuario con ese DNI o ese mail.", cerrarAviso());
        Assertions.assertEquals(1, inserciones);
        FxAssert.verifyThat("#btnPagar", NodeMatchers.isVisible());
    }

    /** Comprueba que sin conexión con MongoDB (ConexionException) avisa de que se arranque. */
    @Test
    public void sinConexionConMongoAvisaQueSeArranque() {
        fallo = new ConexionException("insertarCliente", new RuntimeException("Timed out"));

        registrarConDatosValidos();

        Assertions.assertEquals("No se puede conectar con MongoDB. Comprueba que esté iniciado.", cerrarAviso());
        FxAssert.verifyThat("#btnPagar", NodeMatchers.isVisible());
    }

    /** Comprueba que cualquier otro error de MongoDB (DatosException) muestra un aviso genérico. */
    @Test
    public void otroErrorDeMongoMuestraUnAvisoGenerico() {
        fallo = new DatosException("insertarCliente", "fallo de escritura", new RuntimeException("write error"));

        registrarConDatosValidos();

        Assertions.assertEquals("No se pudo completar el registro.", cerrarAviso());
        FxAssert.verifyThat("#btnPagar", NodeMatchers.isVisible());
    }

    /** Comprueba que si el mail ya está en la base de datos avisa sin intentar insertar. */
    @Test
    public void unMailQueYaExisteAvisaSinInsertar() {
        mailYaExiste = true;

        registrarConDatosValidos();

        Assertions.assertEquals("Ya existe un usuario con ese mail.", cerrarAviso());
        Assertions.assertEquals(0, inserciones, "No debería haber intentado insertar");
    }

    /** Comprueba que si MongoDB guarda bien, informa del registro y vuelve al login. */
    @Test
    public void sinErroresInformaYVuelveAlLogin() {
        registrarConDatosValidos();

        Assertions.assertEquals("Registro completado. Ya puedes iniciar sesión.", cerrarAviso());
        Assertions.assertEquals(1, inserciones);
        FxAssert.verifyThat("#btnLogin", NodeMatchers.isVisible());
    }
}
