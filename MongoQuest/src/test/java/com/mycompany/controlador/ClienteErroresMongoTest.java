
package com.mycompany.controlador;

import com.mycompany.controlador.dao.ConexionException;
import com.mycompany.controlador.dao.DatosException;
import com.mycompany.controlador.dao.daoImplementacio;
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

/**
 * Pruebas TestFX de cómo trata la pantalla del cliente los errores de MongoDB: al abrirla y al
 * apuntarse a una clase.
 * <p>
 * Cada test abre la pantalla con un acceso a datos falso (una subclase de {@link daoImplementacio})
 * que devuelve un cliente inventado y tres clases, y que lanza la excepción que pide el test.
 * Ningún test lee ni escribe en MongoDB.
 */
public class ClienteErroresMongoTest extends ApplicationTest {

    /** Error que lanzará {@code listarClases} al abrir la pantalla; {@code null} = sin error. */
    private RuntimeException falloAlAbrir;
    /** Error que lanzará {@code apuntarseAClase}; {@code null} = se apunta bien. */
    private RuntimeException falloAlApuntar;
    /** Ventana que prepara TestFX; cada test carga en ella la pantalla. */
    private Stage ventana;
    /** Correo de la sesión antes del test, para dejarlo como estaba. */
    private String mailAntes;

    /** Crea la clase de pruebas; JUnit crea una instancia para cada test. */
    public ClienteErroresMongoTest() {
    }

    /**
     * Antes de cada test: guarda la ventana; la pantalla la abre cada test con {@link #abrir()},
     * después de decidir qué error lanza el acceso a datos falso.
     *
     * @param stage ventana que prepara TestFX
     */
    @Override
    public void start(Stage stage) {
        ventana = stage;
        mailAntes = LoginController.mailActual;
        LoginController.mailActual = "cliente.prueba@prueba.com";
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
    }

    // ---------- Ayudas ----------

    /** Abre la pantalla del cliente con el acceso a datos falso. */
    private void abrir() {
        final daoImplementacio falso = new daoImplementacio() {
            @Override
            public Document buscarClientePorMail(String mail) {
                return new Document("_id", new ObjectId())
                        .append("nombre", "Ana").append("apellido", "Prueba")
                        .append("dni", "87654321X").append("mail", mail)
                        .append("membresia", "normal").append("fechaAlta", new Date())
                        .append("clases", new ArrayList<Document>())
                        .append("logros", new ArrayList<Document>());
            }

            @Override
            public void revisarLogros(String mail) {
                // nada: el cliente inventado no gana logros
            }

            @Override
            public List<Document> listarClases() {
                if (falloAlAbrir != null) {
                    throw falloAlAbrir;
                }
                List<Document> clases = new ArrayList<Document>();
                clases.add(new Document("_id", new ObjectId()).append("nombre", "Yoga"));
                clases.add(new Document("_id", new ObjectId()).append("nombre", "Spinning"));
                clases.add(new Document("_id", new ObjectId()).append("nombre", "Pilates"));
                return clases;
            }

            @Override
            public Document ultimaSesionDeClase(ObjectId claseId) {
                return null;
            }

            @Override
            public boolean apuntarseAClase(String mail, ObjectId claseId, String nombreClase) {
                if (falloAlApuntar != null) {
                    throw falloAlApuntar;
                }
                return true;
            }
        };
        interact(new Runnable() {
            @Override
            public void run() {
                try {
                    FXMLLoader cargador = new FXMLLoader(getClass().getResource("/com/mycompany/vista/Cliente.fxml"));
                    cargador.setControllerFactory(new Callback<Class<?>, Object>() {
                        @Override
                        public Object call(Class<?> tipo) {
                            return new ClienteController(falso);
                        }
                    });
                    Parent root = cargador.load();
                    ventana.setScene(new Scene(root));
                    ventana.show();
                } catch (Exception e) {
                    throw new IllegalStateException("No se pudo abrir Cliente.fxml", e);
                }
            }
        });
    }

    /** Pulsa con el ratón la tarjeta de «Yoga» (la primera clase del centro) y responde «Sí». */
    private void apuntarseAYoga() {
        Node tarjeta = lookup("#lblClase7").queryAs(Label.class).getParent();
        clickOn(tarjeta);
        DialogPane pregunta = lookup(".dialog-pane").queryAs(DialogPane.class);
        clickOn(pregunta.lookupButton(ButtonType.YES));
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

    /** Comprueba que sin conexión al abrir la pantalla (ConexionException) avisa y la pantalla sigue abierta. */
    @Test
    public void sinConexionAlAbrirAvisaYLaPantallaSeAbre() {
        falloAlAbrir = new ConexionException("listarClases", new RuntimeException("Timed out"));

        abrir();

        Assertions.assertEquals("No se puede conectar con MongoDB. Comprueba que esté iniciado.", cerrarAviso());
        FxAssert.verifyThat("#tablaLogros", NodeMatchers.isVisible());
    }

    /** Comprueba que otro error de MongoDB al abrir (DatosException) avisa de que no se cargaron los datos. */
    @Test
    public void otroErrorAlAbrirAvisaQueNoSeCargaronLosDatos() {
        falloAlAbrir = new DatosException("listarClases", "lectura fallida", new RuntimeException("read error"));

        abrir();

        Assertions.assertEquals("No se pudieron cargar tus datos.", cerrarAviso());
    }

    /** Comprueba que sin conexión al apuntarse (ConexionException) avisa y la clase no pasa a «mis clases». */
    @Test
    public void sinConexionAlApuntarseAvisaYNoApunta() {
        falloAlApuntar = new ConexionException("apuntarseAClase", new RuntimeException("Timed out"));
        abrir();

        apuntarseAYoga();

        Assertions.assertEquals("No se puede conectar con MongoDB. No te has apuntado a Yoga.", cerrarAviso());
        Label primera = lookup("#lblClase1").queryAs(Label.class);
        Assertions.assertEquals("", primera.getText(), "Yoga no debería aparecer en mis clases");
    }

    /** Comprueba que otro error al apuntarse (DatosException) avisa y la clase no pasa a «mis clases». */
    @Test
    public void otroErrorAlApuntarseAvisaYNoApunta() {
        falloAlApuntar = new DatosException("apuntarseAClase", "escritura fallida", new RuntimeException("write error"));
        abrir();

        apuntarseAYoga();

        Assertions.assertEquals("No se pudo apuntar a Yoga por un error de la base de datos.", cerrarAviso());
        Assertions.assertEquals("", lookup("#lblClase1").queryAs(Label.class).getText());
    }

    /** Comprueba que si MongoDB guarda bien, la clase aparece en «mis clases» sin ningún aviso de error. */
    @Test
    public void sinErroresLaClasePasaAMisClases() {
        abrir();

        apuntarseAYoga();

        Assertions.assertEquals("Yoga", lookup("#lblClase1").queryAs(Label.class).getText());
    }
}
