package com.mycompany.controlador;

import com.mycompany.controlador.dao.DatosException;
import com.mycompany.controlador.dao.DuplicadoException;
import com.mycompany.controlador.dao.ConexionException;
import com.mycompany.controlador.dao.daoImplementacio;
import java.io.File;
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
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.bson.Document;

/**
 * Controlador de la pantalla de gestión de monitores.
 * <p>
 * Centraliza la consulta, edición y baja de monitores, además del manejo de sus
 * datos personales y fotografías.
 */
public class GestionMonitorController {

    /** Crea el controlador; lo instancia JavaFX al cargar el FXML. */
    public GestionMonitorController() {
    }

    // Controles enlazados con la lista de monitores y su ficha de datos.
    /** Contenedor raíz de la pantalla. */
    @FXML
    private BorderPane raiz;
    /** Lista de monitores (documentos de MongoDB) que se muestra a la izquierda. */
    @FXML
    private ListView<Document> listMonitores;
    /** Etiqueta con el nombre y los apellidos del monitor seleccionado. */
    @FXML
    private Label lblNombreCompleto;
    /** Foto del monitor seleccionado. */
    @FXML
    private ImageView imgFoto;
    /** Recuadro que contiene la foto; la imagen se ajusta a su tamaño. */
    @FXML
    private StackPane panelFoto;
    /** Campo del nombre. */
    @FXML
    private TextField txtNombre;
    /** Campo de los apellidos. */
    @FXML
    private TextField txtApellidos;
    /** Campo del DNI. */
    @FXML
    private TextField txtDni;
    /** Campo del correo electrónico. */
    @FXML
    private TextField txtMail;
    /** Fecha de alta, con formato dd/MM/yyyy. */
    @FXML
    private TextField txtAlta;
    /** Dirección del monitor, con el formato {@code Calle, CP, Ciudad}. */
    @FXML
    private TextField txtDireccion;
    /** Botón que guarda los cambios de la ficha; está desactivado mientras no haya un monitor seleccionado. */
    @FXML
    private Button btnGuardar;
    /** Botón para dar de baja al monitor seleccionado; está desactivado mientras no haya selección. */
    @FXML
    private Button btnBaja;

    /** Acceso a la base de datos. */
    private final daoImplementacio dao = new daoImplementacio();
    // Formato de presentación de la fecha de alta.
    /** Formato con el que se muestra la fecha de alta. */
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Define cómo se presenta cada monitor en la lista de la izquierda.
    /** Celda de la lista: muestra el nombre y los apellidos en una línea, y el DNI y el correo en otra. */
    private static class CeldaMonitor extends ListCell<Document> {

        /** Crea una celda vacía de la lista de monitores. */
        CeldaMonitor() {
        }

        /**
         * Rellena la celda con los datos del monitor, o la deja vacía.
         *
         * @param m documento del monitor
         * @param vacio {@code true} si la celda no tiene contenido
         */
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

    /** Prepara la pantalla: define cómo se dibuja cada monitor en la lista, enlaza la selección con la ficha, ajusta la foto al recuadro y carga los monitores. */
    @FXML
    private void initialize() {
        // Configura las celdas y reacciona al cambio de monitor seleccionado.
        listMonitores.setCellFactory(new Callback<ListView<Document>, ListCell<Document>>() {
            @Override
            public ListCell<Document> call(ListView<Document> lista) {
                return new CeldaMonitor();
            }
        });

        // Copia a la ficha los campos del documento seleccionado.
        listMonitores.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Document>() {
            @Override
            public void changed(ObservableValue<? extends Document> obs, Document anterior, Document elegido) {
                mostrarMonitor(elegido);
            }
        });

        // La foto se ajusta al tamaño del recuadro.
        imgFoto.fitWidthProperty().bind(panelFoto.widthProperty().subtract(4));
        imgFoto.fitHeightProperty().bind(panelFoto.heightProperty().subtract(4));

        Registro.info("Pantalla abierta: gestión de monitores");
        cargarMonitores();
    }

    /** Lee los monitores de MongoDB y los muestra en la lista. Si no hay conexión, muestra una alerta. */
    private void cargarMonitores() {
        try {
            // Pide al DAO los trabajadores cuyo tipo es monitor y actualiza la lista.
            List<Document> monitores = dao.listarMonitores();
            Registro.info("Monitores cargados: " + monitores.size());
            listMonitores.getItems().setAll(monitores);
        } catch (DatosException e) {
            Registro.error("No se pudieron cargar los monitores", e);
            alerta(Alert.AlertType.ERROR, "No se puede conectar con MongoDB.");
        }
    }

    // Convierte el primer elemento del arreglo BSON de direcciones a texto para la ficha.
    /**
     * Convierte la primera dirección del monitor en un texto {@code Calle, CP, Ciudad}.
     *
     * @param m documento del monitor
     * @return la dirección como texto, o una cadena vacía si no tiene ninguna
     */
    private String direccionATexto(Document m) {
        List<Document> dirs = m.getList("direccion", Document.class);
        if (dirs == null || dirs.isEmpty()) {
            return "";
        }
        Document d = dirs.get(0);
        return d.getString("calle") + ", " + d.getString("cp") + ", " + d.getString("ciudad");
    }

    // Convierte el texto de la ficha al formato BSON; null señala un formato inválido.
    /**
     * Convierte el texto de la ficha en una dirección con el formato que guarda MongoDB.
     *
     * @param texto texto con el formato {@code Calle, CP, Ciudad}
     * @return lista con un documento de dirección, o {@code null} si el formato no es válido
     */
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

    /**
     * Rellena la ficha con los datos del monitor indicado. Si es {@code null}, limpia la ficha y desactiva los botones de edición.
     *
     * @param m documento del monitor seleccionado, o {@code null} si no hay selección
     */
    private void mostrarMonitor(Document m) {
        if (m == null) {
            // Sin selección se limpia la ficha y se desactivan las acciones de edición.
            lblNombreCompleto.setText("Nombre y apellidos monitor");
            txtNombre.clear();
            txtApellidos.clear();
            txtDni.clear();
            txtMail.clear();
            txtAlta.clear();
            txtDireccion.clear();
            mostrarFoto(null);
            btnGuardar.setDisable(true);
            btnBaja.setDisable(true);
            return;
        }
        // Completa la ficha con los datos del documento de MongoDB.
        lblNombreCompleto.setText(m.getString("nombre") + " " + m.getString("apellido"));
        txtNombre.setText(m.getString("nombre"));
        txtApellidos.setText(m.getString("apellido"));
        txtDni.setText(m.getString("dni"));
        txtMail.setText(m.getString("mail"));
        txtDireccion.setText(direccionATexto(m));

        // Convierte la fecha almacenada por MongoDB a texto en la zona horaria local.
        Date alta = m.getDate("fechaAlta");
        if (alta == null) {
            txtAlta.setText("");
        } else {
            txtAlta.setText(alta.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().format(FECHA));
        }

        mostrarFoto(m);
        btnGuardar.setDisable(false);
        btnBaja.setDisable(false);
    }

    /**
     * Muestra la foto del monitor, que se busca por su DNI, o la quita si no hay monitor.
     *
     * @param m documento del monitor, o {@code null}
     */
    private void mostrarFoto(Document m) {
        if (m == null) {
            imgFoto.setImage(null);
            return;
        }
        // La foto se busca en la carpeta de imágenes por el DNI del monitor.
        imgFoto.setImage(GestorImagenes.cargar(m.getString("dni")));
    }

    /**
     * Valida y guarda los cambios de la ficha del monitor seleccionado.
     *
     * <p>Si cambia el DNI, la foto se renombra para seguirlo. Avisa si falta algún campo, si el correo no es válido, si la dirección no tiene el formato {@code Calle, CP, Ciudad}, si el DNI o el correo ya existen, o si falla la conexión o el renombrado de la foto.</p>
     */
    @FXML
    private void onGuardarDatos() {
        // La edición solo es posible cuando se ha seleccionado un monitor.
        Document m = listMonitores.getSelectionModel().getSelectedItem();
        if (m == null) {
            return;
        }

        String nombre = txtNombre.getText().trim();
        String apellido = txtApellidos.getText().trim();
        String dni = txtDni.getText().trim();
        String mail = txtMail.getText().trim();

        // Comprueba datos obligatorios y el formato antes de guardar en MongoDB.
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

        // El DNI antiguo se guarda antes de modificar el documento en memoria.
        String dniOriginal = m.getString("dni");

        try {
            dao.actualizarMonitor(m.getObjectId("_id"), nombre, apellido, dni, mail, direccion);
            GestorImagenes.renombrar(dniOriginal, dni);   // la foto sigue al nuevo DNI

            // Actualiza también el documento de la lista para reflejar los cambios al instante.
            m.put("nombre", nombre);
            m.put("apellido", apellido);
            m.put("dni", dni);
            m.put("mail", mail);
            m.put("direccion", direccion);
            listMonitores.refresh();
            lblNombreCompleto.setText(nombre + " " + apellido);

            Registro.info("Monitor modificado: " + dniOriginal
                    + (dniOriginal.equals(dni) ? "" : " (nuevo DNI: " + dni + ")"));
            alerta(Alert.AlertType.INFORMATION, "Datos guardados.");
        } catch (DuplicadoException e) {
            Registro.aviso("Modificación rechazada, DNI o mail duplicado: " + dni + " / " + mail);
            alerta(Alert.AlertType.ERROR, "Ya existe otro trabajador con ese DNI o ese mail.");
        } catch (ConexionException e) {
            Registro.error("No se pudo conectar con MongoDB al guardar al monitor " + dniOriginal, e);
            alerta(Alert.AlertType.ERROR, "No se puede conectar con MongoDB.");
        } catch (DatosException e) {
            Registro.error("No se pudieron guardar los datos del monitor " + dniOriginal, e);
            alerta(Alert.AlertType.ERROR, "No se pudieron guardar los datos.");
        } catch (IOException e) {
            Registro.error("Datos guardados, pero no se pudo renombrar la foto de " + dniOriginal, e);
            alerta(Alert.AlertType.ERROR, "Datos guardados, pero no se pudo renombrar la foto.");
        }
    }

    /**
     * Abre un diálogo para introducir los datos y la dirección de un monitor nuevo.
     *
     * <p>Si los datos son válidos lo inserta como trabajador de tipo monitor, con la fecha de alta actual, y lo deja seleccionado en la lista.</p>
     */
    @FXML
    private void onDarDeAlta() {
        // Se crea un diálogo para introducir los datos personales y la dirección del monitor.
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
        App.ponerIcono(dialogo);
        dialogo.setTitle("Dar de alta monitor");
        dialogo.getDialogPane().setContent(grid);
        dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Cancelar el diálogo evita cualquier escritura en la base de datos.
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

        // MongoDB almacena la dirección dentro de un arreglo de documentos.
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
            // Selecciona el nuevo monitor para que sus datos aparezcan en la ficha.
            listMonitores.getItems().add(nuevo);
            listMonitores.getSelectionModel().select(nuevo);
            listMonitores.scrollTo(nuevo);
            Registro.info("Monitor dado de alta: " + dni);
        } catch (DuplicadoException e) {
            Registro.aviso("Alta rechazada, DNI o mail duplicado: " + dni + " / " + mail);
            alerta(Alert.AlertType.ERROR, "Ya existe un trabajador con ese DNI o ese mail.");
        } catch (ConexionException e) {
            Registro.error("No se pudo conectar con MongoDB al dar de alta a " + dni, e);
            alerta(Alert.AlertType.ERROR, "No se puede conectar con MongoDB.");
        } catch (DatosException e) {
            Registro.error("No se pudo guardar el monitor " + dni, e);
            alerta(Alert.AlertType.ERROR, "No se pudo guardar el monitor.");
        }
    }

    /** Pide confirmación y elimina al monitor seleccionado y su foto; después recarga la lista. */
    @FXML
    private void onDarDeBaja() {
        // La eliminación exige que haya un monitor seleccionado y confirmación del usuario.
        Document m = listMonitores.getSelectionModel().getSelectedItem();
        if (m == null) {
            return;
        }

        Alert confirmar = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Seguro que quieres dar de baja a " + m.getString("nombre") + " "
                + m.getString("apellido") + "?", ButtonType.YES, ButtonType.NO);
        App.ponerIcono(confirmar);
        confirmar.setHeaderText(null);

        ButtonType respuesta = confirmar.showAndWait().orElse(ButtonType.NO);
        if (respuesta == ButtonType.YES) {
            String dni = m.getString("dni");
            try {
                // Primero se borra de MongoDB (por _id) y después su foto (por DNI).
                dao.eliminarMonitor(m.getObjectId("_id"));
                Registro.info("Monitor dado de baja: " + dni);
                GestorImagenes.borrar(dni);
                // Recarga la lista desde MongoDB y limpia la ficha que mostraba al monitor eliminado.
                cargarMonitores();
                mostrarMonitor(null);
            } catch (DatosException e) {
                Registro.error("No se pudo dar de baja al monitor " + dni, e);
                alerta(Alert.AlertType.ERROR, "No se pudo dar de baja al monitor.");
            } catch (IOException e) {
                Registro.error("Monitor " + dni + " dado de baja, pero no se pudo borrar su foto", e);
                alerta(Alert.AlertType.ERROR, "Monitor dado de baja, pero no se pudo borrar su foto.");
            }
        }
    }

    /** Permite elegir una imagen (png, jpg o jpeg) y la guarda como foto del monitor seleccionado. */
    @FXML
    private void onElegirFoto() {
        Document m = listMonitores.getSelectionModel().getSelectedItem();
        if (m == null) {
            alerta(Alert.AlertType.WARNING, "Elige primero un monitor de la lista.");
            return;
        }

        FileChooser selector = new FileChooser();
        selector.setTitle("Elegir foto");
        selector.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg"));
        File archivo = selector.showOpenDialog(raiz.getScene().getWindow());
        if (archivo == null) {
            return;   // canceló
        }

        try {
            GestorImagenes.guardar(m.getString("dni"), archivo);
            Registro.info("Foto actualizada del monitor " + m.getString("dni"));
            mostrarFoto(m);
        } catch (IOException e) {
            Registro.error("No se pudo guardar la foto del monitor " + m.getString("dni"), e);
            alerta(Alert.AlertType.ERROR, "No se pudo guardar la imagen.");
        }
    }

    /** Vuelve al panel del gerente dentro de la misma ventana. */
    @FXML
    private void onVolver() {
        try {
            URL ruta = getClass().getResource("/com/mycompany/vista/Gerente.fxml");
            if (ruta == null) {
                Registro.aviso("No se encuentra el FXML del panel del gerente");
                alerta(Alert.AlertType.ERROR, "No se encuentra el FXML del panel del gerente.");
                return;
            }

            // Mantiene la ventana abierta y sustituye solo el contenido por el panel del gerente.
            Scene escena = raiz.getScene();
            Stage ventana = (Stage) escena.getWindow();

            Parent root = FXMLLoader.load(ruta);
            escena.setRoot(root);
            ventana.setTitle("Tolotolo - Gerente");
            Registro.info("Vuelta al panel del gerente desde la gestión de monitores");
        } catch (Exception e) {
            Registro.error("No se pudo volver al panel del gerente", e);
            alerta(Alert.AlertType.ERROR, "No se pudo volver: " + e.getMessage());
        }
    }

    /** Abre la pantalla de login en una ventana nueva y cierra la actual. */
    @FXML
    private void onCerrarSesion() {
        try {
            // Abre el formulario de acceso y después cierra la ventana actual.
            Parent root = FXMLLoader.load(getClass().getResource("/com/mycompany/vista/Login.fxml"));
            Stage login = new Stage();
            App.ponerIcono(login);
            login.setTitle("Tolotolo - Iniciar sesión");
            login.setScene(new Scene(root));
            login.show();
            Registro.info("Sesión cerrada desde la gestión de monitores");

            ((Stage) raiz.getScene().getWindow()).close();
        } catch (IOException | NullPointerException e) {
            Registro.error("No se pudo volver al login desde la gestión de monitores", e);
            alerta(Alert.AlertType.ERROR, "No se pudo volver al login.");
        }
    }

    /**
     * Muestra una alerta con el icono de la aplicación y espera a que se cierre.
     *
     * @param tipo tipo de alerta
     * @param mensaje texto que se muestra
     */
    private void alerta(Alert.AlertType tipo, String mensaje) {
        // Centraliza los avisos de error, validación y confirmación de esta pantalla.
        Alert a = new Alert(tipo, mensaje);
        App.ponerIcono(a);
        a.setHeaderText(null);
        a.showAndWait();
    }
}