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
import javafx.scene.control.ChoiceBox;
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
 * Controlador de la pantalla de gestión de clientes.
 * <p>
 * Permite listar, editar, dar de baja y registrar fotos para los clientes del
 * gimnasio desde una ficha de administración.
 */
public class GestionClientesController {

    // Controles vinculados por fx:id con la ficha y la lista de clientes.
    /** Contenedor raíz de la pantalla. */
    @FXML
    private BorderPane raiz;
    /** Lista de clientes (documentos de MongoDB) que se muestra a la izquierda. */
    @FXML
    private ListView<Document> listClientes;
    /** Etiqueta con el nombre y los apellidos del cliente seleccionado. */
    @FXML
    private Label lblNombreCompleto;
    /** Foto del cliente seleccionado. */
    @FXML
    private ImageView imgFoto;
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
    /** Selector de membresía (normal o premium). */
    @FXML
    private ChoiceBox<String> choMembresia;
    /** Botón que guarda los cambios de la ficha; está desactivado mientras no haya un cliente seleccionado. */
    @FXML
    private Button btnGuardar;
    /** Botón para dar de baja al cliente seleccionado; está desactivado mientras no haya selección. */
    @FXML
    private Button btnBaja;
    /** Recuadro que contiene la foto; la imagen se ajusta a su tamaño. */
    @FXML
    private StackPane panelFoto;
    /** Título de la pantalla. */
    @FXML
    private Label lblTitulo;
    /** Botón para cerrar la sesión. */
    @FXML
    private Button btnCerrarSesion;
    /** Botón para dar de alta un cliente nuevo. */
    @FXML
    private Button btnAlta;

    /** Acceso a la base de datos. */
    private final daoImplementacio dao = new daoImplementacio();
    // Formato utilizado para mostrar las fechas BSON en la ficha.
    /** Formato con el que se muestran las fechas en la ficha. */
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Define cómo se presenta cada cliente en la lista de la izquierda.
    /** Celda de la lista: muestra el nombre y los apellidos en una línea, y el DNI y la membresía en otra. */
    private static class CeldaCliente extends ListCell<Document> {

        /**
         * Rellena la celda con los datos del cliente, o la deja vacía.
         *
         * @param c documento del cliente
         * @param vacio {@code true} si la celda no tiene contenido
         */
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

    /** Prepara la pantalla: rellena el selector de membresía, define cómo se dibuja cada cliente en la lista, enlaza la selección con la ficha, ajusta la foto al recuadro y carga los clientes. */
    @FXML
    private void initialize() {
        // Configura las opciones y la presentación antes de cargar los registros.
        choMembresia.getItems().addAll("normal", "premium");

        listClientes.setCellFactory(new Callback<ListView<Document>, ListCell<Document>>() {
            @Override
            public ListCell<Document> call(ListView<Document> lista) {
                return new CeldaCliente();
            }
        });

        // Al cambiar la selección, copia los datos del documento a los campos de la ficha.
        listClientes.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Document>() {
            @Override
            public void changed(ObservableValue<? extends Document> obs, Document anterior, Document elegido) {
                mostrarCliente(elegido);
            }
        });

        // La foto se ajusta al tamaño del recuadro.
        imgFoto.fitWidthProperty().bind(panelFoto.widthProperty().subtract(4));
        imgFoto.fitHeightProperty().bind(panelFoto.heightProperty().subtract(4));

        Registro.info("Pantalla abierta: gestión de clientes");
        cargarClientes();
    }

    /** Lee los clientes de MongoDB y los muestra en la lista. Si no hay conexión, muestra una alerta. */
    private void cargarClientes() {
        try {
            // Obtiene el contenido de MongoDB y lo muestra en el ListView.
            List<Document> clientes = dao.listarClientes();
            Registro.info("Clientes cargados: " + clientes.size());
            listClientes.getItems().setAll(clientes);
        } catch (DatosException e) {
            Registro.error("No se pudieron cargar los clientes", e);
            alerta(Alert.AlertType.ERROR, "No se puede conectar con MongoDB.");
        }
    }

    /**
     * Rellena la ficha con los datos del cliente indicado. Si es {@code null}, limpia la ficha y desactiva los botones de edición.
     *
     * @param c documento del cliente seleccionado, o {@code null} si no hay selección
     */
    private void mostrarCliente(Document c) {
        if (c == null) {
            // Sin selección se limpia la ficha y se desactivan las acciones de edición.
            lblNombreCompleto.setText("Nombre y apellidos cliente");
            txtNombre.clear();
            txtApellidos.clear();
            txtDni.clear();
            txtMail.clear();
            txtAlta.clear();
            choMembresia.setValue(null);
            mostrarFoto(null);
            btnGuardar.setDisable(true);
            btnBaja.setDisable(true);
            return;
        }
        // Rellena la ficha desde el documento seleccionado.
        lblNombreCompleto.setText(c.getString("nombre") + " " + c.getString("apellido"));
        txtNombre.setText(c.getString("nombre"));
        txtApellidos.setText(c.getString("apellido"));
        txtDni.setText(c.getString("dni"));
        txtMail.setText(c.getString("mail"));
        choMembresia.setValue(c.getString("membresia"));

        // MongoDB almacena fechaAlta como BSON Date; se convierte a fecha local para mostrarla.
        Date alta = c.getDate("fechaAlta");
        if (alta == null) {
            txtAlta.setText("");
        } else {
            txtAlta.setText(alta.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().format(FECHA));
        }

        mostrarFoto(c);
        btnGuardar.setDisable(false);
        btnBaja.setDisable(false);
    }

    /**
     * Muestra la foto del cliente, que se busca por su DNI, o la quita si no hay cliente.
     *
     * @param c documento del cliente, o {@code null}
     */
    private void mostrarFoto(Document c) {
        if (c == null) {
            imgFoto.setImage(null);
            return;
        }
        // La foto se busca en la carpeta de imágenes por el DNI del cliente.
        imgFoto.setImage(GestorImagenes.cargar(c.getString("dni")));
    }

    /**
     * Valida y guarda los cambios de la ficha del cliente seleccionado.
     *
     * <p>Si cambia el DNI, la foto se renombra para seguirlo. Avisa si falta algún campo, si el correo no es válido, si el DNI o el correo ya existen, o si falla la conexión o el renombrado de la foto.</p>
     */
    @FXML
    private void onGuardarDatos() {
        // No se puede editar si no hay un cliente seleccionado.
        Document c = listClientes.getSelectionModel().getSelectedItem();
        if (c == null) {
            return;
        }

        String nombre = txtNombre.getText().trim();
        String apellido = txtApellidos.getText().trim();
        String dni = txtDni.getText().trim();
        String mail = txtMail.getText().trim();
        String membresia = choMembresia.getValue();

        // Valida los campos antes de enviar cambios a la base de datos.
        if (nombre.isEmpty() || apellido.isEmpty() || dni.isEmpty() || mail.isEmpty() || membresia == null) {
            alerta(Alert.AlertType.WARNING, "Rellena todos los campos.");
            return;
        }
        if (!mail.matches("^.+@.+\\..+$")) {
            alerta(Alert.AlertType.WARNING, "El mail no es válido.");
            return;
        }

        // El DNI antiguo se guarda antes de modificar el documento en memoria.
        String dniOriginal = c.getString("dni");

        try {
            dao.actualizarCliente(c.getObjectId("_id"), nombre, apellido, dni, mail, membresia);
            GestorImagenes.renombrar(dniOriginal, dni);   // la foto sigue al nuevo DNI

            // Sin volver a consultar MongoDB, actualiza el documento visible en la lista.
            c.put("nombre", nombre);
            c.put("apellido", apellido);
            c.put("dni", dni);
            c.put("mail", mail);
            c.put("membresia", membresia);
            listClientes.refresh();
            lblNombreCompleto.setText(nombre + " " + apellido);

            Registro.info("Cliente modificado: " + dniOriginal
                    + (dniOriginal.equals(dni) ? "" : " (nuevo DNI: " + dni + ")"));
            alerta(Alert.AlertType.INFORMATION, "Datos guardados.");
        } catch (DuplicadoException e) {
            Registro.aviso("Modificación rechazada, DNI o mail duplicado: " + dni + " / " + mail);
            alerta(Alert.AlertType.ERROR, "Ya existe otro cliente con ese DNI o ese mail.");
        } catch (ConexionException e) {
            Registro.error("No se pudo conectar con MongoDB al guardar al cliente " + dniOriginal, e);
            alerta(Alert.AlertType.ERROR, "No se puede conectar con MongoDB.");
        } catch (DatosException e) {
            Registro.error("No se pudieron guardar los datos del cliente " + dniOriginal, e);
            alerta(Alert.AlertType.ERROR, "No se pudieron guardar los datos.");
        } catch (IOException e) {
            Registro.error("Datos guardados, pero no se pudo renombrar la foto de " + dniOriginal, e);
            alerta(Alert.AlertType.ERROR, "Datos guardados, pero no se pudo renombrar la foto.");
        }
    }

    /**
     * Abre un diálogo para introducir los datos de un cliente nuevo.
     *
     * <p>Si los datos son válidos lo inserta con la fecha de alta actual y sin logros ni clases, y lo deja seleccionado en la lista.</p>
     */
    @FXML
    private void onDarDeAlta() {
        // El formulario de alta se construye como diálogo para recoger todos los datos requeridos.
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
        App.ponerIcono(dialogo);
        dialogo.setTitle("Dar de alta cliente");
        dialogo.getDialogPane().setContent(grid);
        dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Si se cancela el diálogo no se modifica la base de datos.
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

        // Los campos opcionales de la aplicación empiezan como listas vacías.
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
            // Inserta el registro recién creado en la lista y lo selecciona para mostrar su ficha.
            listClientes.getItems().add(nuevo);
            listClientes.getSelectionModel().select(nuevo);
            listClientes.scrollTo(nuevo);
            Registro.info("Cliente dado de alta: " + dni + " (" + fMembresia.getValue() + ")");
        } catch (DuplicadoException e) {
            Registro.aviso("Alta rechazada, DNI o mail duplicado: " + dni + " / " + mail);
            alerta(Alert.AlertType.ERROR, "Ya existe un cliente con ese DNI o ese mail.");
        } catch (ConexionException e) {
            Registro.error("No se pudo conectar con MongoDB al dar de alta a " + dni, e);
            alerta(Alert.AlertType.ERROR, "No se puede conectar con MongoDB.");
        } catch (DatosException e) {
            Registro.error("No se pudo guardar el cliente " + dni, e);
            alerta(Alert.AlertType.ERROR, "No se pudo guardar el cliente.");
        }
    }

    /** Pide confirmación y elimina al cliente seleccionado y su foto; después recarga la lista. */
    @FXML
    private void onDarDeBaja() {
        Document c = listClientes.getSelectionModel().getSelectedItem();
        if (c == null) {
            return;
        }

        Alert confirmar = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Seguro que quieres dar de baja a " + c.getString("nombre") + " "
                + c.getString("apellido") + "?", ButtonType.YES, ButtonType.NO);
        App.ponerIcono(confirmar);
        confirmar.setHeaderText(null);

        ButtonType respuesta = confirmar.showAndWait().orElse(ButtonType.NO);
        if (respuesta == ButtonType.YES) {
            String dni = c.getString("dni");
            try {
                // Primero se borra de MongoDB (por _id) y después su foto (por DNI).
                dao.eliminarCliente(c.getObjectId("_id"));
                Registro.info("Cliente dado de baja: " + dni);
                GestorImagenes.borrar(dni);
                cargarClientes();
                mostrarCliente(null);
            } catch (DatosException e) {
                Registro.error("No se pudo dar de baja al cliente " + dni, e);
                alerta(Alert.AlertType.ERROR, "No se pudo dar de baja al cliente.");
            } catch (IOException e) {
                Registro.error("Cliente " + dni + " dado de baja, pero no se pudo borrar su foto", e);
                alerta(Alert.AlertType.ERROR, "Cliente dado de baja, pero no se pudo borrar su foto.");
            }
        }
    }

    /** Permite elegir una imagen (png, jpg o jpeg) y la guarda como foto del cliente seleccionado. */
    @FXML
    private void onElegirFoto() {
        Document c = listClientes.getSelectionModel().getSelectedItem();
        if (c == null) {
            alerta(Alert.AlertType.WARNING, "Elige primero un cliente de la lista.");
            return;
        }

        FileChooser selector = new FileChooser();
        selector.setTitle("Elegir foto");
        selector.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg"));
        File archivo = selector.showOpenDialog(raiz.getScene().getWindow());
        if (archivo == null) {
            return;
        }

        try {
            GestorImagenes.guardar(c.getString("dni"), archivo);
            Registro.info("Foto actualizada del cliente " + c.getString("dni"));
            mostrarFoto(c);
        } catch (IOException e) {
            Registro.error("No se pudo guardar la foto del cliente " + c.getString("dni"), e);
            alerta(Alert.AlertType.ERROR, "No se pudo guardar la imagen.");
        }
    }

    /** Abre la pantalla de login en una ventana nueva y cierra la actual. */
    @FXML
    private void onCerrarSesion() {
        try {
            // Abre el login antes de cerrar la ventana de administración.
            Parent root = FXMLLoader.load(getClass().getResource("/com/mycompany/vista/Login.fxml"));
            Stage login = new Stage();
            App.ponerIcono(login);
            login.setTitle("Tolotolo - Iniciar sesión");
            login.setScene(new Scene(root));
            login.show();
            Registro.info("Sesión cerrada desde la gestión de clientes");

            ((Stage) raiz.getScene().getWindow()).close();
        } catch (IOException | NullPointerException e) {
            Registro.error("No se pudo volver al login desde la gestión de clientes", e);
            alerta(Alert.AlertType.ERROR, "No se pudo volver al login.");
        }
    }

    /** Vuelve al panel del gerente o del monitor dentro de la misma ventana según el rol en sesión. */
    @FXML
    private void onVolver() {
        try {
            boolean esMonitor = "monitor".equalsIgnoreCase(LoginController.rolActual);
            String fxml = esMonitor ? "/com/mycompany/vista/Monitor.fxml" : "/com/mycompany/vista/Gerente.fxml";
            String titulo = esMonitor ? "Tolotolo - Monitor" : "Tolotolo - Gerente";

            URL ruta = getClass().getResource(fxml);
            if (ruta == null) {
                Registro.aviso("No se encuentra el FXML " + fxml);
                alerta(Alert.AlertType.ERROR, "No se encuentra el FXML " + fxml);
                return;
            }

            // Conserva la misma ventana y cambia únicamente la vista que contiene.
            Scene escena = raiz.getScene();
            Stage ventana = (Stage) escena.getWindow();

            Parent root = FXMLLoader.load(ruta);
            escena.setRoot(root);
            ventana.setTitle(titulo);
            Registro.info("Vuelta al panel principal desde la gestión de clientes");
        } catch (Exception e) {
            Registro.error("No se pudo volver al panel principal", e);
            alerta(Alert.AlertType.ERROR, "No se pudo volver: " + e.getMessage());
        }
    }

    /**
     * Muestra una alerta con el icono de la aplicación y espera a que se cierre.
     *
     * @param tipo tipo de alerta
     * @param mensaje texto que se muestra
     */
    private void alerta(Alert.AlertType tipo, String mensaje) {
        // Un único método mantiene consistentes los avisos de esta pantalla.
        Alert a = new Alert(tipo, mensaje);
        App.ponerIcono(a);
        a.setHeaderText(null);
        a.showAndWait();
    }
}