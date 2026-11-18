package com.mycompany.controlador;

import com.mycompany.controlador.dao.DatosException;
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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * Panel de gestión de clases (arriba) y de sus sesiones (abajo).
 * Los cambios de nombre, fecha y aforo se guardan al pulsar Enter en el campo;
 * los de sala y monitor, al elegir otra opción en el desplegable.
 */
public class GestionClasesController {

    /** Crea el controlador; lo instancia JavaFX al cargar el FXML. */
    public GestionClasesController() {
    }

    /** Contenedor raíz de la vista; da acceso a la escena y a la ventana. */
    @FXML private BorderPane raiz;

    // Clases
    /** Lista de todas las clases (nombre y sala). */
    @FXML private ListView<Document> listClases;
    /** Encabezado de la ficha de la clase seleccionada. */
    @FXML private Label lblClase;
    /** Nombre de la clase seleccionada; se guarda al pulsar Enter. */
    @FXML private TextField txtNombreClase;
    /** Sala de la clase seleccionada; se guarda al elegir otra. */
    @FXML private ChoiceBox<Document> choSala;
    /** Abre el horario semanal de la clase seleccionada. */
    @FXML private Button btnGestionarSesiones;
    /** Borra la clase seleccionada y sus sesiones. */
    @FXML private Button btnBorrarClase;

    // Sesiones
    /** Sesiones de la clase seleccionada (fecha y plazas ocupadas). */
    @FXML private ListView<Document> listSesiones;
    /** Encabezado de la ficha de la sesión seleccionada. */
    @FXML private Label lblSesion;
    /** Fecha y hora de la sesión ({@code dd/MM/yyyy HH:mm}); se guarda al pulsar Enter. */
    @FXML private TextField txtNombreSesion;
    /** Monitor que imparte la sesión; se guarda al elegir otro. */
    @FXML private ChoiceBox<Document> choMonitor;
    /** Nombres de los clientes apuntados a la sesión. */
    @FXML private ListView<String> listInscritos;
    /** Plazas máximas de la sesión; se guarda al pulsar Enter. */
    @FXML private TextField txtAforo;
    /** Borra la sesión seleccionada. */
    @FXML private Button btnBorrarSesion;

    /** Acceso a MongoDB. */
    private final daoImplementacio dao = new daoImplementacio();
    /** Formato con el que se muestran y se escriben las fechas de las sesiones. */
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Nombre y apellido de cada cliente por su _id, para la lista de inscritos. */
    private final Map<ObjectId, String> nombreClientes = new HashMap<ObjectId, String>();

    /**
     * Vale {@code true} mientras se rellena la ficha por código, para que los
     * listeners de los desplegables no lo tomen como un cambio del usuario y guarden.
     */
    private boolean rellenando = false;

    // ---------- Utilidades de presentación ----------

    /** Cómo convertir un documento en el texto que se ve en una lista o un desplegable. */
    private interface Texto {
        /**
         * Devuelve el texto que representa al documento.
         *
         * @param d documento que se va a mostrar
         * @return texto visible
         */
        String de(Document d);
    }

    /**
     * Crea el conversor de un {@code ChoiceBox} que muestra cada documento con el texto indicado.
     * Solo convierte en un sentido: {@code fromString} no se usa y devuelve {@code null}.
     *
     * @param t cómo se escribe cada documento
     * @return conversor para {@link ChoiceBox#setConverter(StringConverter)}
     */
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

    /**
     * Crea una celda de {@code ListView} que muestra cada documento con el texto indicado.
     *
     * @param t cómo se escribe cada documento
     * @return celda para la fábrica de celdas de la lista
     */
    private ListCell<Document> celda(final Texto t) {
        return new ListCell<Document>() {
            @Override
            protected void updateItem(Document d, boolean vacio) {
                super.updateItem(d, vacio);
                setText(vacio || d == null ? null : t.de(d));
            }
        };
    }

    /**
     * Escribe una fecha con el formato {@link #FECHA_HORA} en la zona horaria del equipo.
     *
     * @param fecha fecha guardada en MongoDB
     * @return la fecha como texto, o cadena vacía si es {@code null}
     */
    private String fechaATexto(Date fecha) {
        return fecha == null ? "" : fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().format(FECHA_HORA);
    }

    /**
     * Busca en una lista el documento con un _id dado.
     *
     * @param lista documentos en los que buscar
     * @param id identificador buscado
     * @return el documento, o {@code null} si no está
     */
    private Document buscarPorId(List<Document> lista, ObjectId id) {
        for (Document d : lista) {
            if (d.getObjectId("_id").equals(id)) {
                return d;
            }
        }
        return null;
    }

    /**
     * Devuelve el nombre de una sala a partir de las salas ya cargadas en {@link #choSala}.
     *
     * @param salaId identificador (_id) de la sala
     * @return nombre de la sala, o {@code "sin sala"} si no se encuentra
     */
    private String nombreSala(ObjectId salaId) {
        Document sala = buscarPorId(choSala.getItems(), salaId);
        return sala == null ? "sin sala" : sala.getString("nombre");
    }

    // ---------- Arranque ----------

    /**
     * Lo llama JavaFX al cargar el FXML: prepara cómo se pintan listas y desplegables,
     * engancha el guardado automático (Enter en los campos, cambio en los desplegables)
     * y carga de MongoDB las salas, los monitores, los clientes y las clases.
     */
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

        listClases.setCellFactory(new Callback<ListView<Document>, ListCell<Document>>() {
            @Override
            public ListCell<Document> call(ListView<Document> lv) {
                return celda(textoClase);
            }
        });
        listSesiones.setCellFactory(new Callback<ListView<Document>, ListCell<Document>>() {
            @Override
            public ListCell<Document> call(ListView<Document> lv) {
                return celda(textoSesion);
            }
        });
        choSala.setConverter(conversor(new Texto() {
            public String de(Document d) {
                return d.getString("nombre") + " (" + d.getInteger("aforoMaximo", 0) + ")";
            }
        }));
        choMonitor.setConverter(conversor(new Texto() {
            public String de(Document d) {
                return d.getString("nombre") + " " + d.getString("apellido");
            }
        }));

        listClases.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Document>() {
            @Override
            public void changed(ObservableValue<? extends Document> obs, Document antes, Document clase) {
                mostrarClase(clase);
            }
        });
        listSesiones.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Document>() {
            @Override
            public void changed(ObservableValue<? extends Document> obs, Document antes, Document sesion) {
                mostrarSesion(sesion);
            }
        });

        // Guardar al cambiar sala o monitor (solo si lo ha cambiado el usuario)
        choSala.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Document>() {
            @Override
            public void changed(ObservableValue<? extends Document> obs, Document antes, Document sala) {
                if (!rellenando && sala != null) {
                    guardarClase();
                }
            }
        });
        choMonitor.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Document>() {
            @Override
            public void changed(ObservableValue<? extends Document> obs, Document antes, Document monitor) {
                if (!rellenando && monitor != null) {
                    guardarSesion();
                }
            }
        });

        // Enter en un campo = guardar
        txtNombreClase.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                guardarClase();
            }
        });
        EventHandler<ActionEvent> guardarSesionAlPulsarEnter = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                guardarSesion();
            }
        };
        txtNombreSesion.setOnAction(guardarSesionAlPulsarEnter);
        txtAforo.setOnAction(guardarSesionAlPulsarEnter);

        try {
            choSala.getItems().setAll(dao.listarSalas());
            choMonitor.getItems().setAll(dao.listarMonitores());
            for (Document c : dao.listarClientes()) {
                nombreClientes.put(c.getObjectId("_id"), c.getString("nombre") + " " + c.getString("apellido"));
            }
            listClases.getItems().setAll(dao.listarClases());
        } catch (DatosException e) {
            Registro.error("Error en " + getClass().getSimpleName(), e);
            alerta(Alert.AlertType.ERROR, "No se puede conectar con MongoDB.");
        }

        mostrarClase(null);
    }

    // ---------- Clases ----------

    /**
     * Rellena la ficha con la clase elegida y carga sus sesiones. Con {@code null}
     * vacía la ficha y desactiva sus controles.
     *
     * @param c clase seleccionada, o {@code null} si no hay ninguna
     */
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
            } catch (DatosException e) {
                alerta(Alert.AlertType.ERROR, "No se pudieron cargar las sesiones.");
            }
        }
        mostrarSesion(null);
    }

    /**
     * Guarda en MongoDB el nombre y la sala de la ficha para la clase seleccionada.
     * Avisa y no guarda si falta el nombre o la sala.
     */
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
        } catch (DatosException e) {
            alerta(Alert.AlertType.ERROR, "No se pudo guardar la clase.");
        }
    }

    /**
     * Pide nombre y sala en un diálogo, crea la clase con el horario vacío
     * y la deja seleccionada en la lista.
     */
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
        } catch (DatosException e) {
            alerta(Alert.AlertType.ERROR, "No se pudo crear la clase.");
        }
    }

    /** Tras confirmarlo el usuario, borra la clase seleccionada junto con todas sus sesiones. */
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
        } catch (DatosException e) {
            alerta(Alert.AlertType.ERROR, "No se pudo borrar la clase.");
        }
    }

    // ---------- Sesiones ----------

    /**
     * Botón «Gestionar sesiones»: abre en la misma ventana el horario semanal
     * ({@code GestorSesiones.fxml}) de la clase seleccionada.
     */
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
            Registro.error("Error en " + getClass().getSimpleName(), e);
            alerta(Alert.AlertType.ERROR, "No se pudo abrir el horario de sesiones.");
        }
    }

    /**
     * Rellena la ficha con la sesión elegida: fecha, aforo, monitor y clientes apuntados.
     * Con {@code null} vacía la ficha y desactiva sus controles.
     *
     * @param s sesión seleccionada, o {@code null} si no hay ninguna
     */
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

    /**
     * Guarda en MongoDB la fecha, el aforo y el monitor de la ficha para la sesión seleccionada.
     * Avisa y no guarda si la fecha no tiene el formato {@code dd/MM/yyyy HH:mm},
     * si el aforo no es válido o si no hay monitor.
     */
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
        } catch (DatosException e) {
            alerta(Alert.AlertType.ERROR, "No se pudo guardar la sesión.");
        }
    }

    /**
     * Comprueba el aforo escrito: debe ser un entero mayor que 0, no superar el aforo
     * de la sala de la clase y no ser menor que el número de clientes ya apuntados.
     * Si no es válido, avisa al usuario.
     *
     * @param texto aforo escrito por el usuario
     * @param clase clase a la que pertenece la sesión (para saber su sala)
     * @param apuntados clientes ya apuntados a la sesión
     * @return el aforo, o {@code null} si no es válido
     */
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

    /** Tras confirmarlo el usuario, borra la sesión seleccionada. */
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
        } catch (DatosException e) {
            alerta(Alert.AlertType.ERROR, "No se pudo borrar la sesión.");
        }
    }

    // ---------- Navegación ----------

    /** Vuelve al menú del gerente o del monitor según el rol en sesión. */
    @FXML
    private void onVolver() {
        try {
            boolean esMonitor = "monitor".equalsIgnoreCase(LoginController.rolActual);
            String fxml = esMonitor ? "/com/mycompany/vista/Monitor.fxml" : "/com/mycompany/vista/Gerente.fxml";
            String titulo = esMonitor ? "Tolotolo - Monitor" : "Tolotolo - Gerente";

            Scene escena = raiz.getScene();
            Stage ventana = (Stage) escena.getWindow();
            escena.setRoot(FXMLLoader.load(getClass().getResource(fxml)));
            ventana.setTitle(titulo);
        } catch (Exception e) {
            Registro.error("Error en " + getClass().getSimpleName(), e);
            alerta(Alert.AlertType.ERROR, "No se pudo volver: " + e.getMessage());
        }
    }

    /** Cierra la sesión actual: abre la ventana de login y cierra esta. */
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
            Registro.error("Error en " + getClass().getSimpleName(), e);
            alerta(Alert.AlertType.ERROR, "No se pudo volver al login.");
        }
    }

    // ---------- Diálogos ----------

    /**
     * Convierte una fecha local en el {@code Date} que guarda MongoDB, usando la zona horaria del equipo.
     *
     * @param f fecha y hora local
     * @return la misma fecha como {@code Date}
     */
    private Date aDate(LocalDateTime f) {
        return Date.from(f.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Crea una rejilla vacía con márgenes para montar el formulario de un diálogo.
     *
     * @return rejilla con separación de 10 px y margen de 20 px
     */
    private GridPane formulario() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        return grid;
    }

    /**
     * Muestra un diálogo con un formulario y botones Aceptar y Cancelar, y espera la respuesta.
     *
     * @param titulo título de la ventana del diálogo
     * @param contenido formulario que se muestra
     * @return {@code true} si el usuario pulsa Aceptar
     */
    private boolean pedir(String titulo, GridPane contenido) {
        Dialog<ButtonType> dialogo = new Dialog<ButtonType>();
        dialogo.setTitle(titulo);
        dialogo.getDialogPane().setContent(contenido);
        dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        return dialogo.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    /**
     * Hace una pregunta de sí o no y espera la respuesta.
     *
     * @param pregunta texto de la pregunta
     * @return {@code true} si el usuario responde Sí
     */
    private boolean confirmar(String pregunta) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, pregunta, ButtonType.YES, ButtonType.NO);
        a.setHeaderText(null);
        return a.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }

    /**
     * Muestra un aviso y espera a que el usuario lo cierre.
     *
     * @param tipo tipo de aviso (error, advertencia…)
     * @param mensaje texto del aviso
     */
    private void alerta(Alert.AlertType tipo, String mensaje) {
        Alert a = new Alert(tipo, mensaje);
        a.setHeaderText(null);
        a.showAndWait();
    }
}