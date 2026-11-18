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
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isDisabled;
import static org.testfx.matcher.base.NodeMatchers.isVisible;
import static org.testfx.matcher.control.LabeledMatchers.hasText;
import static org.testfx.matcher.control.TableViewMatchers.hasNumRows;

/**
 * Pruebas TestFX de la pantalla del cliente contra la base de datos real.
 * <p>
 * Antes de empezar se crean un cliente de prueba y dos clases de prueba: una con una sesión
 * de 5 plazas y otra con una sesión completa (0 plazas). Al terminar se borra todo, también
 * las plazas que haya ocupado el cliente. Los tests van en orden porque el tercero apunta al
 * cliente a una clase y el cuarto comprueba que no se puede apuntar dos veces.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ClienteControllerTest extends ApplicationTest {

    /** Crea la clase de pruebas; JUnit crea una instancia para cada test. */
    public ClienteControllerTest() {
    }

    /** Acceso a MongoDB para preparar y comprobar los datos de prueba. */
    private static final daoImplementacio DAO = new daoImplementacio();
    /** Marca de tiempo que hace únicos los nombres de los datos de prueba. */
    private static final String SELLO = String.valueOf(System.currentTimeMillis());
    /** Correo del cliente de prueba, con el que se abre la pantalla. */
    private static final String MAIL = "prueba.cliente." + SELLO + "@test.com";
    /** Nombre de la clase de prueba con plazas libres. */
    private static final String CLASE_LIBRE = "PRUEBA libre " + SELLO;
    /** Nombre de la clase de prueba sin plazas. */
    private static final String CLASE_LLENA = "PRUEBA llena " + SELLO;

    /** Cliente de prueba creado al empezar. */
    private static Document cliente;
    /** Clase de prueba con una sesión de 5 plazas. */
    private static Document claseLibre;
    /** Clase de prueba con una sesión completa (0 plazas). */
    private static Document claseLlena;

    /** Ventana de la pantalla del cliente que abre cada test. */
    private Stage ventanaCliente;

    // ---------- Preparación y limpieza ----------

    /**
     * Crea el cliente y las dos clases de prueba. Las pruebas se omiten si MongoDB
     * no está arrancado o si faltan salas o monitores.
     */
    @BeforeAll
    public static void prepararDatos() {
        Assumptions.assumeTrue(mongoActivo(), "MongoDB no está arrancado en localhost:27017");
        Assumptions.assumeTrue(!DAO.listarSalas().isEmpty(), "No hay salas en la base de datos");
        Assumptions.assumeTrue(!DAO.listarMonitores().isEmpty(), "No hay monitores en la base de datos");

        List<Document> logros = new ArrayList<Document>();
        logros.add(new Document("nombre", "Constante").append("fecha", "2026-10-01").append("semanas", 4));
        cliente = new Document("dni", "T" + SELLO.substring(SELLO.length() - 8))
                .append("nombre", "Prueba")
                .append("apellido", "Cliente")
                .append("fechaAlta", new Date())
                .append("mail", MAIL)
                .append("contraseña", "prueba")
                .append("membresia", "premium")
                .append("logros", logros)
                .append("clases", new ArrayList<Document>());
        DAO.insertarCliente(cliente);

        claseLibre = crearClaseConSesion(CLASE_LIBRE, 5);
        claseLlena = crearClaseConSesion(CLASE_LLENA, 0);

        // La pantalla muestra 3 clases al abrir y 9 más con «Ver más clases».
        List<Document> todas = DAO.listarClases();
        Assumptions.assumeTrue(posicion(todas, CLASE_LLENA) < 12,
                "Hay más de 10 clases en la base de datos: las de prueba no caben en la pantalla");
    }

    /** Borra el cliente y las clases de prueba con sus sesiones. */
    @AfterAll
    public static void borrarDatos() {
        if (claseLibre != null) {
            DAO.eliminarClase(claseLibre.getObjectId("_id"));
        }
        if (claseLlena != null) {
            DAO.eliminarClase(claseLlena.getObjectId("_id"));
        }
        if (cliente != null) {
            DAO.eliminarCliente(cliente.getObjectId("_id"));
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        LoginController.mailActual = MAIL;
        Parent root = FXMLLoader.load(getClass().getResource("/com/mycompany/vista/Cliente.fxml"));
        stage.setScene(new Scene(root));
        stage.show();
        ventanaCliente = stage;
    }

    /**
     * Cierra los avisos que hayan quedado abiertos y todas las ventanas después de cada test.
     *
     * @throws Exception si TestFX no puede cerrar las ventanas
     */
    @AfterEach
    public void cerrarVentanas() throws Exception {
        cerrarAvisos();
        FxToolkit.cleanupStages();
    }

    // ---------- Tests ----------

    /** Comprueba que al abrir muestra los datos y los logros del cliente. */
    @Test
    @Order(1)
    public void alAbrir_muestraLosDatosYLosLogrosDelCliente() {
        verifyThat("#txtNombre", org.testfx.matcher.control.TextInputControlMatchers.hasText("Prueba Cliente"));
        verifyThat("#txtDni", org.testfx.matcher.control.TextInputControlMatchers.hasText(cliente.getString("dni")));
        verifyThat("#txtMail", org.testfx.matcher.control.TextInputControlMatchers.hasText(MAIL));
        verifyThat("#txtMembresia", org.testfx.matcher.control.TextInputControlMatchers.hasText("premium"));
        verifyThat("#tablaLogros", hasNumRows(1));
        verifyThat("#lblClase1", hasText(""));
    }

    /** Comprueba que ver más clases muestra el resto y desactiva el botón. */
    @Test
    @Order(2)
    public void verMasClases_muestraElRestoYDesactivaElBoton() {
        clickOn("#btnVerMasClases");

        verifyThat("#tarjeta10", isVisible());
        verifyThat("#btnVerMasClases", isDisabled());
        Assertions.assertNotNull(tarjetaDe(CLASE_LLENA), "La clase de prueba debería verse tras «Ver más clases»");
    }

    /** Comprueba que apuntarse a una clase con plaza la añade y ocupa una plaza. */
    @Test
    @Order(3)
    public void apuntarseAUnaClaseConPlaza_laAnadeYOcupaUnaPlaza() {
        abrirTarjeta(CLASE_LIBRE);
        pulsarEnAviso(ButtonType.YES);

        verifyThat("#lblClase1", hasText(CLASE_LIBRE));
        verifyThat("#lblPlazas1", hasText("1/5"));
        Assertions.assertTrue(tieneLaClase(claseLibre.getObjectId("_id")), "La clase debería estar guardada en el cliente");
    }

    /** Comprueba que apuntarse otra vez a la misma avisa que ya está apuntado. */
    @Test
    @Order(4)
    public void apuntarseOtraVezALaMisma_avisaQueYaEstaApuntado() {
        abrirTarjeta(CLASE_LIBRE);

        Assertions.assertTrue(textoDelAviso().contains("Ya estás apuntado"), "Debería avisar de que ya está apuntado");
    }

    /** Comprueba que apuntarse a una clase llena avisa y no la añade. */
    @Test
    @Order(5)
    public void apuntarseAUnaClaseLlena_avisaYNoLaAnade() {
        abrirTarjeta(CLASE_LLENA);
        pulsarEnAviso(ButtonType.YES);

        Assertions.assertTrue(textoDelAviso().contains("completa"), "Debería avisar de que la clase está completa");
        Assertions.assertFalse(tieneLaClase(claseLlena.getObjectId("_id")), "La clase llena no debería guardarse");
    }

    /** Comprueba que apuntarse y cancelar no cambia nada. */
    @Test
    @Order(6)
    public void apuntarseYCancelar_noCambiaNada() {
        abrirTarjeta(CLASE_LLENA);
        pulsarEnAviso(ButtonType.NO);

        verifyThat("#lblClase2", hasText(""));
        Assertions.assertFalse(tieneLaClase(claseLlena.getObjectId("_id")));
    }

    /** Comprueba que cerrar sesión abre el login y cierra la ventana del cliente. */
    @Test
    @Order(7)
    public void cerrarSesion_abreElLoginYCierraLaVentanaDelCliente() {
        clickOn("#btnCerrarSesion");

        verifyThat("#txtUsuario", isVisible());
        verifyThat("#txtPassword", isVisible());
        Assertions.assertFalse(ventanaCliente.isShowing());
    }

    // ---------- Ayudas ----------

    /**
     * Crea una clase de prueba con una sesión para mañana con las plazas indicadas.
     *
     * @param nombre nombre de la clase
     * @param plazas plazas máximas de la sesión
     * @return la clase creada, con su _id
     */
    private static Document crearClaseConSesion(String nombre, int plazas) {
        // El esquema de Clases (01_crear_bd_validaciones.js) exige al menos una franja en "horas".
        List<Document> horas = new ArrayList<Document>();
        horas.add(new Document("dia", "lunes").append("horainicio", "09:00").append("horafin", "10:00"));
        Document clase = new Document("nombre", nombre)
                .append("horas", horas)
                .append("salaid", DAO.listarSalas().get(0).getObjectId("_id"));
        DAO.insertarClase(clase);
        DAO.insertarSesion(new Document("claseid", clase.getObjectId("_id"))
                .append("fecha", new Date(System.currentTimeMillis() + 86400000L))
                .append("plazaMax", plazas)
                .append("monitor", DAO.listarMonitores().get(0).getObjectId("_id"))
                .append("asistentes", new ArrayList<Document>()));
        return clase;
    }

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
     * Posición de una clase en la lista de todas las clases, o un número grande si no está.
     *
     * @param clases clases en las que buscar
     * @param nombre nombre de la clase
     * @return posición de la clase, o {@code Integer.MAX_VALUE} si no está
     */
    private static int posicion(List<Document> clases, String nombre) {
        for (int i = 0; i < clases.size(); i++) {
            if (nombre.equals(clases.get(i).getString("nombre"))) {
                return i;
            }
        }
        return Integer.MAX_VALUE;
    }

    /**
     * Busca la etiqueta de una clase disponible (de la 7 a la 18) con ese nombre.
     *
     * @param nombre nombre de la clase
     * @return la etiqueta, o {@code null} si no se ve
     */
    private Label tarjetaDe(String nombre) {
        for (int i = 7; i <= 18; i++) {
            Label etiqueta = lookup("#lblClase" + i).queryAs(Label.class);
            if (nombre.equals(etiqueta.getText()) && etiqueta.getParent().isVisible()) {
                return etiqueta;
            }
        }
        return null;
    }

    /**
     * Pulsa la tarjeta de la clase; si no está entre las tres primeras, pulsa antes «Ver más clases».
     *
     * @param nombre nombre de la clase
     */
    private void abrirTarjeta(String nombre) {
        Label etiqueta = tarjetaDe(nombre);
        if (etiqueta == null) {
            clickOn("#btnVerMasClases");
            etiqueta = tarjetaDe(nombre);
        }
        Assertions.assertNotNull(etiqueta, "No se encuentra la tarjeta de " + nombre);
        clickOn(etiqueta.getParent());
    }

    /**
     * Devuelve el aviso (Alert) que está abierto.
     *
     * @return el contenido del aviso abierto
     */
    private DialogPane avisoAbierto() {
        for (Window w : listWindows()) {
            if (w.isShowing() && w.getScene() != null && w.getScene().getRoot() instanceof DialogPane) {
                return (DialogPane) w.getScene().getRoot();
            }
        }
        Assertions.fail("No hay ningún aviso abierto");
        return null;
    }

    /**
     * Texto del aviso abierto.
     *
     * @return el texto del aviso
     */
    private String textoDelAviso() {
        return avisoAbierto().getContentText();
    }

    /**
     * Pulsa un botón del aviso abierto (Sí, No…).
     *
     * @param boton botón que se pulsa
     */
    private void pulsarEnAviso(ButtonType boton) {
        Node nodo = avisoAbierto().lookupButton(boton);
        Assertions.assertNotNull(nodo, "El aviso no tiene el botón " + boton.getText());
        clickOn(nodo);
    }

    /** Cierra los avisos que hayan quedado abiertos (cleanupStages no los cierra). */
    private void cerrarAvisos() {
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

    /**
     * Comprueba en MongoDB si el cliente de prueba tiene la clase en su lista.
     *
     * @param idClase identificador de la clase
     * @return {@code true} si el cliente tiene la clase
     */
    private boolean tieneLaClase(ObjectId idClase) {
        List<Document> clases = DAO.buscarClientePorMail(MAIL).getList("clases", Document.class);
        if (clases == null) {
            return false;
        }
        for (Document c : clases) {
            if (idClase.equals(c.getObjectId("id_clase"))) {
                return true;
            }
        }
        return false;
    }
}
