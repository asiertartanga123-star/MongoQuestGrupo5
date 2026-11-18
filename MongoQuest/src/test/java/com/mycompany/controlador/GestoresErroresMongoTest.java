package com.mycompany.controlador;

import com.mycompany.controlador.dao.ConexionException;
import com.mycompany.controlador.dao.DatosException;
import com.mycompany.controlador.dao.daoImplementacio;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxAssert;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;
import org.testfx.util.WaitForAsyncUtils;

/**
 * Pruebas TestFX de cómo tratan los errores de MongoDB el login, la gestión de monitores, la
 * gestión de clases y el horario de sesiones.
 * <p>
 * Cada test abre la pantalla con un acceso a datos falso (una subclase de {@link daoImplementacio}):
 * devuelve datos inventados y, en el método que indica el test, lanza la excepción del test.
 * Ningún test lee ni escribe en MongoDB.
 */
public class GestoresErroresMongoTest extends ApplicationTest {

    /** Nombre del método del acceso a datos que debe fallar; {@code null} = ninguno falla. */
    private String metodoQueFalla;
    /** Excepción que lanza ese método. */
    private RuntimeException fallo;
    /** Ventana que prepara TestFX; cada test carga en ella su pantalla. */
    private Stage ventana;
    /** Controlador de la última pantalla abierta. */
    private Object controlador;
    /** Sesión antes del test, para dejarla como estaba. */
    private String mailAntes;
    /** Rol antes del test, para dejarlo como estaba. */
    private String rolAntes;

    /** Crea la clase de pruebas; JUnit crea una instancia para cada test. */
    public GestoresErroresMongoTest() {
    }

    /**
     * Antes de cada test: guarda la ventana y la sesión; la pantalla la abre cada test.
     *
     * @param stage ventana que prepara TestFX
     */
    @Override
    public void start(Stage stage) {
        ventana = stage;
        mailAntes = LoginController.mailActual;
        rolAntes = LoginController.rolActual;
    }

    /**
     * Después de cada test: cierra los avisos abiertos y las ventanas, y restaura la sesión.
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
        LoginController.mailActual = mailAntes;
        LoginController.rolActual = rolAntes;
    }

    // ---------- Ayudas ----------

    /**
     * Lanza la excepción del test si el método indicado es el que debe fallar.
     *
     * @param metodo nombre del método del acceso a datos que se está ejecutando
     */
    private void fallarSi(String metodo) {
        if (metodo.equals(metodoQueFalla)) {
            throw fallo;
        }
    }

    /**
     * Crea una lista de documentos con un solo elemento.
     *
     * @param d el documento
     * @return lista con {@code d}
     */
    private static List<Document> uno(Document d) {
        List<Document> lista = new ArrayList<Document>();
        lista.add(d);
        return lista;
    }

    /**
     * Crea el acceso a datos falso de los tests.
     *
     * @return acceso a datos falso con un monitor, una sala y una clase inventados
     */
    private daoImplementacio falso() {
        return new daoImplementacio() {
            @Override
            public String validarLogin(String mail, String contraseña) {
                fallarSi("validarLogin");
                return null;
            }

            @Override
            public List<Document> listarMonitores() {
                fallarSi("listarMonitores");
                return uno(new Document("_id", new ObjectId()).append("nombre", "Mario").append("apellido", "Monitor")
                        .append("dni", "11111111H").append("mail", "mario@prueba.com").append("tipo", "monitor"));
            }

            @Override
            public boolean eliminarMonitor(ObjectId id) {
                fallarSi("eliminarMonitor");
                return true;
            }

            @Override
            public List<Document> listarSalas() {
                fallarSi("listarSalas");
                return uno(new Document("_id", new ObjectId()).append("nombre", "Sala 1").append("aforo", 20));
            }

            @Override
            public List<Document> listarClientes() {
                fallarSi("listarClientes");
                return new ArrayList<Document>();
            }

            @Override
            public List<Document> listarClases() {
                fallarSi("listarClases");
                return uno(new Document("_id", new ObjectId()).append("nombre", "Yoga")
                        .append("horas", new ArrayList<Document>()));
            }

            @Override
            public List<Document> listarSesiones(ObjectId claseId) {
                fallarSi("listarSesiones");
                return new ArrayList<Document>();
            }

            @Override
            public boolean eliminarClase(ObjectId id) {
                fallarSi("eliminarClase");
                return true;
            }
        };
    }

    /**
     * Abre una pantalla con el acceso a datos falso. La carga se lanza sin esperar a que termine,
     * porque si falla al abrir, la pantalla muestra un aviso modal mientras se carga; después se
     * espera a que JavaFX no tenga nada pendiente (o a que ese aviso esté a la vista).
     *
     * @param fxml nombre del fichero FXML dentro de {@code /com/mycompany/vista/}
     */
    private void abrir(final String fxml) {
        final daoImplementacio dao = falso();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    FXMLLoader cargador = new FXMLLoader(getClass().getResource("/com/mycompany/vista/" + fxml));
                    cargador.setControllerFactory(new Callback<Class<?>, Object>() {
                        @Override
                        public Object call(Class<?> tipo) {
                            if (tipo == LoginController.class) {
                                return new LoginController(dao);
                            } else if (tipo == GestionMonitorController.class) {
                                return new GestionMonitorController(dao);
                            } else if (tipo == GestionClasesController.class) {
                                return new GestionClasesController(dao);
                            }
                            return new GestionSesionesController(dao);
                        }
                    });
                    Parent root = cargador.load();
                    controlador = cargador.getController();
                    ventana.setScene(new Scene(root));
                    ventana.show();
                    // El robot escribe en la ventana que tiene el foco: en el primer test aún no la tiene.
                    ventana.toFront();
                    ventana.requestFocus();
                } catch (Exception e) {
                    throw new IllegalStateException("No se pudo abrir " + fxml, e);
                }
            }
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Comprueba que hay un aviso, devuelve su mensaje y lo cierra con su botón Aceptar.
     *
     * @return el mensaje del aviso
     */
    private String cerrarAviso() {
        esperarDialogo();
        FxAssert.verifyThat(".dialog-pane", NodeMatchers.isVisible());
        DialogPane dialogo = lookup(".dialog-pane").queryAs(DialogPane.class);
        String mensaje = dialogo.getContentText();
        clickOn(dialogo.lookupButton(ButtonType.OK));
        return mensaje;
    }

    /**
     * Espera hasta 5 segundos a que haya un diálogo a la vista: el aviso lo abre JavaFX un instante
     * después del clic, y sin esperar el test lo buscaría antes de que exista.
     */
    private void esperarDialogo() {
        try {
            WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, new Callable<Boolean>() {
                @Override
                public Boolean call() {
                    return lookup(".dialog-pane").tryQuery().isPresent();
                }
            });
        } catch (TimeoutException e) {
            Assertions.fail("No apareció ningún aviso en 5 segundos");
        }
    }

    /** Pulsa «Sí» en la pregunta de confirmación que está a la vista. */
    private void confirmar() {
        esperarDialogo();
        DialogPane pregunta = lookup(".dialog-pane").queryAs(DialogPane.class);
        clickOn(pregunta.lookupButton(ButtonType.YES));
    }

    /**
     * Selecciona con el ratón el primer elemento de una lista.
     *
     * @param idLista fx:id de la lista, con su {@code #}
     */
    private void seleccionarPrimero(String idLista) {
        ListView<?> lista = lookup(idLista).queryAs(ListView.class);
        Node celda = from(lista).lookup(".list-cell").query();
        clickOn(celda);
    }

    // ---------- Login ----------

    /** Comprueba que si MongoDB no responde al iniciar sesión, avisa y se queda en el login sin sesión. */
    @Test
    public void loginSinConexionAvisaYNoEntra() {
        metodoQueFalla = "validarLogin";
        fallo = new ConexionException("validarLogin", new RuntimeException("Timed out"));
        LoginController.mailActual = null;
        abrir("Login.fxml");

        clickOn("#txtUsuario").write("gerente@prueba.com");
        clickOn("#txtPassword").write("clave123");
        clickOn("#btnLogin");

        Assertions.assertEquals("No se puede conectar con MongoDB. Comprueba que esté iniciado.", cerrarAviso());
        FxAssert.verifyThat("#btnLogin", NodeMatchers.isVisible());
        Assertions.assertNull(LoginController.mailActual, "No debería quedar ninguna sesión abierta");
    }

    // ---------- Gestión de monitores ----------

    /** Comprueba que si MongoDB falla al abrir la gestión de monitores, avisa y la lista queda vacía. */
    @Test
    public void monitoresSinConexionAlAbrirAvisaYLaListaQuedaVacia() {
        metodoQueFalla = "listarMonitores";
        fallo = new ConexionException("listarMonitores", new RuntimeException("Timed out"));

        abrir("GestorMonitor.fxml");

        Assertions.assertEquals("No se puede conectar con MongoDB.", cerrarAviso());
        WaitForAsyncUtils.waitForFxEvents();
        Assertions.assertTrue(lookup("#listMonitores").queryAs(ListView.class).getItems().isEmpty());
    }

    /** Comprueba que si MongoDB falla al dar de baja a un monitor, avisa y el monitor sigue en la lista. */
    @Test
    public void bajaDeMonitorConErrorAvisaYElMonitorSigue() {
        metodoQueFalla = "eliminarMonitor";
        fallo = new DatosException("eliminarMonitor", "borrado fallido", new RuntimeException("write error"));
        abrir("GestorMonitor.fxml");

        seleccionarPrimero("#listMonitores");
        clickOn("#btnBaja");
        confirmar();

        Assertions.assertEquals("No se pudo dar de baja al monitor.", cerrarAviso());
        Assertions.assertEquals(1, lookup("#listMonitores").queryAs(ListView.class).getItems().size());
    }

    // ---------- Gestión de clases ----------

    /** Comprueba que si MongoDB falla al abrir la gestión de clases, avisa y no muestra clases. */
    @Test
    public void clasesSinConexionAlAbrirAvisaYNoMuestraClases() {
        metodoQueFalla = "listarClases";
        fallo = new ConexionException("listarClases", new RuntimeException("Timed out"));

        abrir("GestorClase.fxml");

        Assertions.assertEquals("No se puede conectar con MongoDB.", cerrarAviso());
        WaitForAsyncUtils.waitForFxEvents();
        Assertions.assertTrue(lookup("#listClases").queryAs(ListView.class).getItems().isEmpty());
    }

    /** Comprueba que si MongoDB falla al borrar una clase, avisa y la clase sigue en la lista. */
    @Test
    public void borrarClaseConErrorAvisaYLaClaseSigue() {
        metodoQueFalla = "eliminarClase";
        fallo = new DatosException("eliminarClase", "transacción abortada", new RuntimeException("write error"));
        abrir("GestorClase.fxml");

        seleccionarPrimero("#listClases");
        clickOn("#btnBorrarClase");
        confirmar();

        Assertions.assertEquals("No se pudo borrar la clase.", cerrarAviso());
        Assertions.assertEquals(1, lookup("#listClases").queryAs(ListView.class).getItems().size());
    }

    // ---------- Horario de sesiones ----------

    /** Comprueba que si MongoDB falla al crear una sesión en un hueco libre, avisa y el hueco sigue libre. */
    @Test
    public void sesionSinConexionAvisaYElHuecoSigueLibre() {
        metodoQueFalla = "listarSalas";
        fallo = new ConexionException("listarSalas", new RuntimeException("Timed out"));
        abrir("GestorSesiones.fxml");
        interact(new Runnable() {
            @Override
            public void run() {
                ((GestionSesionesController) controlador).setClase(new Document("_id", new ObjectId())
                        .append("nombre", "Yoga").append("salaid", new ObjectId())
                        .append("horas", new ArrayList<Document>()));
            }
        });

        StackPane libre = hueco("3,11");
        clickOn(libre);

        Assertions.assertEquals("No se pudo guardar el horario en MongoDB.", cerrarAviso());
        // Un hueco ocupado lleva dentro la etiqueta con el nombre de la clase; uno libre está vacío.
        Assertions.assertTrue(hueco("3,11").getChildren().isEmpty(), "El hueco debería seguir libre");
    }

    /**
     * Busca un hueco del horario por su posición.
     *
     * @param posicion columna (día) y fila (hora) separadas por coma, como {@code "3,11"}
     * @return el hueco
     */
    private StackPane hueco(String posicion) {
        GridPane grid = lookup("#gridHorario").query();
        for (Node n : grid.getChildren()) {
            if (posicion.equals(n.getUserData())) {
                return (StackPane) n;
            }
        }
        throw new IllegalStateException("No hay hueco " + posicion);
    }
}
