package com.mycompany.controlador;

import javafx.scene.control.ListCell;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Set;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxAssert;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;

/**
 * Pruebas TestFX de la gestión de monitores (GestorMonitor.fxml): la ficha al elegir
 * un monitor, los avisos de los datos no válidos, el alta cancelada y la baja con «No».
 */
public class GestionMonitorControllerTest extends ApplicationTest {

    /** Crea la clase de pruebas; JUnit crea una instancia para cada test. */
    public GestionMonitorControllerTest() {
    }

    /** Si MongoDB no está arrancado, se saltan todos los tests de esta clase. */
    @BeforeAll
    public static void comprobarMongo() {
        boolean activo = false;
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress("localhost", 27017), 500);
            activo = true;
        } catch (Exception e) {
            activo = false;
        } finally {
            try {
                socket.close();
            } catch (Exception e) {
                // no hace falta hacer nada
            }
        }
        Assumptions.assumeTrue(activo, "MongoDB no está arrancado en localhost:27017");
    }

    // Se ejecuta antes de cada test: abre la pantalla de gestión de monitores.
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/com/mycompany/vista/GestorMonitor.fxml"));
        stage.setScene(new Scene(root));
        stage.show();
    }

    /**
     * Después de cada test se cierran todas las ventanas que se hayan abierto.
     *
     * @throws Exception si TestFX no puede abrir, esperar o cerrar una ventana
     */
    @AfterEach
    public void cerrarVentanas() throws Exception {
        FxToolkit.cleanupStages();
    }

    // ---------- Ayudas ----------

    /** Selecciona el primer monitor de la lista (se omite el test si no hay ninguno). */
    private void seleccionarPrimerMonitor() {
        final ListView<Document> lista = lookup("#listMonitores").query();
        Assumptions.assumeTrue(!lista.getItems().isEmpty(), "No hay monitores en la base de datos");
        pulsarFila(lista, 0);
    }

    /**
     * Escribe un texto en un campo, sustituyendo el que hubiera.
     *
     * @param selector selector CSS del campo
     * @param texto texto que se escribe o que debe decir el aviso
     */
    private void escribir(String selector, final String texto) {
        final TextField campo = lookup(selector).query();
        teclear(campo, texto);
    }

    /** Pulsa el botón "No" de una confirmación, sin depender del idioma del texto. */
    private void pulsarNoEnLaConfirmacion() {
        Set<Button> botones = lookup(".dialog-pane .button").queryAllAs(Button.class);
        for (Button b : botones) {
            if (ButtonBar.getButtonData(b) == ButtonBar.ButtonData.NO) {
                clickOn(b);
                return;
            }
        }
        Assertions.fail("No se encontró el botón No en la confirmación");
    }

    // ---------- Tests ----------

    /**
     * Pulsa con el ratón la fila «indice» de una lista, como haría el usuario.
     *
     * @param lista lista en la que se pulsa
     * @param indice posición de la fila, empezando en 0
     */
    private void pulsarFila(final ListView<Document> lista, final int indice) {
        interact(new Runnable() {
            @Override
            public void run() {
                lista.scrollTo(indice);
            }
        });
        for (ListCell<?> celda : lista.lookupAll(".list-cell").toArray(new ListCell<?>[0])) {
            if (celda.getIndex() == indice && celda.isVisible()) {
                clickOn(celda);
                return;
            }
        }
        Assertions.fail("No se ve la fila " + indice + " de la lista");
    }

    /**
     * Sustituye el texto de un campo escribiéndolo con el teclado.
     *
     * @param campo campo de texto
     * @param texto texto que se escribe o que debe decir el aviso
     */
    private void teclear(TextField campo, String texto) {
        clickOn(campo);
        push(KeyCode.CONTROL, KeyCode.A);
        type(KeyCode.BACK_SPACE);
        if (!texto.isEmpty()) {
            write(texto);
        }
    }

    /** Comprueba que al abrir la pantalla los botones de edición están desactivados. */
    @Test
    public void alAbrirLaPantallaLosBotonesDeEdicionEstanDesactivados() {
        FxAssert.verifyThat("#btnGuardar", NodeMatchers.isDisabled());
        FxAssert.verifyThat("#btnBaja", NodeMatchers.isDisabled());
        FxAssert.verifyThat("#btnAlta", NodeMatchers.isEnabled());
    }

    /** Comprueba que la lista solo contiene monitores. */
    @Test
    public void laListaSoloContieneMonitores() {
        final ListView<Document> lista = lookup("#listMonitores").query();
        Assumptions.assumeTrue(!lista.getItems().isEmpty(), "No hay monitores en la base de datos");

        for (Document d : lista.getItems()) {
            Assertions.assertEquals("monitor", d.getString("tipo"),
                    "Aparece alguien que no es monitor: " + d.getString("mail"));
        }
    }

    /** Comprueba que al elegir un monitor se rellena la ficha y se activan los botones. */
    @Test
    public void alElegirUnMonitorSeRellenaLaFichaYSeActivanLosBotones() {
        seleccionarPrimerMonitor();

        TextField nombre = lookup("#txtNombre").query();
        TextField dni = lookup("#txtDni").query();
        TextField direccion = lookup("#txtDireccion").query();
        Assertions.assertFalse(nombre.getText().isEmpty(), "El nombre debería estar rellenado");
        Assertions.assertFalse(dni.getText().isEmpty(), "El DNI debería estar rellenado");
        Assertions.assertFalse(direccion.getText().isEmpty(), "La dirección debería estar rellenada");

        FxAssert.verifyThat("#btnGuardar", NodeMatchers.isEnabled());
        FxAssert.verifyThat("#btnBaja", NodeMatchers.isEnabled());
    }

    /** Comprueba que guardar con el nombre vacío muestra un aviso. */
    @Test
    public void guardarConElNombreVacioMuestraUnAviso() {
        seleccionarPrimerMonitor();
        escribir("#txtNombre", "");

        clickOn("#btnGuardar");

        // Sale el aviso "Rellena todos los campos." y no se toca la base de datos.
        FxAssert.verifyThat(".dialog-pane", NodeMatchers.isVisible());
        type(KeyCode.ENTER);
    }

    /** Comprueba que guardar con un mail no válido muestra un aviso. */
    @Test
    public void guardarConUnMailNoValidoMuestraUnAviso() {
        seleccionarPrimerMonitor();
        escribir("#txtMail", "esto-no-es-un-mail");

        clickOn("#btnGuardar");

        FxAssert.verifyThat(".dialog-pane", NodeMatchers.isVisible());
        type(KeyCode.ENTER);
    }

    /** Comprueba que guardar con una dirección sin el formato correcto muestra un aviso. */
    @Test
    public void guardarConUnaDireccionSinElFormatoCorrectoMuestraUnAviso() {
        seleccionarPrimerMonitor();
        // Debe ser "Calle, CP, Ciudad": con una sola parte el aviso tiene que salir.
        escribir("#txtDireccion", "solo una parte");

        clickOn("#btnGuardar");

        FxAssert.verifyThat(".dialog-pane", NodeMatchers.isVisible());
        type(KeyCode.ENTER);
    }

    /** Comprueba que guardar con una dirección con partes vacías muestra un aviso. */
    @Test
    public void guardarConUnaDireccionConPartesVaciasMuestraUnAviso() {
        seleccionarPrimerMonitor();
        escribir("#txtDireccion", "Calle Mayor 5, , Bilbao");

        clickOn("#btnGuardar");

        FxAssert.verifyThat(".dialog-pane", NodeMatchers.isVisible());
        type(KeyCode.ENTER);
    }

    /** Comprueba que cancelar el alta no añade ningún monitor. */
    @Test
    public void cancelarElAltaNoAnadeNingunMonitor() {
        final ListView<Document> lista = lookup("#listMonitores").query();
        int antes = lista.getItems().size();

        clickOn("#btnAlta");
        FxAssert.verifyThat(".dialog-pane", NodeMatchers.isVisible());

        type(KeyCode.ESCAPE);   // cierra el formulario sin guardar

        Assertions.assertEquals(antes, lista.getItems().size(), "No debería haberse añadido ningún monitor");
    }

    /** Comprueba que responder «No» al dar de baja no elimina al monitor. */
    @Test
    public void respondeNoAlDarDeBajaNoEliminaAlMonitor() {
        seleccionarPrimerMonitor();
        final ListView<Document> lista = lookup("#listMonitores").query();
        int antes = lista.getItems().size();

        clickOn("#btnBaja");
        FxAssert.verifyThat(".dialog-pane", NodeMatchers.isVisible());
        pulsarNoEnLaConfirmacion();

        Assertions.assertEquals(antes, lista.getItems().size(), "No debería haberse eliminado nadie");
        FxAssert.verifyThat("#btnBaja", NodeMatchers.isEnabled());
    }

    /** Comprueba que pulsar el título vuelve al panel del gerente. */
    @Test
    public void pulsarElTituloVuelveAlPanelDelGerente() {
        clickOn("#lblTitulo");
        FxAssert.verifyThat("#cardMonitores", NodeMatchers.isVisible());
    }

    /** Comprueba que cerrar sesión abre el login. */
    @Test
    public void cerrarSesionAbreElLogin() {
        clickOn("#btnCerrarSesion");
        FxAssert.verifyThat("#txtUsuario", NodeMatchers.isVisible());
    }
}