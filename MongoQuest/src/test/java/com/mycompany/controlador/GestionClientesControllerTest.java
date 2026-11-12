package com.mycompany.controlador;

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

public class GestionClientesControllerTest extends ApplicationTest {

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

    // Se ejecuta antes de cada test: abre la pantalla de gestión de clientes.
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/com/mycompany/vista/GestorCliente.fxml"));
        stage.setScene(new Scene(root));
        stage.show();
    }

    // ---------- Ayudas ----------

    // Selecciona el primer cliente de la lista (se omite el test si no hay clientes).
    private void seleccionarPrimerCliente() {
        final ListView<Document> lista = lookup("#listClientes").query();
        Assumptions.assumeTrue(!lista.getItems().isEmpty(), "No hay clientes en la base de datos");
        interact(new Runnable() {
            @Override
            public void run() {
                lista.getSelectionModel().selectFirst();
            }
        });
    }

    // Escribe un texto en un campo, sustituyendo el que hubiera.
    private void escribir(String selector, final String texto) {
        final TextField campo = lookup(selector).query();
        interact(new Runnable() {
            @Override
            public void run() {
                campo.setText(texto);
            }
        });
    }

    // ---------- Tests ----------

    @Test
    public void alAbrirLaPantallaLosBotonesDeEdicionEstanDesactivados() {
        FxAssert.verifyThat("#btnGuardar", NodeMatchers.isDisabled());
        FxAssert.verifyThat("#btnBaja", NodeMatchers.isDisabled());
        FxAssert.verifyThat("#btnAlta", NodeMatchers.isEnabled());
    }

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

    @Test
    public void guardarConElNombreVacioMuestraUnAviso() {
        seleccionarPrimerCliente();
        escribir("#txtNombre", "");

        clickOn("#btnGuardar");

        // Sale el aviso "Rellena todos los campos." y no se toca la base de datos.
        FxAssert.verifyThat(".dialog-pane", NodeMatchers.isVisible());
        type(KeyCode.ENTER);
    }

    @Test
    public void guardarConUnMailNoValidoMuestraUnAviso() {
        seleccionarPrimerCliente();
        escribir("#txtMail", "esto-no-es-un-mail");

        clickOn("#btnGuardar");

        FxAssert.verifyThat(".dialog-pane", NodeMatchers.isVisible());
        type(KeyCode.ENTER);
    }

    @Test
    public void cancelarElAltaNoAnadeNingunCliente() {
        final ListView<Document> lista = lookup("#listClientes").query();
        int antes = lista.getItems().size();

        clickOn("#btnAlta");
        FxAssert.verifyThat(".dialog-pane", NodeMatchers.isVisible());

        type(KeyCode.ESCAPE);   // cierra el formulario sin guardar

        Assertions.assertEquals(antes, lista.getItems().size(), "No debería haberse añadido ningún cliente");
    }

     @Test
    public void pulsarElTituloVuelveAlPanelDelGerente() {
        clickOn("#lblTitulo");
        FxAssert.verifyThat("#cardMonitores", NodeMatchers.isVisible());
    }
}