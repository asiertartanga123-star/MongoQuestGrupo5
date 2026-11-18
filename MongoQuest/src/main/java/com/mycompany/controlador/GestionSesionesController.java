package com.mycompany.controlador;

import com.mycompany.controlador.dao.DatosException;
import com.mycompany.controlador.dao.daoImplementacio;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * Horario semanal fijo de una clase (GestorSesiones.fxml).
 * Cada hueco de la rejilla es una franja de una hora, de lunes a sábado.
 * <ul>
 * <li>Hueco libre: pide monitor y aforo, lo añade al horario ({@code horas} de la clase)
 * y crea la sesión de la próxima fecha que cae en ese día y hora.</li>
 * <li>Hueco ocupado: muestra monitor y aforo de su próxima sesión. «Guardar» edita todas
 * las sesiones futuras de la franja; «Borrar» la quita del horario y borra esas sesiones.
 * Las sesiones pasadas no se tocan.</li>
 * </ul>
 * Lo abre "Gestionar sesiones" desde el panel de clases, que llama a setClase().
 */
public class GestionSesionesController {

    /** Crea el controlador con el acceso a datos real; es el que usa JavaFX al cargar el FXML. */
    public GestionSesionesController() {
        this(new daoImplementacio());
    }

    /**
     * Crea el controlador con el acceso a datos indicado; se usa en los tests de errores de MongoDB.
     *
     * @param dao acceso a datos que usará el controlador
     */
    public GestionSesionesController(daoImplementacio dao) {
        this.dao = dao;
    }

    /** Nombre del día tal como se guarda en {@code horas.dia}; el índice es la columna de la rejilla menos 1. */
    private static final String[] DIAS = {"lunes", "martes", "miércoles", "jueves", "viernes", "sábado"};
    /** Estilo de un hueco libre (el mismo que trae el FXML). */
    private static final String ESTILO_LIBRE = "-fx-background-color: #d9d9d9; -fx-border-color: black; -fx-border-width: 0.5; -fx-cursor: hand;";
    /** Estilo de un hueco que forma parte del horario de la clase. */
    private static final String ESTILO_OCUPADO = "-fx-background-color: black; -fx-border-color: black; -fx-border-width: 0.5; -fx-cursor: hand;";

    /** Contenedor raíz de la vista; da acceso a la escena y a la ventana. */
    @FXML private BorderPane raiz;
    /** Título del horario: nombre de la clase. Al pulsarlo se vuelve al panel de clases. */
    @FXML private Label lblTitulo;
    /** Rejilla del horario semanal de la clase. */
    @FXML private GridPane gridHorario;

    /** Acceso a MongoDB. */
    private final daoImplementacio dao;
    /** Clase cuyo horario se muestra; la fija {@link #setClase(Document)}. */
    private Document clase;

    /** Engancha el clic de cada hueco de la rejilla. */
    @FXML
    private void initialize() {
        for (Node n : gridHorario.getChildren()) {
            if (n.getStyleClass().contains("hueco-sesion")) {
                final StackPane hueco = (StackPane) n;
                hueco.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent e) {
                        onHueco(hueco);
                    }
                });
            }
        }
    }

    /**
     * Indica qué clase se va a mostrar, pone su nombre como título y pinta su horario.
     * Hay que llamarlo justo después de cargar el FXML.
     *
     * @param clase documento de la colección {@code Clases}
     */
    public void setClase(Document clase) {
        this.clase = clase;
        lblTitulo.setText(clase.getString("nombre"));
        pintar();
    }

    // ---------- Horario ----------

    /**
     * Devuelve la lista {@code horas} de la clase, creándola si no existe.
     *
     * @return franjas del horario (la misma lista que guarda el documento)
     */
    private List<Document> horas() {
        List<Document> h = clase.getList("horas", Document.class);
        if (h == null) {
            h = new ArrayList<Document>();
            clase.put("horas", h);
        }
        return h;
    }

    /**
     * Busca en el horario la franja de un día y hora.
     *
     * @param dia nombre del día ({@link #DIAS})
     * @param hora hora de inicio (9 a 22)
     * @return la franja, o {@code null} si ese hueco está libre
     */
    private Document franja(String dia, int hora) {
        String inicio = String.format("%02d:00", hora);
        for (Document f : horas()) {
            if (dia.equalsIgnoreCase(f.getString("dia")) && inicio.equals(f.getString("horainicio"))) {
                return f;
            }
        }
        return null;
    }

    /** Pinta cada hueco como ocupado (negro, con el nombre de la clase) o libre (gris). */
    private void pintar() {
        for (Node n : gridHorario.getChildren()) {
            if (!n.getStyleClass().contains("hueco-sesion")) {
                continue;
            }
            StackPane hueco = (StackPane) n;
            String[] pos = ((String) hueco.getUserData()).split(",");
            boolean ocupado = franja(DIAS[Integer.parseInt(pos[0]) - 1], Integer.parseInt(pos[1])) != null;
            hueco.setStyle(ocupado ? ESTILO_OCUPADO : ESTILO_LIBRE);
            hueco.getChildren().clear();
            if (ocupado) {
                Label texto = new Label(clase.getString("nombre"));
                texto.setStyle("-fx-text-fill: white;");
                hueco.getChildren().add(texto);
            }
        }
    }

    /** Botón «Borrar» del diálogo de una franja ocupada. */
    private static final ButtonType BORRAR = new ButtonType("Borrar", ButtonBar.ButtonData.LEFT);
    /** Botón «Guardar» de los diálogos de franja. */
    private static final ButtonType GUARDAR = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);

    /**
     * Clic en un hueco: si está libre, crea una sesión en él; si está ocupado, abre
     * la sesión de esa franja para editarla o borrarla.
     *
     * @param hueco celda pulsada; su {@code userData} es {@code "columna,hora"}
     */
    private void onHueco(StackPane hueco) {
        String[] pos = ((String) hueco.getUserData()).split(",");
        int col = Integer.parseInt(pos[0]);
        int hora = Integer.parseInt(pos[1]);
        String dia = DIAS[col - 1];
        String texto = dia + " " + String.format("%02d:00", hora);

        Document existente = franja(dia, hora);
        try {
            if (existente != null) {
                editarEn(DayOfWeek.of(col), hora, existente, texto);
            } else {
                crearEn(DayOfWeek.of(col), hora, dia, texto);
            }
        } catch (DatosException e) {
            Registro.error("Error en " + getClass().getSimpleName(), e);
            alerta(Alert.AlertType.ERROR, "No se pudo guardar el horario en MongoDB.");
        }
        pintar();
    }

    /**
     * Pide monitor y aforo y, si son válidos, crea la sesión de la próxima fecha con ese
     * día y hora y añade la franja al horario de la clase.
     *
     * @param diaSemana día de la semana del hueco
     * @param hora hora de inicio
     * @param dia nombre del día tal como se guarda
     * @param texto día y hora para los mensajes
     */
    private void crearEn(DayOfWeek diaSemana, int hora, String dia, String texto) {
        int maxSala = aforoSala();
        ChoiceBox<Document> fMonitor = desplegableMonitores();
        TextField fAforo = new TextField(maxSala == Integer.MAX_VALUE ? "" : String.valueOf(maxSala));

        if (dialogoFranja("Nueva sesión: " + texto, fMonitor, fAforo, false) != GUARDAR) {
            return;
        }
        Integer aforo = validar(fMonitor, fAforo, maxSala, 0);
        if (aforo == null) {
            return;
        }

        insertarProxima(diaSemana, hora, aforo, fMonitor.getValue().getObjectId("_id"));
        horas().add(new Document("dia", dia)
                .append("horainicio", String.format("%02d:00", hora))
                .append("horafin", String.format("%02d:00", hora + 1)));
        dao.actualizarHorasClase(clase.getObjectId("_id"), horas());
    }

    /**
     * Abre la sesión de una franja ocupada con su monitor y su aforo.
     * «Guardar» aplica los cambios a todas las sesiones futuras de la franja (o crea la
     * próxima si no queda ninguna); «Borrar» quita la franja del horario y borra sus
     * sesiones futuras. Las sesiones ya pasadas no se tocan: son el historial de la clase.
     *
     * @param diaSemana día de la semana del hueco
     * @param hora hora de inicio
     * @param existente franja del horario que corresponde al hueco
     * @param texto día y hora para los mensajes
     */
    private void editarEn(DayOfWeek diaSemana, int hora, Document existente, String texto) {
        int maxSala = aforoSala();
        List<Document> futuras = sesionesFuturas(diaSemana, hora);
        Document proxima = futuras.isEmpty() ? null : futuras.get(0);

        ChoiceBox<Document> fMonitor = desplegableMonitores();
        TextField fAforo = new TextField();
        if (proxima != null) {
            fMonitor.getSelectionModel().select(buscarPorId(fMonitor.getItems(), proxima.getObjectId("monitor")));
            fAforo.setText(String.valueOf(proxima.getInteger("plazaMax", 0)));
        } else if (maxSala != Integer.MAX_VALUE) {
            fAforo.setText(String.valueOf(maxSala));
        }

        ButtonType respuesta = dialogoFranja("Sesión: " + texto, fMonitor, fAforo, true);

        if (respuesta == BORRAR) {
            if (confirmar("¿Quitar " + texto + " del horario y borrar sus " + futuras.size() + " sesiones futuras?")) {
                for (Document s : futuras) {
                    dao.eliminarSesion(s.getObjectId("_id"));
                }
                horas().remove(existente);
                dao.actualizarHorasClase(clase.getObjectId("_id"), horas());
            }
        } else if (respuesta == GUARDAR) {
            int apuntados = 0;
            for (Document s : futuras) {
                List<Document> asist = s.getList("asistentes", Document.class);
                apuntados = Math.max(apuntados, asist == null ? 0 : asist.size());
            }
            Integer aforo = validar(fMonitor, fAforo, maxSala, apuntados);
            if (aforo == null) {
                return;
            }
            ObjectId monitorId = fMonitor.getValue().getObjectId("_id");
            if (futuras.isEmpty()) {
                insertarProxima(diaSemana, hora, aforo, monitorId);
            }
            for (Document s : futuras) {
                dao.actualizarSesion(s.getObjectId("_id"), s.getDate("fecha"), aforo, monitorId);
            }
        }
    }

    /**
     * Sesiones de la clase que aún no se han celebrado y caen en un día y hora dados.
     *
     * @param diaSemana día de la semana de la franja
     * @param hora hora de inicio de la franja
     * @return sesiones futuras de la franja, de la más próxima a la más lejana
     */
    private List<Document> sesionesFuturas(DayOfWeek diaSemana, int hora) {
        LocalDateTime ahora = LocalDateTime.now();
        List<Document> resultado = new ArrayList<Document>();
        for (Document s : dao.listarSesiones(clase.getObjectId("_id"))) {
            Date d = s.getDate("fecha");
            if (d == null) {
                continue;
            }
            LocalDateTime f = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            if (f.isAfter(ahora) && f.getDayOfWeek() == diaSemana && f.getHour() == hora) {
                resultado.add(s);
            }
        }
        return resultado;
    }

    /**
     * Inserta la sesión de la próxima fecha que cae en un día y hora, sin nadie apuntado.
     *
     * @param diaSemana día de la semana
     * @param hora hora de inicio
     * @param aforo plazas máximas
     * @param monitorId monitor que la imparte
     */
    private void insertarProxima(DayOfWeek diaSemana, int hora, int aforo, ObjectId monitorId) {
        LocalDateTime fecha = proximaFecha(LocalDateTime.now(), diaSemana, hora);
        dao.insertarSesion(new Document("claseid", clase.getObjectId("_id"))
                .append("fecha", Date.from(fecha.atZone(ZoneId.systemDefault()).toInstant()))
                .append("plazaMax", aforo)
                .append("monitor", monitorId)
                .append("asistentes", new ArrayList<Document>()));
    }

    /**
     * Crea el desplegable con todos los monitores, mostrados por nombre y apellido.
     *
     * @return desplegable sin ningún monitor elegido
     */
    private ChoiceBox<Document> desplegableMonitores() {
        ChoiceBox<Document> fMonitor = new ChoiceBox<Document>();
        fMonitor.getItems().setAll(dao.listarMonitores());
        fMonitor.setConverter(new StringConverter<Document>() {
            @Override
            public String toString(Document d) {
                return d == null ? "" : d.getString("nombre") + " " + d.getString("apellido");
            }

            @Override
            public Document fromString(String s) {
                return null;
            }
        });
        return fMonitor;
    }

    /**
     * Muestra el diálogo de una franja con el monitor y el aforo, y espera la respuesta.
     *
     * @param titulo título de la ventana
     * @param fMonitor desplegable de monitores
     * @param fAforo campo del aforo
     * @param conBorrar {@code true} para añadir el botón «Borrar» (franja ocupada)
     * @return {@link #GUARDAR}, {@link #BORRAR} o {@link ButtonType#CANCEL}
     */
    private ButtonType dialogoFranja(String titulo, ChoiceBox<Document> fMonitor, TextField fAforo, boolean conBorrar) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.addRow(0, new Label("Monitor:"), fMonitor);
        grid.addRow(1, new Label("Aforo:"), fAforo);

        Dialog<ButtonType> dialogo = new Dialog<ButtonType>();
        dialogo.setTitle(titulo);
        dialogo.getDialogPane().setContent(grid);
        dialogo.getDialogPane().getButtonTypes().add(GUARDAR);
        if (conBorrar) {
            dialogo.getDialogPane().getButtonTypes().add(BORRAR);
        }
        dialogo.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        return dialogo.showAndWait().orElse(ButtonType.CANCEL);
    }

    /**
     * Comprueba el monitor y el aforo del diálogo. Si algo no es válido, avisa al usuario.
     *
     * @param fMonitor desplegable de monitores
     * @param fAforo campo del aforo
     * @param maxSala aforo máximo de la sala
     * @param apuntados clientes ya apuntados (el aforo no puede ser menor)
     * @return el aforo, o {@code null} si algo no es válido
     */
    private Integer validar(ChoiceBox<Document> fMonitor, TextField fAforo, int maxSala, int apuntados) {
        if (fMonitor.getValue() == null) {
            alerta(Alert.AlertType.WARNING, "Elige un monitor.");
            return null;
        }
        int aforo;
        try {
            aforo = Integer.parseInt(fAforo.getText().trim());
        } catch (NumberFormatException e) {
            alerta(Alert.AlertType.WARNING, "El aforo debe ser un número.");
            return null;
        }
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
     * Aforo máximo de la sala de la clase.
     *
     * @return aforo de la sala, o {@link Integer#MAX_VALUE} si no se encuentra
     */
    private int aforoSala() {
        ObjectId salaId = clase.getObjectId("salaid");
        for (Document s : dao.listarSalas()) {
            if (s.getObjectId("_id").equals(salaId)) {
                return s.getInteger("aforoMaximo", Integer.MAX_VALUE);
            }
        }
        return Integer.MAX_VALUE;
    }

    /**
     * Calcula la fecha de la primera sesión de una franja nueva del horario.
     *
     * @param ahora momento actual (se pasa como parámetro para poder probarlo)
     * @param dia día de la semana de la franja
     * @param hora hora de inicio de la franja (9 a 22)
     * @return fecha y hora de la próxima sesión, siempre posterior a {@code ahora}
     */
    static LocalDateTime proximaFecha(LocalDateTime ahora, DayOfWeek dia, int hora) {
        // Si hoy es ese día pero la hora ya ha pasado, va a la semana siguiente.
        LocalDateTime f = ahora.with(TemporalAdjusters.nextOrSame(dia)).truncatedTo(ChronoUnit.HOURS).withHour(hora);
        return f.isAfter(ahora) ? f : f.plusWeeks(1);
    }

    // ---------- Navegación ----------

    /** Vuelve al panel de gestión de clases en la misma ventana (se activa al pulsar el título). */
    @FXML
    private void onVolver() {
        try {
            Scene escena = raiz.getScene();
            escena.setRoot(FXMLLoader.load(getClass().getResource("/com/mycompany/vista/GestorClase.fxml")));
            ((Stage) escena.getWindow()).setTitle("Tolotolo - Gestión de clases");
        } catch (IOException | NullPointerException e) {
            Registro.error("Error en " + getClass().getSimpleName(), e);
            alerta(Alert.AlertType.ERROR, "No se pudo volver al panel de clases.");
        }
    }

    /** Cierra la sesión del gerente: abre la ventana de login y cierra esta. */
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
