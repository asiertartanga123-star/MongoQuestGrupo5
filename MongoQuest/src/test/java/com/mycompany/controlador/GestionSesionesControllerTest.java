package com.mycompany.controlador;

import com.mycompany.controlador.dao.daoImplementacio;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
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
 * Pruebas de la pantalla del horario semanal de una clase (GestorSesiones.fxml).
 * Usan la primera clase de MongoDB; ninguna prueba llega a guardar cambios.
 */
public class GestionSesionesControllerTest extends ApplicationTest {

    /** Días tal como se guardan en {@code horas.dia}; el índice es la columna menos 1. */
    private static final String[] DIAS = {"lunes", "martes", "miércoles", "jueves", "viernes", "sábado"};

    private static final daoImplementacio DAO = new daoImplementacio();
    /** Clase cuyo horario se abre en cada prueba. */
    private static Document clase;

    // Si MongoDB no está arrancado o no hay clases, se saltan todos los tests de esta clase.
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
        List<Document> clases = DAO.listarClases();
        Assumptions.assumeTrue(!clases.isEmpty(), "No hay clases en la base de datos");
        clase = clases.get(0);
    }

    // Se ejecuta antes de cada test: abre el horario de la primera clase.
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader cargador = new FXMLLoader(getClass().getResource("/com/mycompany/vista/GestorSesiones.fxml"));
        Parent root = cargador.load();
        GestionSesionesController controlador = cargador.getController();
        controlador.setClase(clase);
        stage.setScene(new Scene(root));
        stage.show();
    }

    // Después de cada test se cierran todas las ventanas que se hayan abierto.
    @AfterEach
    public void cerrarVentanas() throws Exception {
        FxToolkit.cleanupStages();
    }

    // ---------- Ayudas ----------

    // Indica si un hueco está pintado como parte del horario (lleva la etiqueta con el nombre).
    private boolean ocupado(StackPane hueco) {
        return !hueco.getChildren().isEmpty();
    }

    // Devuelve el primer hueco libre u ocupado de la rejilla, o null si no hay ninguno.
    private StackPane buscarHueco(boolean queOcupado) {
        GridPane grid = lookup("#gridHorario").query();
        for (Node n : grid.getChildren()) {
            if (n.getStyleClass().contains("hueco-sesion") && ocupado((StackPane) n) == queOcupado) {
                return (StackPane) n;
            }
        }
        return null;
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

    // Número de sesiones que tiene ahora la clase en MongoDB.
    private int sesionesEnMongo() {
        return DAO.listarSesiones(clase.getObjectId("_id")).size();
    }

    // ---------- Tests ----------

    @Test
    public void elTituloEsElNombreDeLaClase() {
        Label titulo = lookup("#lblTitulo").query();
        Assertions.assertEquals(clase.getString("nombre"), titulo.getText());
    }

    @Test
    public void cadaHuecoOcupadoCorrespondeAUnaFranjaDelHorario() {
        List<Document> horas = clase.getList("horas", Document.class);
        GridPane grid = lookup("#gridHorario").query();
        int ocupados = 0;
        for (Node n : grid.getChildren()) {
            if (!n.getStyleClass().contains("hueco-sesion") || !ocupado((StackPane) n)) {
                continue;
            }
            ocupados++;
            String[] pos = ((String) n.getUserData()).split(",");
            String dia = DIAS[Integer.parseInt(pos[0]) - 1];
            String inicio = String.format("%02d:00", Integer.parseInt(pos[1]));
            boolean encontrada = false;
            for (Document f : horas) {
                if (dia.equalsIgnoreCase(f.getString("dia")) && inicio.equals(f.getString("horainicio"))) {
                    encontrada = true;
                }
            }
            Assertions.assertTrue(encontrada, "Hueco pintado sin franja en el horario: " + dia + " " + inicio);
        }
        Assertions.assertEquals(horas == null ? 0 : horas.size(), ocupados,
                "Debería haber un hueco pintado por cada franja del horario");
    }

    @Test
    public void cancelarLaNuevaSesionNoCreaNada() {
        StackPane libre = buscarHueco(false);
        Assumptions.assumeTrue(libre != null, "No hay huecos libres en el horario");
        int antes = sesionesEnMongo();

        clickOn(libre);
        FxAssert.verifyThat(".dialog-pane", NodeMatchers.isVisible());
        pulsarBoton(ButtonBar.ButtonData.CANCEL_CLOSE);

        Assertions.assertFalse(ocupado(libre), "El hueco debería seguir libre");
        Assertions.assertEquals(antes, sesionesEnMongo(), "No debería haberse creado ninguna sesión");
    }

    @Test
    public void aceptarSinMonitorMuestraUnAvisoYNoCreaNada() {
        StackPane libre = buscarHueco(false);
        Assumptions.assumeTrue(libre != null, "No hay huecos libres en el horario");
        int antes = sesionesEnMongo();

        clickOn(libre);
        pulsarBoton(ButtonBar.ButtonData.OK_DONE);

        // Sale el aviso "Elige un monitor." y no se toca la base de datos.
        FxAssert.verifyThat(".dialog-pane", NodeMatchers.isVisible());
        type(KeyCode.ENTER);
        Assertions.assertFalse(ocupado(libre), "El hueco debería seguir libre");
        Assertions.assertEquals(antes, sesionesEnMongo(), "No debería haberse creado ninguna sesión");
    }

    @Test
    public void cancelarLaEdicionDeUnaFranjaLaMantiene() {
        StackPane ocupadoHueco = buscarHueco(true);
        Assumptions.assumeTrue(ocupadoHueco != null, "La clase no tiene franjas en el horario");
        int antes = sesionesEnMongo();

        clickOn(ocupadoHueco);
        FxAssert.verifyThat(".dialog-pane", NodeMatchers.isVisible());
        pulsarBoton(ButtonBar.ButtonData.CANCEL_CLOSE);

        Assertions.assertTrue(ocupado(ocupadoHueco), "La franja debería seguir en el horario");
        Assertions.assertEquals(antes, sesionesEnMongo(), "No debería haber cambiado ninguna sesión");
    }

    @Test
    public void pulsarBorrarYResponderNoMantieneLaFranja() {
        StackPane ocupadoHueco = buscarHueco(true);
        Assumptions.assumeTrue(ocupadoHueco != null, "La clase no tiene franjas en el horario");
        int antes = sesionesEnMongo();

        clickOn(ocupadoHueco);
        pulsarBoton(ButtonBar.ButtonData.LEFT);          // Borrar
        FxAssert.verifyThat(".dialog-pane", NodeMatchers.isVisible());
        pulsarBoton(ButtonBar.ButtonData.NO);

        Assertions.assertTrue(ocupado(ocupadoHueco), "La franja debería seguir en el horario");
        Assertions.assertEquals(antes, sesionesEnMongo(), "No debería haberse borrado ninguna sesión");
    }

    @Test
    public void editarConUnAforoQueNoEsNumeroMuestraUnAviso() {
        StackPane ocupadoHueco = buscarHueco(true);
        Assumptions.assumeTrue(ocupadoHueco != null, "La clase no tiene franjas en el horario");

        clickOn(ocupadoHueco);
        final TextField aforo = lookup(".dialog-pane .text-field").query();
        interact(new Runnable() {
            @Override
            public void run() {
                aforo.setText("muchos");
            }
        });
        pulsarBoton(ButtonBar.ButtonData.OK_DONE);      // Guardar

        FxAssert.verifyThat(".dialog-pane", NodeMatchers.isVisible());
        type(KeyCode.ENTER);
        Assertions.assertTrue(ocupado(ocupadoHueco), "La franja debería seguir en el horario");
    }

    @Test
    public void pulsarElTituloVuelveAlPanelDeClases() {
        clickOn("#lblTitulo");
        FxAssert.verifyThat("#listClases", NodeMatchers.isVisible());
    }

    @Test
    public void cerrarSesionAbreElLogin() {
        clickOn("#btnCerrarSesion");
        FxAssert.verifyThat("#txtUsuario", NodeMatchers.isVisible());
    }
}
