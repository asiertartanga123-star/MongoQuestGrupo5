package com.mycompany.controlador;

import com.mongodb.ErrorCategory;
import com.mongodb.MongoException;
import com.mongodb.MongoWriteException;
import com.mycompany.controlador.dao.daoImplementacio;
import java.io.IOException;
import java.net.URL;
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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.bson.Document;

public class GestionMonitorController {

    @FXML private BorderPane raiz;
    @FXML private ListView<Document> listMonitores;
    @FXML private Label lblNombreCompleto;
    @FXML private ImageView imgFoto;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellidos;
    @FXML private TextField txtDni;
    @FXML private TextField txtMail;
    @FXML private TextField txtAlta;
    @FXML private TextField txtDireccion;
    @FXML private Button btnGuardar;
    @FXML private Button btnBaja;

    private final daoImplementacio dao = new daoImplementacio();
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Cada fila de la lista: nombre y apellido + dni y mail
    private static class CeldaMonitor extends ListCell<Document> {
        @Override
        protected void updateItem(Document m, boolean vacio) {
            super.updateItem(m, vacio);
            if (vacio || m == null) {
                setText(null);
            } else {
                setText(m.getString("nombre") + " " + m.getString("apellido")
                        + "\n" + m.getString("dni") + "  ·  " + m.getString("mail"));
            }
        }
    }

    @FXML
    private void initialize() {
        listMonitores.setCellFactory(new Callback<ListView<Document>, ListCell<Document>>() {
            @Override
            public ListCell<Document> call(ListView<Document> lista) {
                return new CeldaMonitor();
            }
        });

        // Al elegir un monitor se rellena la ficha
        listMonitores.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Document>() {
            @Override
            public void changed(ObservableValue<? extends Document> obs, Document anterior, Document elegido) {
                mostrarMonitor(elegido);
            }
        });

        cargarMonitores();
    }

    private void cargarMonitores() {
        try {
            List<Document> monitores = dao.listarMonitores();
            System.out.println("Monitores cargados: " + monitores.size());
            listMonitores.getItems().setAll(monitores);
        } catch (MongoException e) {
            e.printStackTrace();
            alerta(Alert.AlertType.ERROR, "No se puede conectar con MongoDB.");
        }
    }

    // Convierte direccion[0] en el texto "Calle, CP, Ciudad"
    private String direccionATexto(Document m) {
        List<Document> dirs = m.getList("direccion", Document.class);
        if (dirs == null || dirs.isEmpty()) {
            return "";
        }
        Document d = dirs.get(0);
        return d.getString("calle") + ", " + d.getString("cp") + ", " + d.getString("ciudad");
    }

    // Convierte "Calle, CP, Ciudad" en la lista de direcciones. Devuelve null si el formato es incorrecto
    private List<Document> textoADireccion(String texto) {
        String[] partes = texto.split(",");
        if (partes.length != 3) {
            return null;
        }
        String calle = partes[0].trim();
        String cp = partes[1].trim();
        String ciudad = partes[2].trim();
        if (calle.isEmpty() || cp.isEmpty() || ciudad.isEmpty()) {
            return null;
        }
        List<Document> lista = new ArrayList<Document>();
        lista.add(new Document("calle", calle).append("cp", cp).append("ciudad", ciudad));
        return lista;
    }

    private void mostrarMonitor(Document m) {
        if (m == null) {
            lblNombreCompleto.setText("Nombre y apellidos monitor");
            txtNombre.clear();
            txtApellidos.clear();
            txtDni.clear();
            txtMail.clear();
            txtAlta.clear();
            txtDireccion.clear();
            btnGuardar.setDisable(true);
            btnBaja.setDisable(true);
            return;
        }
        lblNombreCompleto.setText(m.getString("nombre") + " " + m.getString("apellido"));
        txtNombre.setText(m.getString("nombre"));
        txtApellidos.setText(m.getString("apellido"));
        txtDni.setText(m.getString("dni"));
        txtMail.setText(m.getString("mail"));
        txtDireccion.setText(direccionATexto(m));

        Date alta = m.getDate("fechaAlta");
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
        Document m = listMonitores.getSelectionModel().getSelectedItem();
        if (m == null) {
            return;
        }

        String nombre = txtNombre.getText().trim();
        String apellido = txtApellidos.getText().trim();
        String dni = txtDni.getText().trim();
        String mail = txtMail.getText().trim();

        if (nombre.isEmpty() || apellido.isEmpty() || dni.isEmpty() || mail.isEmpty()) {
            alerta(Alert.AlertType.WARNING, "Rellena todos los campos.");
            return;
        }
        if (!mail.matches("^.+@.+\\..+$")) {
            alerta(Alert.AlertType.WARNING, "El mail no es válido.");
            return;
        }
        List<Document> direccion = textoADireccion(txtDireccion.getText());
        if (direccion == null) {
            alerta(Alert.AlertType.WARNING, "La dirección debe tener el formato: Calle, CP, Ciudad");
            return;
        }

        try {
            dao.actualizarMonitor(m.getObjectId("_id"), nombre, apellido, dni, mail, direccion);

            // Actualiza el monitor en memoria y refresca la lista y el título
            m.put("nombre", nombre);
            m.put("apellido", apellido);
            m.put("dni", dni);
            m.put("mail", mail);
            m.put("direccion", direccion);
            listMonitores.refresh();
            lblNombreCompleto.setText(nombre + " " + apellido);

            alerta(Alert.AlertType.INFORMATION, "Datos guardados.");
        } catch (MongoWriteException e) {
            if (e.getError().getCategory() == ErrorCategory.DUPLICATE_KEY) {
                alerta(Alert.AlertType.ERROR, "Ya existe otro trabajador con ese DNI o ese mail.");
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
        TextField fCalle = new TextField();
        TextField fCp = new TextField();
        TextField fCiudad = new TextField();

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
        grid.add(new Label("Calle:"), 0, 5);
        grid.add(fCalle, 1, 5);
        grid.add(new Label("CP:"), 0, 6);
        grid.add(fCp, 1, 6);
        grid.add(new Label("Ciudad:"), 0, 7);
        grid.add(fCiudad, 1, 7);

        Dialog<ButtonType> dialogo = new Dialog<ButtonType>();
        dialogo.setTitle("Dar de alta monitor");
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
        String calle = fCalle.getText().trim();
        String cp = fCp.getText().trim();
        String ciudad = fCiudad.getText().trim();

        if (dni.isEmpty() || nombre.isEmpty() || apellido.isEmpty() || mail.isEmpty()
                || pass.isEmpty() || calle.isEmpty() || cp.isEmpty() || ciudad.isEmpty()) {
            alerta(Alert.AlertType.WARNING, "Rellena todos los campos.");
            return;
        }
        if (!mail.matches("^.+@.+\\..+$")) {
            alerta(Alert.AlertType.WARNING, "El mail no es válido.");
            return;
        }

        List<Document> direccion = new ArrayList<Document>();
        direccion.add(new Document("calle", calle).append("cp", cp).append("ciudad", ciudad));

        Document nuevo = new Document("dni", dni)
                .append("nombre", nombre)
                .append("apellido", apellido)
                .append("fechaAlta", new Date())
                .append("mail", mail)
                .append("contraseña", pass)
                .append("direccion", direccion)
                .append("tipo", "monitor")
                .append("clases", new ArrayList<Document>());

        try {
            dao.insertarMonitor(nuevo);
            listMonitores.getItems().add(nuevo);
            listMonitores.getSelectionModel().select(nuevo);
            listMonitores.scrollTo(nuevo);
        } catch (MongoWriteException e) {
            if (e.getError().getCategory() == ErrorCategory.DUPLICATE_KEY) {
                alerta(Alert.AlertType.ERROR, "Ya existe un trabajador con ese DNI o ese mail.");
            } else {
                alerta(Alert.AlertType.ERROR, "No se pudo guardar el monitor.");
            }
        } catch (MongoException e) {
            alerta(Alert.AlertType.ERROR, "No se puede conectar con MongoDB.");
        }
    }

    @FXML
    private void onDarDeBaja() {
        Document m = listMonitores.getSelectionModel().getSelectedItem();
        if (m == null) {
            return;
        }

        Alert confirmar = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Seguro que quieres dar de baja a " + m.getString("nombre") + " "
                + m.getString("apellido") + "?", ButtonType.YES, ButtonType.NO);
        confirmar.setHeaderText(null);

        ButtonType respuesta = confirmar.showAndWait().orElse(ButtonType.NO);
        if (respuesta == ButtonType.YES) {
            try {
                dao.eliminarMonitor(m.getObjectId("_id"));
                cargarMonitores();
                mostrarMonitor(null);
            } catch (MongoException e) {
                alerta(Alert.AlertType.ERROR, "No se pudo dar de baja al monitor.");
            }
        }
    }

    @FXML
    private void onVolver() {
        try {
            URL ruta = getClass().getResource("/com/mycompany/vista/Gerente.fxml");
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

    private void alerta(Alert.AlertType tipo, String mensaje) {
        Alert a = new Alert(tipo, mensaje);
        a.setHeaderText(null);
        a.showAndWait();
    }
}