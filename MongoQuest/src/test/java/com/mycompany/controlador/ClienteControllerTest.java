package com.mycompany.controlador;

import com.mycompany.controlador.dao.daoImplementacio;
import java.util.Arrays;
import java.util.Collections;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isDisabled;
import static org.testfx.matcher.base.NodeMatchers.isEnabled;
import static org.testfx.matcher.base.NodeMatchers.isVisible;
import static org.testfx.matcher.control.LabeledMatchers.hasText;
import static org.testfx.matcher.control.TableViewMatchers.hasNumRows;

public class ClienteControllerTest extends ApplicationTest {

    private daoImplementacio dao;
    private Stage ventanaCliente;   // para comprobar si se cierra al cerrar sesión

    @Override
    public void start(Stage stage) throws Exception {
        // 1. Preparamos el DAO falso y los datos del cliente antes de abrir la ventana
        dao = mock(daoImplementacio.class);
        LoginController.mailActual = "ana@x.com";

        Document claseApuntada = new Document("nombre", "Yoga")
                .append("id_clase", new ObjectId());

        Document logro = new Document("nombre", "Primera clase")
                .append("fecha", "2026-10-01")
                .append("semanas", 1);

        Document cliente = new Document("nombre", "Ana")
                .append("apellido", "Lopez")
                .append("dni", "12345678A")
                .append("mail", "ana@x.com")
                .append("membresia", "premium")
                .append("clases", Collections.singletonList(claseApuntada))
                .append("logros", Collections.singletonList(logro));

        when(dao.buscarClientePorMail("ana@x.com")).thenReturn(cliente);

        // 2. Preparamos 4 clases en el centro (3 visibles + 1 oculta para probar "Ver más clases")
        ObjectId idYoga = new ObjectId();
        ObjectId idSpinning = new ObjectId();
        ObjectId idBoxeo = new ObjectId();
        ObjectId idPilates = new ObjectId();

        when(dao.listarClases()).thenReturn(Arrays.asList(
                new Document("_id", idYoga).append("nombre", "Yoga"),
                new Document("_id", idSpinning).append("nombre", "Spinning"),
                new Document("_id", idBoxeo).append("nombre", "Boxeo"),
                new Document("_id", idPilates).append("nombre", "Pilates")
        ));

        Document sesionConPlazas = new Document("plazaMax", 10)
                .append("asistentes", Arrays.asList(new Document(), new Document()));
        when(dao.ultimaSesionDeClase(any())).thenReturn(sesionConPlazas);

        // 3. Cargamos Cliente.fxml inyectando el controlador con el DAO falso
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/vista/Cliente.fxml"));
        loader.setControllerFactory(new Callback<Class<?>, Object>() {
            @Override
            public Object call(Class<?> param) {
                return new ClienteController(dao);
            }
        });

        Parent root = loader.load();
        stage.setScene(new Scene(root));
        stage.show();
        ventanaCliente = stage;
    }

    // Cierra las ventanas que haya dejado abierta cada prueba (por ejemplo, la de Login)
    @AfterEach
    public void cerrarVentanas() throws Exception {
        interact(new Runnable() {
            @Override
            public void run() {
                for (Window w : listWindows()) {
                    w.hide();
                }
            }
        });
    }

    @Test
    public void alAbrirVentana_cargaDatosClienteClasesYLogros() {
        // Comprueba los TextField de la derecha
        verifyThat("#txtNombre", org.testfx.matcher.control.TextInputControlMatchers.hasText("Ana Lopez"));
        verifyThat("#txtDni", org.testfx.matcher.control.TextInputControlMatchers.hasText("12345678A"));
        verifyThat("#txtMail", org.testfx.matcher.control.TextInputControlMatchers.hasText("ana@x.com"));
        verifyThat("#txtMembresia", org.testfx.matcher.control.TextInputControlMatchers.hasText("premium"));

        // Comprueba columna izquierda (clases apuntadas) y tabla de logros
        verifyThat("#lblClase1", hasText("Yoga"));
        verifyThat("#tablaLogros", hasNumRows(1));

        // Comprueba las 3 primeras clases del centro
        verifyThat("#lblClase7", hasText("Yoga"));
        verifyThat("#lblPlazas7", hasText("2/10"));
        verifyThat("#btnVerMasClases", isEnabled());
    }

    @Test
    public void pulsarVerMasClases_muestraTarjeta10YDesactivaBoton() {
        clickOn("#btnVerMasClases");

        verifyThat("#tarjeta10", isVisible());
        verifyThat("#lblClase10", hasText("Pilates"));
        verifyThat("#btnVerMasClases", isDisabled());
    }

    // ---------- Botón "cerrar sesion" ----------

    @Test
    public void cerrarSesion_abreLaVentanaDeLogin() {
        verifyThat("#btnCerrarSesion", isEnabled());

        clickOn("#btnCerrarSesion");

        // Aparece la pantalla de Login (campos y botones que solo tiene Login.fxml)
        verifyThat("#txtUsuario", isVisible());
        verifyThat("#txtPassword", isVisible());
        verifyThat("#btnLogin", hasText("Iniciar sesión"));
        verifyThat("#linkRegistro", isVisible());
    }

    @Test
    public void cerrarSesion_cierraLaVentanaDelCliente() {
        assertTrue(ventanaCliente.isShowing());

        clickOn("#btnCerrarSesion");

        assertFalse(ventanaCliente.isShowing());
    }
}