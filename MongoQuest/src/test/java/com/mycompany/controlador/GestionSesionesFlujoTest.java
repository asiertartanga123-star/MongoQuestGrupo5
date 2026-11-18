package com.mycompany.controlador;

import com.mycompany.controlador.dao.daoImplementacio;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;

/**
 * Flujo completo del gestor de sesiones contra MongoDB: añadir, editar y borrar una sesión.
 * Trabaja sobre una clase temporal que se crea al empezar y se borra al acabar
 * (con sus sesiones), para no tocar los datos reales.
 */
public class GestionSesionesFlujoTest extends ApplicationTest {

    private static final daoImplementacio DAO = new daoImplementacio();
    /** Clase temporal de la prueba. */
    private static Document clase;

    // Si MongoDB no está arrancado o faltan salas o monitores, se salta la prueba.
    @BeforeAll
    public static void crearClaseTemporal() {
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
        Assumptions.assumeTrue(!DAO.listarSalas().isEmpty(), "No hay salas en la base de datos");
        Assumptions.assumeTrue(!DAO.listarMonitores().isEmpty(), "No hay monitores en la base de datos");

        clase = new Document("nombre", "PRUEBA horario " + System.currentTimeMillis())
                .append("horas", new ArrayList<Document>())
                .append("salaid", DAO.listarSalas().get(0).getObjectId("_id"));
        DAO.insertarClase(clase);
    }

    // Borra la clase temporal y todas sus sesiones.
    @AfterAll
    public static void borrarClaseTemporal() {
        if (clase != null) {
            DAO.eliminarClase(clase.getObjectId("_id"));
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader cargador = new FXMLLoader(getClass().getResource("/com/mycompany/vista/GestorSesiones.fxml"));
        Parent root = cargador.load();
        GestionSesionesController controlador = cargador.getController();
        controlador.setClase(clase);
        stage.setScene(new Scene(root));
        stage.show();
    }

    @AfterEach
    public void cerrarVentanas() throws Exception {
        FxToolkit.cleanupStages();
    }

    // ---------- Ayudas ----------

    // Hueco del lunes a las 9:00.
    private StackPane huecoLunes9() {
        GridPane grid = lookup("#gridHorario").query();
        for (Node n : grid.getChildren()) {
            if ("1,9".equals(n.getUserData())) {
                return (StackPane) n;
            }
        }
        Assertions.fail("No se encontró el hueco del lunes a las 9:00");
        return null;
    }

    private void pulsarBoton(ButtonBar.ButtonData funcion) {
        for (Button b : lookup(".dialog-pane .button").queryAllAs(Button.class)) {
            if (ButtonBar.getButtonData(b) == funcion) {
                clickOn(b);
                return;
            }
        }
        Assertions.fail("No se encontró el botón " + funcion + " en el diálogo");
    }

    // Elige el primer monitor del diálogo y escribe el aforo.
    private void rellenarDialogo(final String aforo) {
        final ChoiceBox<Document> monitor = lookup(".dialog-pane .choice-box").query();
        final TextField campo = lookup(".dialog-pane .text-field").query();
        interact(new Runnable() {
            @Override
            public void run() {
                monitor.getSelectionModel().selectFirst();
                campo.setText(aforo);
            }
        });
    }

    // Sesiones de la clase temporal que aún no se han celebrado.
    private List<Document> sesionesFuturas() {
        List<Document> futuras = new ArrayList<Document>();
        for (Document s : DAO.listarSesiones(clase.getObjectId("_id"))) {
            if (s.getDate("fecha").after(new Date())) {
                futuras.add(s);
            }
        }
        return futuras;
    }

    // Horario de la clase temporal tal como está guardado en MongoDB.
    private List<Document> horasEnMongo() {
        ObjectId id = clase.getObjectId("_id");
        for (Document c : DAO.listarClases()) {
            if (c.getObjectId("_id").equals(id)) {
                return c.getList("horas", Document.class);
            }
        }
        Assertions.fail("La clase temporal ya no está en MongoDB");
        return null;
    }

    // ---------- Test ----------

    @Test
    public void anadirEditarYBorrarUnaSesion() {
        StackPane hueco = huecoLunes9();

        // 1. Añadir: hueco libre -> monitor y aforo 5 -> Guardar
        clickOn(hueco);
        rellenarDialogo("5");
        pulsarBoton(ButtonBar.ButtonData.OK_DONE);

        Assertions.assertFalse(hueco.getChildren().isEmpty(), "El hueco debería pintarse como ocupado");
        Assertions.assertEquals(1, horasEnMongo().size(), "El horario debería tener una franja");
        Assertions.assertEquals("lunes", horasEnMongo().get(0).getString("dia"));
        Assertions.assertEquals("09:00", horasEnMongo().get(0).getString("horainicio"));
        Assertions.assertEquals(1, sesionesFuturas().size(), "Debería haberse creado una sesión");
        Assertions.assertEquals(5, sesionesFuturas().get(0).getInteger("plazaMax").intValue());

        // 2. Editar: hueco ocupado -> el diálogo trae el aforo 5 -> cambiar a 7 -> Guardar
        clickOn(hueco);
        TextField campo = lookup(".dialog-pane .text-field").query();
        Assertions.assertEquals("5", campo.getText(), "El diálogo debería traer el aforo actual");
        rellenarDialogo("7");
        pulsarBoton(ButtonBar.ButtonData.OK_DONE);

        Assertions.assertEquals(1, sesionesFuturas().size(), "Editar no debería crear otra sesión");
        Assertions.assertEquals(7, sesionesFuturas().get(0).getInteger("plazaMax").intValue());

        // 3. Borrar: hueco ocupado -> Borrar -> Sí
        clickOn(hueco);
        pulsarBoton(ButtonBar.ButtonData.LEFT);
        pulsarBoton(ButtonBar.ButtonData.YES);

        Assertions.assertTrue(hueco.getChildren().isEmpty(), "El hueco debería quedar libre");
        Assertions.assertTrue(horasEnMongo().isEmpty(), "La franja debería haber salido del horario");
        Assertions.assertTrue(sesionesFuturas().isEmpty(), "La sesión debería haberse borrado");
    }
}
