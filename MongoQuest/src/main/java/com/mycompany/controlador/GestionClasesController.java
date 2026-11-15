package com.mycompany.controlador;

import com.mongodb.MongoException;
import com.mycompany.controlador.dao.daoImplementacio;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
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
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * Panel de gestión de clases (arriba) y de sus sesiones (abajo).
 * Los cambios de nombre, fecha y aforo se guardan al pulsar Enter en el campo;
 * los de sala y monitor, al elegir otra opción en el desplegable.
 */
public class GestionClasesController {

    @FXML private BorderPane raiz;

    // Clases
    @FXML private ListView<Document> listClases;
    @FXML private Label lblClase;
    @FXML private TextField txtNombreClase;
    @FXML private ChoiceBox<Document> choSala;
    @FXML private Button btnGestionarSesiones;
    @FXML private Button btnBorrarClase;

    // Sesiones
    @FXML private ListView<Document> listSesiones;
    @FXML private Label lblSesion;
    @FXML private TextField txtNombreSesion;   // fecha y hora de la sesión
    @FXML private ChoiceBox<Document> choMonitor;
    @FXML private ListView<String> listInscritos;
    @FXML private TextField txtAforo;
    @FXML private Button btnBorrarSesion;

    private final daoImplementacio dao = new daoImplementacio();
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final Map<ObjectId, String> nombreClientes = new HashMap<ObjectId, String>();

    // Mientras rellenamos la ficha por código, los listeners de los desplegables no deben guardar
    private boolean rellenando = false;

    // ---------- Utilidades de presentación ----------

    // Muestra en un ChoiceBox/ListCell el texto que devuelve "texto" en vez del Document entero
    private interface Texto {
        String de(Document d);
    }

    private StringConverter<Document> conversor(final Texto t) {
        return new StringConverter<Document>() {
            @Override
            public String toString(Document d) {
                return d == null ? "" : t.de(d);
            }

            @Override
            public Document fromString(String s) {
                return null;
            }
        };
    }

    private ListCell<Document> celda(final Texto t) {
        return new ListCell<Document>() {
            @Override
            protected void updateItem(Document d, boolean vacio) {
                super.updateItem(d, vacio);
                setText(vacio || d == null ? null : t.de(d));
            }
        };
    }

    private String fechaATexto(Date fecha) {
        return fecha == null ? "" : fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().format(FECHA_HORA);
    }

    private Document buscarPorId(List<Document> lista, ObjectId id) {
        for (Document d : lista) {
            if (d.getObjectId("_id").equals(id)) {
                return d;
            }
        }
        return null;
    }

    private String nombreSala(ObjectId salaId) {
        Document sala = buscarPorId(choSala.getItems(), salaId);
        return sala == null ? "sin sala" : sala.getString("nombre");
    }

    // ---------- Arranque ----------

    @FXML
    private void initialize() {
        final Texto textoClase = new Texto() {
            public String de(Document c) {
                return c.getString("nombre") + "\n" + nombreSala(c.getObjectId("salaid"));
            }
        };
        final Texto textoSesion = new Texto() {
            public String de(Document s) {
                List<Document> asist = s.getList("asistentes", Document.class);
                int apuntados = asist == null ? 0 : asist.size();
                return fechaATexto(s.getDate("fecha")) + "\n" + apuntados + "/" + s.getInteger("plazaMax", 0) + " plazas";
            }
        };

        listClases.setCellFactory(lv -> celda(textoClase));
        listSesiones.setCellFactory(lv -> celda(textoSesion));
        choSala.setConverter(conversor(d -> d.getString("nombre") + " (" + d.getInteger("aforoMaximo", 0) + ")"));
        choMonitor.setConverter(conversor(d -> d.getString("nombre") + " " + d.getString("apellido")));

        listClases.getSelectionModel().selectedItemProperty().addListener((obs, antes, clase) -> mostrarClase(clase));
        listSesiones.getSelectionModel().selectedItemProperty().addListener((obs, antes, sesion) -> mostrarSesion(sesion));

        // Guardar al cambiar sala o monitor (solo si lo ha cambiado el usuario)
        choSala.getSelectionModel().selectedItemProperty().addListener((obs, antes, sala) -> {
            if (!rellenando && sala != null) {
                guardarClase();
            }
        });
        choMonitor.getSelectionModel().selectedItemProperty().addListener((obs, antes, monitor) -> {
            if (!rellenando && monitor != null) {
                guardarSesion();
            }
        });

        // Enter en un campo = guardar
        txtNombreClase.setOnAction(e -> guardarClase());
        txtNombreSesion.setOnAction(e -> guardarSesion());
        txtAforo.setOnAction(e -> guardarSesion());

        try {
            choSala.getItems().setAll(dao.listarSalas());
            choMonitor.getItems().setAll(dao.listarMonitores());
            for (Document c : dao.listarClientes()) {
                nombreClientes.put(c.getObjectId("_id"), c.getString("nombre") + " " + c.getString("apellido"));
            }
            listClases.getItems().setAll(dao.listarClases());
        } catch (MongoException e) {
            e.printStackTrace();
            alerta(Alert.AlertType.ERROR, "No se puede conectar con MongoDB.");
        }

        mostrarClase(null);
    }

    // ---------- Clases ----------

    private void mostrarClase(Document c) {
        rellenando = true;
        boolean hay = c != null;
        lblClase.setText(hay ? "Datos " + c.getString("nombre") : "Datos clase");
        txtNombreClase.setText(hay ? c.getString("nombre") : "");
        choSala.getSelectionModel().select(hay ? buscarPorId(choSala.getItems(), c.getObjectId("salaid")) : null);
        txtNombreClase.setDisable(!hay);
        choSala.setDisable(!hay);
        btnGestionarSesiones.setDisable(!hay);
        btnBorrarClase.setDisable(!hay);
        rellenando = false;

        listSesiones.getItems().clear();
        if (hay) {
            try {
                listSesiones.getItems().setAll(dao.listarSesiones(c.getObjectId("_id")));
            } catch (MongoException e) {
                alerta(Alert.AlertType.ERROR, "No se pudieron cargar las sesiones.");
            }
        }
        mostrarSesion(null);
    }

    private void guardarClase() {
        Document c = listClases.getSelectionModel().getSelectedItem();
        Document sala = choSala.getValue();
        String nombre = txtNombreClase.getText().trim();
        if (c == null) {
            return;
        }
        if (nombre.isEmpty() || sala == null) {
            alerta(Alert.AlertType.WARNING, "La clase necesita nombre y sala.");
            return;
        }
        try {
            dao.actualizarClase(c.getObjectId("_id"), nombre, sala.getObjectId("_id"));
            c.put("nombre", nombre);
            c.put("salaid", sala.getObjectId("_id"));
            listClases.refresh();
            lblClase.setText("Datos " + nombre);
        } catch (MongoException e) {
            alerta(Alert.AlertType.ERROR, "No se pudo guardar la clase.");
        }
    }

    @FXML
    private void onCrearClase() {
        TextField fNombre = new TextField();
        ChoiceBox<Document> fSala = new ChoiceBox<Document>(choSala.getItems());
        fSala.setConverter(choSala.getConverter());

        GridPane grid = formulario();
        grid.addRow(0, new Label("Nombre:"), fNombre);
        grid.addRow(1, new Label("Sala:"), fSala);

        if (!pedir("Crear clase", grid)) {
            return;
        }
        String nombre = fNombre.getText().trim();
        if (nombre.isEmpty() || fSala.getValue() == null) {
            alerta(Alert.AlertType.WARNING, "La clase necesita nombre y sala.");
            return;
        }

        // ponytail: el horario ("horas") se crea vacío; este panel no lo edita
        Document nueva = new Document("nombre", nombre)
                .append("horas", new ArrayList<Document>())
                .append("salaid", fSala.getValue().getObjectId("_id"));
        try {
            dao.insertarClase(nueva);   // el driver rellena _id en "nueva"
            listClases.getItems().add(nueva);
            listClases.getSelectionModel().select(nueva);
            listClases.scrollTo(nueva);
        } catch (MongoException e) {
            alerta(Alert.AlertType.ERROR, "No se pudo crear la clase.");
        }
    }

    @FXML
    private void onBorrarClase() {
        Document c = listClases.getSelectionModel().getSelectedItem();
        if (c == null || !confirmar("¿Borrar la clase " + c.getString("nombre")
                + " y todas sus sesiones (" + listSesiones.getItems().size() + ")?")) {
            return;
        }
        try {
            dao.eliminarClase(c.getObjectId("_id"));
            listClases.getItems().remove(c);
            listClases.getSelectionModel().clearSelection();
        } catch (MongoException e) {
            alerta(Alert.AlertType.ERROR, "No se pudo borrar la clase.");
        }
    }

    // ---------- Sesiones ----------

    // "Gestionar sesiones": abre el horario semanal de la clase elegida en la misma ventana
    @FXML
    private void onGestionarSesiones() {
        Document c = listClases.getSelectionModel().getSelectedItem();
        if (c == null) {
            return;
        }
        try {
            FXMLLoader cargador = new FXMLLoader(getClass().getResource("/com/mycompany/vista/GestorSesiones.fxml"));
            Parent vista = cargador.load();
            GestionSesionesController horario = cargador.getController();
            horario.setClase(c);

            Scene escena = raiz.getScene();
            escena.setRoot(vista);
            ((Stage) escena.getWindow()).setTitle("Tolotolo - Sesiones de " + c.getString("nombre"));
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            alerta(Alert.AlertType.ERROR, "No se pudo abrir el horario de sesiones.");
        }
    }

    private void mostrarSesion(Document s) {
        rellenando = true;
        boolean hay = s != null;
        lblSesion.setText(hay ? "Sesión " + fechaATexto(s.getDate("fecha")) : "Datos sesion");
        txtNombreSesion.setText(hay ? fechaATexto(s.getDate("fecha")) : "");
        txtAforo.setText(hay ? String.valueOf(s.getInteger("plazaMax", 0)) : "");
        choMonitor.getSelectionModel().select(hay ? buscarPorId(choMonitor.getItems(), s.getObjectId("monitor")) : null);

        listInscritos.getItems().clear();
        if (hay && s.getList("asistentes", Document.class) != null) {
            for (Document a : s.getList("asistentes", Document.class)) {
                String nombre = nombreClientes.get(a.getObjectId("clienteid"));
                listInscritos.getItems().add(nombre == null ? "(cliente borrado)" : nombre);
            }
        }

        txtNombreSesion.setDisable(!hay);
        txtAforo.setDisable(!hay);
        choMonitor.setDisable(!hay);
        btnBorrarSesion.setDisable(!hay);
        rellenando = false;
    }

    private void guardarSesion() {
        Document s = listSesiones.getSelectionModel().getSelectedItem();
        Document c = listClases.getSelectionModel().getSelectedItem();
        if (s == null || c == null) {
            return;
        }
        LocalDateTime fecha;
        try {
            fecha = LocalDateTime.parse(txtNombreSesion.getText().trim(), FECHA_HORA);
        } catch (DateTimeParseException e) {
            alerta(Alert.AlertType.WARNING, "La fecha debe tener el formato dd/MM/yyyy HH:mm.");
            return;
        }
        List<Document> asist = s.getList("asistentes", Document.class);
        Integer aforo = validarAforo(txtAforo.getText(), c, asist == null ? 0 : asist.size());
        Document monitor = choMonitor.getValue();
        if (aforo == null || monitor == null) {
            return;
        }
        try {
            Date d = aDate(fecha);
            dao.actualizarSesion(s.getObjectId("_id"), d, aforo, monitor.getObjectId("_id"));
            s.put("fecha", d);
            s.put("plazaMax", aforo);
            s.put("monitor", monitor.getObjectId("_id"));
            listSesiones.refresh();
            lblSesion.setText("Sesión " + fechaATexto(d));
        } catch (MongoException e) {
            alerta(Alert.AlertType.ERROR, "No se pudo guardar la sesión.");
        }
    }

    // Aforo: número entero, mayor que 0, que quepa en la sala y que no deje fuera a los ya apuntados
    private Integer validarAforo(String texto, Document clase, int apuntados) {
        int aforo;
        try {
            aforo = Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            alerta(Alert.AlertType.WARNING, "El aforo debe ser un número.");
            return null;
        }
        Document sala = buscarPorId(choSala.getItems(), clase.getObjectId("salaid"));
        int maxSala = sala == null ? Integer.MAX_VALUE : sala.getInteger("aforoMaximo", Integer.MAX_VALUE);
        if (aforo <= 0 || aforo > maxSala) {
            alerta(Alert.AlertType.WARNING, "El aforo debe estar entre 1 y " + maxSala + " (aforo de la sala).");
            return null;
        }
        if (aforo < apuntados) {
            alerta(Alert.AlertType.WARNING, "Ya hay " + apuntados + " clientes apuntados; el aforo no puede ser menor.");
            return null;
        }
        return aforo;
    }

    @FXML
    private void onBorrarSesion() {
        Document s = listSesiones.getSelectionModel().getSelectedItem();
        if (s == null || !confirmar("¿Borrar la sesión del " + fechaATexto(s.getDate("fecha")) + "?")) {
            return;
        }
        try {
            dao.eliminarSesion(s.getObjectId("_id"));
            listSesiones.getItems().remove(s);
            listSesiones.getSelectionModel().clearSelection();
        } catch (MongoException e) {
            alerta(Alert.AlertType.ERROR, "No se pudo borrar la sesión.");
        }
    }

    // ---------- Navegación (igual que en los otros gestores) ----------

    @FXML
    private void onVolver() {
        try {
            Scene escena = raiz.getScene();
            Stage ventana = (Stage) escena.getWindow();
            escena.setRoot(FXMLLoader.load(getClass().getResource("/com/mycompany/vista/Gerente.fxml")));
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

    // ---------- Diálogos ----------

    private Date aDate(LocalDateTime f) {
        return Date.from(f.atZone(ZoneId.systemDefault()).toInstant());
    }

    private GridPane formulario() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        return grid;
    }

    private boolean pedir(String titulo, GridPane contenido) {
        Dialog<ButtonType> dialogo = new Dialog<ButtonType>();
        dialogo.setTitle(titulo);
        dialogo.getDialogPane().setContent(contenido);
        dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        return dialogo.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private boolean confirmar(String pregunta) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, pregunta, ButtonType.YES, ButtonType.NO);
        a.setHeaderText(null);
        return a.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }

    private void alerta(Alert.AlertType tipo, String mensaje) {
        Alert a = new Alert(tipo, mensaje);
        a.setHeaderText(null);
        a.showAndWait();
    }
}
