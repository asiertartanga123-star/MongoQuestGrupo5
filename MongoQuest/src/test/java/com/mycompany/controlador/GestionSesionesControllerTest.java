package com.mycompany.controlador;

import com.mycompany.controlador.dao.daoImplementacio;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxAssert;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;
import org.testfx.matcher.control.LabeledMatchers;
import org.testfx.matcher.control.TextInputControlMatchers;

/**
 * Pruebas TestFX de la pantalla del horario semanal de una clase (GestorSesiones.fxml).
 * <p>
 * Todas manejan la pantalla como un usuario: el robot de TestFX pulsa los huecos, elige el
 * monitor en el desplegable, escribe el aforo y pulsa los botones de los diálogos. Después se
 * comprueba la pantalla (huecos pintados, texto de los avisos) y lo guardado en MongoDB.
 * <p>
 * Trabajan sobre una clase temporal que se crea al empezar y se borra al acabar con sus
 * sesiones, para no tocar los datos reales. La clase tiene una franja fija, el martes a las
 * 10:00, con una sesión futura de 5 plazas y 2 clientes apuntados.
 */
public class GestionSesionesControllerTest extends ApplicationTest {

    /** Crea la clase de pruebas; JUnit crea una instancia para cada test. */
    public GestionSesionesControllerTest() {
    }

    /** Acceso a MongoDB para preparar y comprobar los datos de prueba. */
    private static final daoImplementacio DAO = new daoImplementacio();
    /** Hueco de la franja que ya tiene la clase: martes (columna 2) a las 10:00. */
    private static final String HUECO_OCUPADO = "2,10";
    /** Hueco libre para las pruebas de validación: miércoles a las 11:00. */
    private static final String HUECO_LIBRE = "3,11";
    /** Hueco libre para el flujo completo: lunes a las 9:00. */
    private static final String HUECO_FLUJO = "1,9";

    /** Clase temporal sobre la que trabajan las pruebas. */
    private static Document clase;
    /** Nombre y apellido del monitor que se elige en los diálogos (el primero de la lista). */
    private static String monitor;
    /** Aforo máximo de la sala de la clase, o {@code Integer.MAX_VALUE} si la sala no lo tiene. */
    private static int aforoSala;

    // ---------- Preparación y limpieza ----------

    /**
     * Crea la clase temporal con la franja del martes a las 10:00 y su sesión de 5 plazas
     * con 2 clientes apuntados. Las pruebas se omiten si MongoDB no está arrancado.
     */
    @BeforeAll
    public static void crearClaseTemporal() {
        Assumptions.assumeTrue(mongoActivo(), "MongoDB no está arrancado en localhost:27017");
        List<Document> salas = DAO.listarSalas();
        List<Document> monitores = DAO.listarMonitores();
        Assumptions.assumeTrue(!salas.isEmpty(), "No hay salas en la base de datos");
        Assumptions.assumeTrue(!monitores.isEmpty(), "No hay monitores en la base de datos");

        Document sala = salas.get(0);
        aforoSala = sala.getInteger("aforoMaximo", Integer.MAX_VALUE);
        Assumptions.assumeTrue(aforoSala >= 5, "La sala de prueba necesita al menos 5 plazas");
        Document primerMonitor = monitores.get(0);
        monitor = primerMonitor.getString("nombre") + " " + primerMonitor.getString("apellido");

        List<Document> horas = new ArrayList<Document>();
        horas.add(new Document("dia", "martes").append("horainicio", "10:00").append("horafin", "11:00"));
        clase = new Document("nombre", "PRUEBA horario " + System.currentTimeMillis())
                .append("horas", horas)
                .append("salaid", sala.getObjectId("_id"));
        DAO.insertarClase(clase);

        List<Document> apuntados = new ArrayList<Document>();
        apuntados.add(new Document("clienteid", new ObjectId()));
        apuntados.add(new Document("clienteid", new ObjectId()));
        LocalDateTime martes = GestionSesionesController.proximaFecha(LocalDateTime.now(), DayOfWeek.TUESDAY, 10);
        DAO.insertarSesion(new Document("claseid", clase.getObjectId("_id"))
                .append("fecha", Date.from(martes.atZone(ZoneId.systemDefault()).toInstant()))
                .append("plazaMax", 5)
                .append("monitor", primerMonitor.getObjectId("_id"))
                .append("asistentes", apuntados));
    }

    /** Borra la clase temporal y todas sus sesiones. */
    @AfterAll
    public static void borrarClaseTemporal() {
        if (clase != null) {
            DAO.eliminarClase(clase.getObjectId("_id"));
        }
    }

    // Antes de cada test se abre el horario de la clase temporal, con lo que tenga en MongoDB.
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader cargador = new FXMLLoader(getClass().getResource("/com/mycompany/vista/GestorSesiones.fxml"));
        Parent root = cargador.load();
        GestionSesionesController controlador = cargador.getController();
        controlador.setClase(claseEnMongo());
        stage.setScene(new Scene(root));
        stage.show();
    }

    /**
     * Cierra los avisos que hayan quedado abiertos y todas las ventanas después de cada test.
     *
     * @throws Exception si TestFX no puede cerrar las ventanas
     */
    @AfterEach
    public void cerrarVentanas() throws Exception {
        cerrarDialogos();
        FxToolkit.cleanupStages();
    }

    // ---------- Al abrir ----------

    /** Comprueba que al abrir el horario el título es el nombre de la clase. */
    @Test
    public void alAbrir_elTituloEsElNombreDeLaClase() {
        FxAssert.verifyThat("#lblTitulo", LabeledMatchers.hasText(clase.getString("nombre")));
    }

    /** Comprueba que al abrir el horario solo se pinta la franja que tiene la clase. */
    @Test
    public void alAbrir_soloSePintaLaFranjaDeLaClase() {
        Assertions.assertTrue(ocupado(hueco(HUECO_OCUPADO)), "El martes a las 10:00 debería estar pintado");
        int pintados = 0;
        GridPane grid = lookup("#gridHorario").query();
        for (Node n : grid.getChildren()) {
            if (n.getStyleClass().contains("hueco-sesion") && ocupado((StackPane) n)) {
                pintados++;
            }
        }
        Assertions.assertEquals(1, pintados, "Solo debería haber un hueco pintado");
    }

    // ---------- Nueva sesión en un hueco libre ----------

    /** Comprueba que al cancelar el diálogo de una sesión nueva no se crea nada. */
    @Test
    public void nuevaSesion_alCancelarNoSeCreaNada() {
        int antes = sesionesEnMongo();

        clickOn(hueco(HUECO_LIBRE));
        FxAssert.verifyThat(".dialog-pane", NodeMatchers.isVisible());
        pulsarBoton(ButtonBar.ButtonData.CANCEL_CLOSE);

        Assertions.assertFalse(ocupado(hueco(HUECO_LIBRE)), "El hueco debería seguir libre");
        Assertions.assertEquals(antes, sesionesEnMongo(), "No debería haberse creado ninguna sesión");
    }

    /** Comprueba que guardar una sesión nueva sin monitor avisa y no crea nada. */
    @Test
    public void nuevaSesion_sinMonitorAvisaYNoCreaNada() {
        int antes = sesionesEnMongo();

        clickOn(hueco(HUECO_LIBRE));
        escribirAforo("5");
        pulsarBoton(ButtonBar.ButtonData.OK_DONE);

        comprobarAviso("Elige un monitor.");
        Assertions.assertFalse(ocupado(hueco(HUECO_LIBRE)), "El hueco debería seguir libre");
        Assertions.assertEquals(antes, sesionesEnMongo(), "No debería haberse creado ninguna sesión");
    }

    /** Comprueba que guardar una sesión nueva con un aforo que no es un número avisa. */
    @Test
    public void nuevaSesion_aforoQueNoEsNumeroAvisa() {
        clickOn(hueco(HUECO_LIBRE));
        elegirMonitor();
        escribirAforo("muchos");
        pulsarBoton(ButtonBar.ButtonData.OK_DONE);

        comprobarAviso("El aforo debe ser un número.");
        Assertions.assertFalse(ocupado(hueco(HUECO_LIBRE)), "El hueco debería seguir libre");
    }

    /** Comprueba que guardar una sesión nueva con aforo 0 avisa. */
    @Test
    public void nuevaSesion_aforoCeroAvisa() {
        clickOn(hueco(HUECO_LIBRE));
        elegirMonitor();
        escribirAforo("0");
        pulsarBoton(ButtonBar.ButtonData.OK_DONE);

        comprobarAvisoQueEmpiezaPor("El aforo debe estar entre 1 y");
        Assertions.assertFalse(ocupado(hueco(HUECO_LIBRE)), "El hueco debería seguir libre");
    }

    /** Comprueba que guardar una sesión nueva con más aforo que la sala avisa. */
    @Test
    public void nuevaSesion_aforoMayorQueLaSalaAvisa() {
        Assumptions.assumeTrue(aforoSala != Integer.MAX_VALUE, "La sala no tiene aforo máximo");

        clickOn(hueco(HUECO_LIBRE));
        elegirMonitor();
        escribirAforo(String.valueOf(aforoSala + 1));
        pulsarBoton(ButtonBar.ButtonData.OK_DONE);

        comprobarAviso("El aforo debe estar entre 1 y " + aforoSala + " (aforo de la sala).");
        Assertions.assertFalse(ocupado(hueco(HUECO_LIBRE)), "El hueco debería seguir libre");
    }

    // ---------- Franja ocupada ----------

    /** Comprueba que al editar una franja el diálogo trae el monitor y el aforo que tiene. */
    @Test
    public void editar_elDialogoTraeElMonitorYElAforoActuales() {
        clickOn(hueco(HUECO_OCUPADO));

        FxAssert.verifyThat(".dialog-pane .text-field", TextInputControlMatchers.hasText("5"));
        ChoiceBox<Document> desplegable = lookup(".dialog-pane .choice-box").queryAs(ChoiceBox.class);
        Document elegido = desplegable.getValue();
        Assertions.assertNotNull(elegido, "El desplegable debería traer el monitor de la sesión");
        Assertions.assertEquals(monitor, elegido.getString("nombre") + " " + elegido.getString("apellido"));
        pulsarBoton(ButtonBar.ButtonData.CANCEL_CLOSE);
    }

    /** Comprueba que editar una franja con menos aforo que clientes apuntados avisa y no cambia nada. */
    @Test
    public void editar_aforoMenorQueLosApuntadosAvisaYNoCambia() {
        clickOn(hueco(HUECO_OCUPADO));
        escribirAforo("1");
        pulsarBoton(ButtonBar.ButtonData.OK_DONE);

        comprobarAviso("Ya hay 2 clientes apuntados; el aforo no puede ser menor.");
        Assertions.assertEquals(5, aforoDeLaSesionDelMartes(), "El aforo guardado no debería cambiar");
    }

    /** Comprueba que al cancelar la edición de una franja esta se mantiene. */
    @Test
    public void editar_alCancelarSeMantieneLaFranja() {
        int antes = sesionesEnMongo();

        clickOn(hueco(HUECO_OCUPADO));
        pulsarBoton(ButtonBar.ButtonData.CANCEL_CLOSE);

        Assertions.assertTrue(ocupado(hueco(HUECO_OCUPADO)), "La franja debería seguir en el horario");
        Assertions.assertEquals(antes, sesionesEnMongo(), "No debería haber cambiado ninguna sesión");
    }

    /** Comprueba que al borrar una franja y responder «No» esta se mantiene. */
    @Test
    public void borrar_alResponderNoSeMantieneLaFranja() {
        int antes = sesionesEnMongo();

        clickOn(hueco(HUECO_OCUPADO));
        pulsarBoton(ButtonBar.ButtonData.LEFT);           // Borrar
        FxAssert.verifyThat(".dialog-pane .content", NodeMatchers.isVisible());
        pulsarBoton(ButtonBar.ButtonData.NO);

        Assertions.assertTrue(ocupado(hueco(HUECO_OCUPADO)), "La franja debería seguir en el horario");
        Assertions.assertEquals(antes, sesionesEnMongo(), "No debería haberse borrado ninguna sesión");
    }

    // ---------- Flujo completo: añadir, editar y borrar ----------

    /**
     * Recorre desde la pantalla el ciclo completo de una franja: añadir la sesión, editar su
     * aforo y borrarla, comprobando cada paso en el horario y en MongoDB.
     */
    @Test
    public void anadirEditarYBorrarUnaSesionDesdeLaPantalla() {
        StackPane lunes = hueco(HUECO_FLUJO);

        // 1. Añadir: hueco libre -> monitor y aforo 5 -> Guardar
        clickOn(lunes);
        elegirMonitor();
        escribirAforo("5");
        pulsarBoton(ButtonBar.ButtonData.OK_DONE);

        Assertions.assertTrue(ocupado(lunes), "El hueco debería pintarse como ocupado");
        Document franja = franjaEnMongo("lunes", "09:00");
        Assertions.assertNotNull(franja, "El horario debería tener la franja del lunes a las 9:00");
        Assertions.assertEquals("10:00", franja.getString("horafin"));
        List<Document> sesiones = sesionesDelLunes();
        Assertions.assertEquals(1, sesiones.size(), "Debería haberse creado una sesión");
        Assertions.assertEquals(5, sesiones.get(0).getInteger("plazaMax").intValue());
        // La sesión se crea para el próximo lunes a las 9:00.
        LocalDateTime fecha = sesiones.get(0).getDate("fecha").toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        Assertions.assertEquals(DayOfWeek.MONDAY, fecha.getDayOfWeek());
        Assertions.assertEquals(9, fecha.getHour());
        Assertions.assertTrue(fecha.isAfter(LocalDateTime.now()), "La sesión debería ser futura");
        Assertions.assertTrue(fecha.isBefore(LocalDateTime.now().plusDays(8)), "Debería ser el próximo lunes");

        // 2. Editar: el diálogo trae el aforo 5 -> se cambia a 7 -> Guardar
        clickOn(lunes);
        FxAssert.verifyThat(".dialog-pane .text-field", TextInputControlMatchers.hasText("5"));
        escribirAforo("7");
        pulsarBoton(ButtonBar.ButtonData.OK_DONE);

        Assertions.assertEquals(1, sesionesDelLunes().size(), "Editar no debería crear otra sesión");
        Assertions.assertEquals(7, sesionesDelLunes().get(0).getInteger("plazaMax").intValue());

        // 3. Borrar: Borrar -> Sí
        clickOn(lunes);
        pulsarBoton(ButtonBar.ButtonData.LEFT);
        pulsarBoton(ButtonBar.ButtonData.YES);

        Assertions.assertFalse(ocupado(lunes), "El hueco debería quedar libre");
        Assertions.assertNull(franjaEnMongo("lunes", "09:00"), "La franja debería haber salido del horario");
        Assertions.assertTrue(sesionesDelLunes().isEmpty(), "La sesión debería haberse borrado");
    }

    // ---------- Navegación ----------

    /** Comprueba que pulsar el título vuelve al panel de clases. */
    @Test
    public void pulsarElTituloVuelveAlPanelDeClases() {
        clickOn("#lblTitulo");
        FxAssert.verifyThat("#listClases", NodeMatchers.isVisible());
    }

    /** Comprueba que cerrar sesión abre el login. */
    @Test
    public void cerrarSesionAbreElLogin() {
        clickOn("#btnCerrarSesion");
        FxAssert.verifyThat("#txtUsuario", NodeMatchers.isVisible());
    }

    // ---------- Ayudas de pantalla ----------

    /**
     * Hueco de la rejilla cuyo {@code userData} es {@code "columna,hora"}.
     *
     * @param posicion posición del hueco como «columna,hora»
     * @return el hueco de la rejilla
     */
    private StackPane hueco(String posicion) {
        GridPane grid = lookup("#gridHorario").query();
        for (Node n : grid.getChildren()) {
            if (posicion.equals(n.getUserData())) {
                return (StackPane) n;
            }
        }
        Assertions.fail("No se encontró el hueco " + posicion);
        return null;
    }

    /**
     * Un hueco está ocupado si lleva pintada la etiqueta con el nombre de la clase.
     *
     * @param hueco hueco de la rejilla
     * @return {@code true} si el hueco está ocupado
     */
    private boolean ocupado(StackPane hueco) {
        return !hueco.getChildren().isEmpty();
    }

    /** Abre el desplegable del diálogo y pulsa el primer monitor, como haría el usuario. */
    private void elegirMonitor() {
        clickOn(".dialog-pane .choice-box");
        clickOn(monitor);
    }

    /**
     * Borra lo que haya en el campo del aforo y escribe el texto con el teclado.
     *
     * @param texto texto que se escribe o que debe decir el aviso
     */
    private void escribirAforo(String texto) {
        clickOn(".dialog-pane .text-field");
        push(KeyCode.CONTROL, KeyCode.A);
        type(KeyCode.BACK_SPACE);
        write(texto);
    }

    /**
     * Pulsa el botón del diálogo abierto que tiene esa función (Guardar, Cancelar, Sí…).
     *
     * @param funcion función del botón (guardar, cancelar, sí, no…)
     */
    private void pulsarBoton(ButtonBar.ButtonData funcion) {
        for (Button b : lookup(".dialog-pane .button").queryAllAs(Button.class)) {
            if (ButtonBar.getButtonData(b) == funcion) {
                clickOn(b);
                return;
            }
        }
        Assertions.fail("No se encontró el botón " + funcion + " en el diálogo");
    }

    /**
     * Comprueba que el aviso abierto dice exactamente ese texto y lo cierra con Aceptar.
     *
     * @param texto texto que se escribe o que debe decir el aviso
     */
    private void comprobarAviso(String texto) {
        FxAssert.verifyThat(".dialog-pane .content", LabeledMatchers.hasText(texto));
        pulsarBoton(ButtonBar.ButtonData.OK_DONE);
    }

    /**
     * Comprueba que el aviso abierto empieza por ese texto y lo cierra con Aceptar.
     *
     * @param inicio comienzo esperado del texto, o hora de inicio de la franja
     */
    private void comprobarAvisoQueEmpiezaPor(String inicio) {
        DialogPane aviso = lookup(".dialog-pane").queryAs(DialogPane.class);
        Assertions.assertTrue(aviso.getContentText().startsWith(inicio), "Aviso: " + aviso.getContentText());
        pulsarBoton(ButtonBar.ButtonData.OK_DONE);
    }

    /** Cierra los diálogos que hayan quedado abiertos si una prueba falla a medias. */
    private void cerrarDialogos() {
        for (final Window w : new ArrayList<Window>(listWindows())) {
            if (w.isShowing() && w.getScene() != null && w.getScene().getRoot() instanceof DialogPane) {
                interact(new Runnable() {
                    @Override
                    public void run() {
                        w.hide();
                    }
                });
            }
        }
    }

    // ---------- Ayudas de datos ----------

    /**
     * Comprueba si MongoDB responde en el puerto por defecto.
     *
     * @return {@code true} si MongoDB responde
     */
    private static boolean mongoActivo() {
        try (Socket s = new Socket()) {
            s.connect(new InetSocketAddress("localhost", 27017), 500);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * La clase temporal tal como está ahora en MongoDB.
     *
     * @return la clase leída de MongoDB
     */
    private static Document claseEnMongo() {
        ObjectId id = clase.getObjectId("_id");
        for (Document c : DAO.listarClases()) {
            if (id.equals(c.getObjectId("_id"))) {
                return c;
            }
        }
        Assertions.fail("La clase temporal ya no está en MongoDB");
        return null;
    }

    /**
     * Franja del horario guardado con ese día y hora de inicio, o {@code null} si no está.
     *
     * @param dia día de la semana en minúsculas, como se guarda en el horario
     * @param inicio comienzo esperado del texto, o hora de inicio de la franja
     * @return la franja, o {@code null} si no está
     */
    private Document franjaEnMongo(String dia, String inicio) {
        List<Document> horas = claseEnMongo().getList("horas", Document.class);
        for (Document f : horas) {
            if (dia.equals(f.getString("dia")) && inicio.equals(f.getString("horainicio"))) {
                return f;
            }
        }
        return null;
    }

    /**
     * Número de sesiones de la clase temporal en MongoDB.
     *
     * @return número de sesiones
     */
    private int sesionesEnMongo() {
        return DAO.listarSesiones(clase.getObjectId("_id")).size();
    }

    /**
     * Sesiones futuras de la clase temporal que caen en lunes a las 9:00.
     *
     * @return las sesiones del lunes a las 9:00
     */
    private List<Document> sesionesDelLunes() {
        List<Document> lunes = new ArrayList<Document>();
        for (Document s : DAO.listarSesiones(clase.getObjectId("_id"))) {
            LocalDateTime f = s.getDate("fecha").toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            if (f.isAfter(LocalDateTime.now()) && f.getDayOfWeek() == DayOfWeek.MONDAY && f.getHour() == 9) {
                lunes.add(s);
            }
        }
        return lunes;
    }

    /**
     * Aforo guardado de la sesión del martes a las 10:00.
     *
     * @return plazas máximas guardadas
     */
    private int aforoDeLaSesionDelMartes() {
        for (Document s : DAO.listarSesiones(clase.getObjectId("_id"))) {
            LocalDateTime f = s.getDate("fecha").toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            if (f.getDayOfWeek() == DayOfWeek.TUESDAY && f.getHour() == 10) {
                return s.getInteger("plazaMax");
            }
        }
        Assertions.fail("No está la sesión del martes");
        return -1;
    }
}
