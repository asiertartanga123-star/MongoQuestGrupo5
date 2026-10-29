package com.mycompany.controlador;

import com.mongodb.ErrorCategory;
import com.mongodb.MongoException;
import com.mongodb.MongoWriteException;
import com.mycompany.controlador.dao.daoImplementacio;
import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.bson.Document;

public class GestionClientesController {

    @FXML
    private BorderPane raiz;
    @FXML
    private ListView<Document> listClientes;
    @FXML
    private Label lblNombreCompleto;
    @FXML
    private ImageView imgFoto;
    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtApellidos;
    @FXML
    private TextField txtDni;
    @FXML
    private TextField txtMail;
    @FXML
    private TextField txtAlta;
    @FXML
    private ChoiceBox<String> choMembresia;
    @FXML
    private Button btnGuardar;
    @FXML
    private Button btnBaja;

    private final daoImplementacio dao = new daoImplementacio();
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    @FXML
    private Label lblTitulo;
    @FXML
    private Button btnCerrarSesion;
    @FXML
    private Button btnAlta;

    // Cada fila de la lista: nombre y apellido + dni y membresía
    private static class CeldaCliente extends ListCell<Document> {

        @Override
        protected void updateItem(Document c, boolean vacio) {
            super.updateItem(c, vacio);
            if (vacio || c == null) {
                setText(null);
            } else {
                setText(c.getString("nombre") + " " + c.getString("apellido")
                        + "\n" + c.getString("dni") + "  ·  " + c.getString("membresia"));
            }
        }
    }

    private void initialize() {
        choMembresia.getItems().addAll("normal", "premium");

        listClientes.setCellFactory(new Callback<ListView<Document>, ListCell<Document>>() {
            @Override
            public ListCell<Document> call(ListView<Document> lista) {
                return new CeldaCliente();
            }
        });

        // Al elegir un cliente se rellena la ficha
        listClientes.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Document>() {
            @Override
            public void changed(ObservableValue<? extends Document> obs, Document anterior, Document elegido) {
                mostrarCliente(elegido);
            }
        });

        cargarClientes();
    }

    private void cargarClientes() {
        try {
            List<Document> clientes = dao.listarClientes();
            System.out.println("Clientes cargados: " + clientes.size());
            listClientes.getItems().setAll(clientes);
        } catch (MongoException e) {
            e.printStackTrace();
            alerta(Alert.AlertType.ERROR, "No se puede conectar con MongoDB.");
        }
    }

    private void mostrarCliente(Document c) {
        if (c == null) {
            lblNombreCompleto.setText("Nombre y apellidos cliente");
            txtNombre.clear();
            txtApellidos.clear();
            txtDni.clear();
            txtMail.clear();
            txtAlta.clear();
            choMembresia.setValue(null);
            btnGuardar.setDisable(true);
            btnBaja.setDisable(true);
            return;
        }
        lblNombreCompleto.setText(c.getString("nombre") + " " + c.getString("apellido"));
        txtNombre.setText(c.getString("nombre"));
        txtApellidos.setText(c.getString("apellido"));
        txtDni.setText(c.getString("dni"));
        txtMail.setText(c.getString("mail"));
        choMembresia.setValue(c.getString("membresia"));

        Date alta = c.getDate("fechaAlta");
        if (alta == null) {
            txtAlta.setText("");
        } else {
            txtAlta.setText(alta.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().format(FECHA));
        }

        btnGuardar.setDisable(false);
        btnBaja.setDisable(false);
    }

    @FXML
    private void onGuardarDatos() {
        Document c = listClientes.getSelectionModel().getSelectedItem();
        if (c == null) {
            return;
        }

        String nombre = txtNombre.getText().trim();
        String apellido = txtApellidos.getText().trim();
        String dni = txtDni.getText().trim();
        String mail = txtMail.getText().trim();
        String membresia = choMembresia.getValue();

        if (nombre.isEmpty() || apellido.isEmpty() || dni.isEmpty() || mail.isEmpty() || membresia == null) {
            alerta(Alert.AlertType.WARNING, "Rellena todos los campos.");
            return;
        }
        if (!mail.matches("^.+@.+\\..+$")) {
            alerta(Alert.AlertType.WARNING, "El mail no es válido.");
            return;
        }

        try {
            dao.actualizarCliente(c.getObjectId("_id"), nombre, apellido, dni, mail, membresia);

            // Actualiza el cliente en memoria y refresca la lista y el título
            c.put("nombre", nombre);
            c.put("apellido", apellido);
            c.put("dni", dni);
            c.put("mail", mail);
            c.put("membresia", membresia);
            listClientes.refresh();
            lblNombreCompleto.setText(nombre + " " + apellido);

            alerta(Alert.AlertType.INFORMATION, "Datos guardados.");
        } catch (MongoWriteException e) {
            if (e.getError().getCategory() == ErrorCategory.DUPLICATE_KEY) {
                alerta(Alert.AlertType.ERROR, "Ya existe otro cliente con ese DNI o ese mail.");
            } else {
                alerta(Alert.AlertType.ERROR, "No se pudieron guardar los datos.");
            }
        } catch (MongoException e) {
            alerta(Alert.AlertType.ERROR, "No se puede conectar con MongoDB.");
        }
    }

    @FXML
    private void onDarDeAlta() {
        TextField fDni = new TextField();
        TextField fNombre = new TextField();
        TextField fApellido = new TextField();
        TextField fMail = new TextField();
        PasswordField fPass = new PasswordField();
        ChoiceBox<String> fMembresia = new ChoiceBox<String>();
        fMembresia.getItems().addAll("normal", "premium");
        fMembresia.setValue("normal");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.add(new Label("Dni:"), 0, 0);
        grid.add(fDni, 1, 0);
        grid.add(new Label("Nombre:"), 0, 1);
        grid.add(fNombre, 1, 1);
        grid.add(new Label("Apellidos:"), 0, 2);
        grid.add(fApellido, 1, 2);
        grid.add(new Label("Mail:"), 0, 3);
        grid.add(fMail, 1, 3);
        grid.add(new Label("Contraseña:"), 0, 4);
        grid.add(fPass, 1, 4);
        grid.add(new Label("Membresía:"), 0, 5);
        grid.add(fMembresia, 1, 5);

        Dialog<ButtonType> dialogo = new Dialog<ButtonType>();
        dialogo.setTitle("Dar de alta cliente");
        dialogo.getDialogPane().setContent(grid);
        dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ButtonType respuesta = dialogo.showAndWait().orElse(ButtonType.CANCEL);
        if (respuesta != ButtonType.OK) {
            return;
        }

        String dni = fDni.getText().trim();
        String nombre = fNombre.getText().trim();
        String apellido = fApellido.getText().trim();
        String mail = fMail.getText().trim();
        String pass = fPass.getText().trim();

        if (dni.isEmpty() || nombre.isEmpty() || apellido.isEmpty() || mail.isEmpty() || pass.isEmpty()) {
            alerta(Alert.AlertType.WARNING, "Rellena todos los campos.");
            return;
        }
        if (!mail.matches("^.+@.+\\..+$")) {
            alerta(Alert.AlertType.WARNING, "El mail no es válido.");
            return;
        }

        Document nuevo = new Document("dni", dni)
                .append("nombre", nombre)
                .append("apellido", apellido)
                .append("fechaAlta", new Date())
                .append("mail", mail)
                .append("contraseña", pass)
                .append("membresia", fMembresia.getValue())
                .append("logros", new ArrayList<Document>())
                .append("clases", new ArrayList<Document>());

        try {
            dao.insertarCliente(nuevo);
            listClientes.getItems().add(nuevo);
            listClientes.getSelectionModel().select(nuevo);
            listClientes.scrollTo(nuevo);
        } catch (MongoWriteException e) {
            if (e.getError().getCategory() == ErrorCategory.DUPLICATE_KEY) {
                alerta(Alert.AlertType.ERROR, "Ya existe un cliente con ese DNI o ese mail.");
            } else {
                alerta(Alert.AlertType.ERROR, "No se pudo guardar el cliente.");
            }
        } catch (MongoException e) {
            alerta(Alert.AlertType.ERROR, "No se puede conectar con MongoDB.");
        }
    }

    @FXML
    private void onDarDeBaja() {
        Document c = listClientes.getSelectionModel().getSelectedItem();
        if (c == null) {
            return;
        }

        Alert confirmar = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Seguro que quieres dar de baja a " + c.getString("nombre") + " "
                + c.getString("apellido") + "?", ButtonType.YES, ButtonType.NO);
        confirmar.setHeaderText(null);

        ButtonType respuesta = confirmar.showAndWait().orElse(ButtonType.NO);
        if (respuesta == ButtonType.YES) {
            try {
                dao.eliminarCliente(c.getObjectId("_id"));
                cargarClientes();
                mostrarCliente(null);
            } catch (MongoException e) {
                alerta(Alert.AlertType.ERROR, "No se pudo dar de baja al cliente.");
            }
        }
    }

    @FXML
    private void onCerrarSesion() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/mycompany/vista/Login.fxml"));
            Stage login = new Stage();
            login.setTitle("Tolotolo - Iniciar sesión");
            login.setScene(new Scene(root));
            login.show();

            ((Stage) raiz.getScene().getWindow()).close();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            alerta(Alert.AlertType.ERROR, "No se pudo volver al login.");
        }
    }

    @FXML
    private void onVolver() {
        try {
            java.net.URL ruta = getClass().getResource("/com/mycompany/vista/Gerente.fxml");
            if (ruta == null) {
                alerta(Alert.AlertType.ERROR, "No se encuentra el FXML del panel del gerente.");
                return;
            }

            // Guardamos la escena y la ventana ANTES de cambiar el contenido
            Scene escena = raiz.getScene();
            Stage ventana = (Stage) escena.getWindow();

            Parent root = FXMLLoader.load(ruta);
            escena.setRoot(root);
            ventana.setTitle("Tolotolo - Gerente");
        } catch (Exception e) {
            e.printStackTrace();
            alerta(Alert.AlertType.ERROR, "No se pudo volver: " + e.getMessage());
        }
    }

    private void alerta(Alert.AlertType tipo, String mensaje) {
        Alert a = new Alert(tipo, mensaje);
        a.setHeaderText(null);
        a.showAndWait();
    }
}
