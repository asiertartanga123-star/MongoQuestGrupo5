package com.mycompany.controlador;

import java.net.InetSocketAddress;
import java.net.Socket;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
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
 * Pruebas del panel de gestión de clases y sesiones (GestorClase.fxml).
 * Ninguna prueba llega a guardar ni a borrar nada en MongoDB.
 */
public class GestionClasesControllerTest extends ApplicationTest {

    // Si MongoDB no está arrancado, se saltan todos los tests de esta clase.
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

    // Se ejecuta antes de cada test: abre el panel de clases.
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/com/mycompany/vista/GestorClase.fxml"));
        stage.setScene(new Scene(root));
        stage.show();
    }

    // Después de cada test se cierran todas las ventanas que se hayan abierto.
    @AfterEach
    public void cerrarVentanas() throws Exception {
        FxToolkit.cleanupStages();
    }

    // ---------- Ayudas ----------

    // Selecciona la primera clase de la lista (se omite el test si no hay ninguna).
    private Document seleccionarPrimeraClase() {
        final ListView<Document> lista = lookup("#listClases").query();
        Assumptions.assumeTrue(!lista.getItems().isEmpty(), "No hay clases en la base de datos");
        interact(new Runnable() {
            @Override
            public void run() {
                lista.getSelectionModel().selectFirst();
            }
        });
        return lista.getSelectionModel().getSelectedItem();
    }

    // Selecciona la primera clase que tenga sesiones y su primera sesión (se omite si no hay).
    private Document seleccionarPrimeraSesion() {
        final ListView<Document> clases = lookup("#listClases").query();
        final ListView<Document> sesiones = lookup("#listSesiones").query();
        for (int i = 0; i < clases.getItems().size(); i++) {
            final int indice = i;
            interact(new Runnable() {
                @Override
                public void run() {
                    clases.getSelectionModel().select(indice);
                }
            });
            if (!sesiones.getItems().isEmpty()) {
                interact(new Runnable() {
                    @Override
                    public void run() {
                        sesiones.getSelectionModel().selectFirst();
                    }
                });
                return sesiones.getSelectionModel().getSelectedItem();
            }
        }
        Assumptions.abort("Ninguna clase tiene sesiones en la base de datos");
        return null;
    }

    // Escribe un texto en un campo y pulsa Enter en él, que es como se guarda en esta pantalla.
    private void escribirYPulsarEnter(String selector, final String texto) {
        final TextField campo = lookup(selector).query();
        interact(new Runnable() {
            @Override
            public void run() {
                campo.setText(texto);
            }
        });
        clickOn(campo);
        type(KeyCode.ENTER);
    }

    // Pulsa el botón de un diálogo por su función (Aceptar, Cancelar, No…), sin depender del idioma.
    private void pulsarBoton(ButtonBar.ButtonData funcion) {
        for (Button b : lookup(".dialog-pane .button").queryAllAs(Button.class)) {
            if (ButtonBar.getButtonData(b) == funcion) {
                clickOn(b);
                return;
            }
        }
        Assertions.fail("No se encontró el botón " + funcion + " en el diálogo");
    }

    // ---------- Tests: clases ----------

    @Test
    public void alAbrirLaPantallaLosControlesDeEdicionEstanDesactivados() {
        FxAssert.verifyThat("#btnCrearClase", NodeMatchers.isEnabled());
        FxAssert.verifyThat("#btnGestionarSesiones", NodeMatchers.isDisabled());
        FxAssert.verifyThat("#btnBorrarClase", NodeMatchers.isDisabled());
        FxAssert.verifyThat("#txtNombreClase", NodeMatchers.isDisabled());
        FxAssert.verifyThat("#btnBorrarSesion", NodeMatchers.isDisabled());
        FxAssert.verifyThat("#txtAforo", NodeMatchers.isDisabled());
    }

    @Test
    public void alElegirUnaClaseSeRellenaLaFichaYSeActivanLosBotones() {
        Document clase = seleccionarPrimeraClase();

        TextField nombre = lookup("#txtNombreClase").query();
        ChoiceBox<Document> sala = lookup("#choSala").query();
        Label titulo = lookup("#lblClase").query();
        Assertions.assertEquals(clase.getString("nombre"), nombre.getText());
        Assertions.assertEquals("Datos " + clase.getString("nombre"), titulo.getText());
        Assertions.assertNotNull(sala.getValue(), "La sala debería estar elegida");
        Assertions.assertEquals(clase.getObjectId("salaid"), sala.getValue().getObjectId("_id"));

        FxAssert.verifyThat("#btnGestionarSesiones", NodeMatchers.isEnabled());
        FxAssert.verifyThat("#btnBorrarClase", NodeMatchers.isEnabled());
    }

    @Test
    public void lasSesionesListadasSonDeLaClaseElegida() {
        Document clase = seleccionarPrimeraClase();
        ListView<Document> sesiones = lookup("#listSesiones").query();

        for (Document s : sesiones.getItems()) {
            Assertions.assertEquals(clase.getObjectId("_id"), s.getObjectId("claseid"),
                    "Aparece una sesión de otra clase");
        }
    }

    @Test
    public void guardarUnaClaseConElNombreVacioMuestraUnAviso() {
        seleccionarPrimeraClase();
        escribirYPulsarEnter("#txtNombreClase", "");

        // Sale el aviso "La clase necesita nombre y sala." y no se toca la base de datos.
        FxAssert.verifyThat(".dialog-pane", NodeMatchers.isVisible());
        type(KeyCode.ENTER);
    }

    @Test
    public void cancelarElAltaNoAnadeNingunaClase() {
        ListView<Document> lista = lookup("#listClases").query();
        int antes = lista.getItems().size();

        clickOn("#btnCrearClase");
        FxAssert.verifyThat(".dialog-pane", NodeMatchers.isVisible());
        pulsarBoton(ButtonBar.ButtonData.CANCEL_CLOSE);

        Assertions.assertEquals(antes, lista.getItems().size(), "No debería haberse añadido ninguna clase");
    }

    @Test
    public void crearUnaClaseSinNombreMuestraUnAvisoYNoLaAnade() {
        ListView<Document> lista = lookup("#listClases").query();
        int antes = lista.getItems().size();

        clickOn("#btnCrearClase");
        pulsarBoton(ButtonBar.ButtonData.OK_DONE);

        FxAssert.verifyThat(".dialog-pane", NodeMatchers.isVisible());
        type(KeyCode.ENTER);
        Assertions.assertEquals(antes, lista.getItems().size(), "No debería haberse añadido ninguna clase");
    }

    @Test
    public void responderNoAlBorrarUnaClaseNoLaElimina() {
        seleccionarPrimeraClase();
        ListView<Document> lista = lookup("#listClases").query();
        int antes = lista.getItems().size();

        clickOn("#btnBorrarClase");
        FxAssert.verifyThat(".dialog-pane", NodeMatchers.isVisible());
        pulsarBoton(ButtonBar.ButtonData.NO);

        Assertions.assertEquals(antes, lista.getItems().size(), "No debería haberse eliminado ninguna clase");
    }

    @Test
    public void gestionarSesionesAbreElHorarioDeLaClase() {
        Document clase = seleccionarPrimeraClase();

        clickOn("#btnGestionarSesiones");

        FxAssert.verifyThat("#gridHorario", NodeMatchers.isVisible());
        Label titulo = lookup("#lblTitulo").query();
        Assertions.assertEquals(clase.getString("nombre"), titulo.getText());
    }

    // ---------- Tests: sesiones ----------

    @Test
    public void alElegirUnaSesionSeRellenaLaFicha() {
        Document sesion = seleccionarPrimeraSesion();

        TextField aforo = lookup("#txtAforo").query();
        TextField fecha = lookup("#txtNombreSesion").query();
        Assertions.assertEquals(String.valueOf(sesion.getInteger("plazaMax", 0)), aforo.getText());
        Assertions.assertFalse(fecha.getText().isEmpty(), "La fecha debería estar rellenada");
        FxAssert.verifyThat("#btnBorrarSesion", NodeMatchers.isEnabled());
    }

    @Test
    public void guardarUnaFechaSinFormatoMuestraUnAviso() {
        seleccionarPrimeraSesion();
        escribirYPulsarEnter("#txtNombreSesion", "mañana por la tarde");

        // Debe ser dd/MM/yyyy HH:mm: sale el aviso y no se guarda.
        FxAssert.verifyThat(".dialog-pane", NodeMatchers.isVisible());
        type(KeyCode.ENTER);
    }

    @Test
    public void guardarUnAforoQueNoEsNumeroMuestraUnAviso() {
        seleccionarPrimeraSesion();
        escribirYPulsarEnter("#txtAforo", "veinte");

        FxAssert.verifyThat(".dialog-pane", NodeMatchers.isVisible());
        type(KeyCode.ENTER);
    }

    @Test
    public void guardarUnAforoMayorQueLaSalaMuestraUnAviso() {
        seleccionarPrimeraSesion();
        escribirYPulsarEnter("#txtAforo", "100000");

        FxAssert.verifyThat(".dialog-pane", NodeMatchers.isVisible());
        type(KeyCode.ENTER);
    }

    @Test
    public void guardarUnAforoDeCeroMuestraUnAviso() {
        seleccionarPrimeraSesion();
        escribirYPulsarEnter("#txtAforo", "0");

        FxAssert.verifyThat(".dialog-pane", NodeMatchers.isVisible());
        type(KeyCode.ENTER);
    }

    @Test
    public void responderNoAlBorrarUnaSesionNoLaElimina() {
        seleccionarPrimeraSesion();
        ListView<Document> sesiones = lookup("#listSesiones").query();
        int antes = sesiones.getItems().size();

        clickOn("#btnBorrarSesion");
        FxAssert.verifyThat(".dialog-pane", NodeMatchers.isVisible());
        pulsarBoton(ButtonBar.ButtonData.NO);

        Assertions.assertEquals(antes, sesiones.getItems().size(), "No debería haberse eliminado ninguna sesión");
    }

    // ---------- Tests: navegación ----------

    @Test
    public void pulsarElTituloVuelveAlPanelDelGerente() {
        // El destino depende del rol en sesión, que otro test puede haber dejado en "monitor".
        LoginController.rolActual = "gerente";
        clickOn("#lblTitulo");
        FxAssert.verifyThat("#cardClases", NodeMatchers.isVisible());
    }

    @Test
    public void cerrarSesionAbreElLogin() {
        clickOn("#btnCerrarSesion");
        FxAssert.verifyThat("#txtUsuario", NodeMatchers.isVisible());
    }
}
