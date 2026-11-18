package com.mycompany.controlador;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * Visor del log de operaciones contra la base de datos, solo para el gerente.
 * <p>
 * Lee {@code datos/logs/operaciones.0.log} con {@link Registro#leerOperaciones()}, parte cada
 * línea ({@code fecha | usuario | tipo | detalle | resultado}) en las columnas de la tabla,
 * muestra primero las más recientes y permite filtrarlas por texto. Las operaciones fallidas
 * se marcan en rojo.
 */
public class VisorLogController {

    /** Número de columnas de cada línea del log. */
    private static final int COLUMNAS = 5;

    /** Campo de texto para filtrar las líneas. */
    @FXML private TextField txtFiltro;
    /** Botón que vuelve a leer el archivo de log. */
    @FXML private Button btnRecargar;
    /** Botón que cierra el visor. */
    @FXML private Button btnCerrar;
    /** Tabla con una fila por operación. */
    @FXML private TableView<String[]> tablaLog;
    /** Columna de la fecha y hora. */
    @FXML private TableColumn<String[], String> colFecha;
    /** Columna del usuario que hizo la operación. */
    @FXML private TableColumn<String[], String> colUsuario;
    /** Columna del tipo de operación (SELECT, INSERT, UPDATE, DELETE y colección). */
    @FXML private TableColumn<String[], String> colTipo;
    /** Columna con el detalle de la operación. */
    @FXML private TableColumn<String[], String> colDetalle;
    /** Columna con el resultado (OK o el error). */
    @FXML private TableColumn<String[], String> colResultado;
    /** Etiqueta con el número de operaciones mostradas. */
    @FXML private Label lblTotal;

    /** Todas las líneas leídas del archivo, ya partidas en columnas y de la más reciente a la más antigua. */
    private final List<String[]> filas = new ArrayList<String[]>();

    /**
     * Enlaza cada columna con su posición en la línea, colorea los errores, filtra al escribir
     * y carga el log por primera vez.
     */
    @FXML
    private void initialize() {
        enlazar(colFecha, 0);
        enlazar(colUsuario, 1);
        enlazar(colTipo, 2);
        enlazar(colDetalle, 3);
        enlazar(colResultado, 4);
        colResultado.setCellFactory(new Callback<TableColumn<String[], String>, TableCell<String[], String>>() {
            @Override
            public TableCell<String[], String> call(TableColumn<String[], String> columna) {
                return new TableCell<String[], String>() {
                    @Override
                    protected void updateItem(String valor, boolean vacia) {
                        super.updateItem(valor, vacia);
                        setText(vacia ? null : valor);
                        setStyle(!vacia && valor != null && valor.startsWith("ERROR") ? "-fx-text-fill: #c62828;" : "");
                    }
                };
            }
        });
        txtFiltro.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> obs, String antes, String ahora) {
                filtrar();
            }
        });
        onRecargar();
    }

    /**
     * Hace que la columna muestre el campo {@code indice} de cada fila.
     *
     * @param columna columna de la tabla
     * @param indice  posición del campo en la línea del log
     */
    private void enlazar(TableColumn<String[], String> columna, final int indice) {
        columna.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<String[], String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<String[], String> datos) {
                return new ReadOnlyStringWrapper(datos.getValue()[indice]);
            }
        });
    }

    /**
     * Vuelve a leer el archivo de log y aplica el filtro escrito.
     */
    @FXML
    private void onRecargar() {
        filas.clear();
        try {
            for (String linea : Registro.leerOperaciones()) {
                if (!linea.isBlank()) {
                    filas.add(partir(linea));
                }
            }
            Collections.reverse(filas);
        } catch (IOException e) {
            Registro.error("No se pudo leer el log de operaciones", e);
            Alert a = new Alert(Alert.AlertType.ERROR, "No se pudo leer el log de operaciones.");
            a.setHeaderText(null);
            a.showAndWait();
        }
        filtrar();
    }

    /**
     * Parte una línea del log en sus cinco campos. El detalle puede contener {@code |}, así que
     * se toman la fecha, el usuario y el tipo por delante, el resultado por detrás y lo que queda
     * en medio es el detalle.
     *
     * @param linea línea del log
     * @return los cinco campos; los que falten quedan vacíos
     */
    static String[] partir(String linea) {
        String[] campos = {"", "", "", "", ""};
        String[] trozos = linea.split(" \\| ");
        if (trozos.length < COLUMNAS) {
            for (int i = 0; i < trozos.length; i++) {
                campos[i] = trozos[i].trim();
            }
            return campos;
        }
        campos[0] = trozos[0].trim();
        campos[1] = trozos[1].trim();
        campos[2] = trozos[2].trim();
        campos[4] = trozos[trozos.length - 1].trim();
        StringBuilder detalle = new StringBuilder();
        for (int i = 3; i < trozos.length - 1; i++) {
            if (detalle.length() > 0) {
                detalle.append(" | ");
            }
            detalle.append(trozos[i]);
        }
        campos[3] = detalle.toString().trim();
        return campos;
    }

    /**
     * Muestra en la tabla solo las filas que contienen el texto del filtro.
     */
    private void filtrar() {
        String texto = txtFiltro.getText() == null ? "" : txtFiltro.getText().trim().toLowerCase();
        List<String[]> visibles = new ArrayList<String[]>();
        for (String[] fila : filas) {
            if (texto.isEmpty() || String.join(" ", fila).toLowerCase().contains(texto)) {
                visibles.add(fila);
            }
        }
        tablaLog.getItems().setAll(visibles);
        lblTotal.setText(visibles.size() + " de " + filas.size() + " operaciones");
    }

    /**
     * Cierra la ventana del visor.
     */
    @FXML
    private void onCerrar() {
        ((Stage) btnCerrar.getScene().getWindow()).close();
    }
}
