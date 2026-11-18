package com.mycompany.controlador;

import com.mycompany.controlador.dao.ConexionException;
import com.mycompany.controlador.dao.DatosException;
import com.mycompany.controlador.dao.DuplicadoException;
import com.mycompany.controlador.dao.daoImplementacio;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
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
 * Pruebas TestFX de cómo trata la gestión de clientes los errores de MongoDB al guardar.
 * <p>
 * La pantalla se carga con un acceso a datos falso que devuelve un único cliente inventado y
 * cuya {@code actualizarCliente} lanza la excepción que pide cada test. Se comprueba el aviso
 * y que la lista sigue mostrando los datos antiguos. Ningún test escribe en MongoDB.
 */
public class GestionClientesErroresMongoTest extends ApplicationTest {

    /** Error que lanzará {@code actualizarCliente} en el test actual. */
    private RuntimeException fallo;
    /** Cliente inventado que muestra la lista. */
    private Document cliente;

    /** Crea la clase de pruebas; JUnit crea una instancia para cada test. */
    public GestionClientesErroresMongoTest() {
    }

    /**
     * Antes de cada test: abre la gestión de clientes con el acceso a datos falso.
     *
     * @param stage ventana que prepara TestFX
     * @throws Exception si no se puede cargar el FXML
     */
    @Override
    public void start(Stage stage) throws Exception {
        cliente = new Document("_id", new ObjectId())
                .append("nombre", "Ana")
                .append("apellido", "Prueba")
                .append("dni", "87654321X")
                .append("mail", "ana.prueba@prueba.com")
                .append("membresia", "normal")
                .append("fechaAlta", new Date());
        final daoImplementacio falso = new daoImplementacio() {
            @Override
            public List<Document> listarClientes() {
                List<Document> lista = new ArrayList<Document>();
                lista.add(cliente);
                return lista;
            }

            @Override
            public void actualizarCliente(ObjectId id, String nombre, String apellido,
                    String dni, String mail, String membresia) {
                throw fallo;
            }
        };
        FXMLLoader cargador = new FXMLLoader(getClass().getResource("/com/mycompany/vista/GestorCliente.fxml"));
        cargador.setControllerFactory(new Callback<Class<?>, Object>() {
            @Override
            public Object call(Class<?> tipo) {
                return new GestionClientesController(falso);
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

    /** Pulsa con el ratón el único cliente de la lista, cambia su nombre con el teclado y pulsa «Guardar». */
    private void cambiarNombreYGuardar() {
        ListView<?> lista = lookup("#listClientes").query();
        for (ListCell<?> celda : lista.lookupAll(".list-cell").toArray(new ListCell<?>[0])) {
            if (celda.getIndex() == 0 && celda.isVisible()) {
                clickOn(celda);
                break;
            }
        }
        TextField nombre = lookup("#txtNombre").query();
        Assertions.assertEquals("Ana", nombre.getText(), "La ficha debería mostrar el cliente elegido");
        clickOn(nombre);
        push(KeyCode.CONTROL, KeyCode.A);
        type(KeyCode.BACK_SPACE);
        write("Nombre nuevo");
        clickOn("#btnGuardar");
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

    /** Comprueba que un DNI o mail duplicado (DuplicadoException) avisa y no cambia el cliente de la lista. */
    @Test
    public void unDuplicadoAlGuardarAvisaYNoCambiaLaLista() {
        fallo = new DuplicadoException("actualizarCliente", new RuntimeException("E11000 duplicate key"));

        cambiarNombreYGuardar();

        Assertions.assertEquals("Ya existe otro cliente con ese DNI o ese mail.", cerrarAviso());
        Assertions.assertEquals("Ana", cliente.getString("nombre"), "El cliente de la lista no debería cambiar");
    }

    /** Comprueba que sin conexión con MongoDB (ConexionException) avisa y no cambia el cliente de la lista. */
    @Test
    public void sinConexionAlGuardarAvisaYNoCambiaLaLista() {
        fallo = new ConexionException("actualizarCliente", new RuntimeException("Timed out"));

        cambiarNombreYGuardar();

        Assertions.assertEquals("No se puede conectar con MongoDB.", cerrarAviso());
        Assertions.assertEquals("Ana", cliente.getString("nombre"));
    }

    /** Comprueba que cualquier otro error de MongoDB (DatosException) avisa de que no se guardó. */
    @Test
    public void otroErrorAlGuardarAvisaQueNoSeGuardo() {
        fallo = new DatosException("actualizarCliente", "fallo de escritura", new RuntimeException("write error"));

        cambiarNombreYGuardar();

        Assertions.assertEquals("No se pudieron guardar los datos.", cerrarAviso());
        Assertions.assertEquals("Ana", cliente.getString("nombre"));
    }
}
