package com.mycompany.controlador;

import javafx.scene.control.ListCell;
import java.net.InetSocketAddress;
import java.net.Socket;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.bson.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxAssert;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;

/**
 * Pruebas TestFX de la gestión de clientes (GestorCliente.fxml): la ficha al elegir
 * un cliente, los avisos de los datos no válidos y el alta cancelada.
 */
public class GestionClientesControllerTest extends ApplicationTest {

    /** Crea la clase de pruebas; JUnit crea una instancia para cada test. */
    public GestionClientesControllerTest() {
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

    // Se ejecuta antes de cada test: abre la pantalla de gestión de clientes.
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/com/mycompany/vista/GestorCliente.fxml"));
        stage.setScene(new Scene(root));
        stage.show();
    }

    // ---------- Ayudas ----------

    /** Selecciona el primer cliente de la lista (se omite el test si no hay clientes). */
    private void seleccionarPrimerCliente() {
        final ListView<Document> lista = lookup("#listClientes").query();
        Assumptions.assumeTrue(!lista.getItems().isEmpty(), "No hay clientes en la base de datos");
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

    /** Comprueba que al elegir un cliente se rellena la ficha y se activan los botones. */
    @Test
    public void alElegirUnClienteSeRellenaLaFichaYSeActivanLosBotones() {
        seleccionarPrimerCliente();

        TextField nombre = lookup("#txtNombre").query();
        TextField dni = lookup("#txtDni").query();
        Assertions.assertFalse(nombre.getText().isEmpty(), "El nombre debería estar rellenado");
        Assertions.assertFalse(dni.getText().isEmpty(), "El DNI debería estar rellenado");

        FxAssert.verifyThat("#btnGuardar", NodeMatchers.isEnabled());
        FxAssert.verifyThat("#btnBaja", NodeMatchers.isEnabled());
    }

    /** Comprueba que guardar con el nombre vacío muestra un aviso. */
    @Test
    public void guardarConElNombreVacioMuestraUnAviso() {
        seleccionarPrimerCliente();
        escribir("#txtNombre", "");

        clickOn("#btnGuardar");

        // Sale el aviso "Rellena todos los campos." y no se toca la base de datos.
        FxAssert.verifyThat(".dialog-pane", NodeMatchers.isVisible());
        type(KeyCode.ENTER);
    }

    /** Comprueba que guardar con un mail no válido muestra un aviso. */
    @Test
    public void guardarConUnMailNoValidoMuestraUnAviso() {
        seleccionarPrimerCliente();
        escribir("#txtMail", "esto-no-es-un-mail");

        clickOn("#btnGuardar");

        FxAssert.verifyThat(".dialog-pane", NodeMatchers.isVisible());
        type(KeyCode.ENTER);
    }

    /** Comprueba que cancelar el alta no añade ningún cliente. */
    @Test
    public void cancelarElAltaNoAnadeNingunCliente() {
        final ListView<Document> lista = lookup("#listClientes").query();
        int antes = lista.getItems().size();

        clickOn("#btnAlta");
        FxAssert.verifyThat(".dialog-pane", NodeMatchers.isVisible());

        type(KeyCode.ESCAPE);   // cierra el formulario sin guardar

        Assertions.assertEquals(antes, lista.getItems().size(), "No debería haberse añadido ningún cliente");
    }

    /** Comprueba que pulsar el título vuelve al panel del gerente. */
     @Test
    public void pulsarElTituloVuelveAlPanelDelGerente() {
        clickOn("#lblTitulo");
        FxAssert.verifyThat("#cardMonitores", NodeMatchers.isVisible());
    }
}