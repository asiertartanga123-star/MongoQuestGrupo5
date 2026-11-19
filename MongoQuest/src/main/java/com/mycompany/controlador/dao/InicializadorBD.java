package com.mycompany.controlador.dao;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.ValidationAction;
import com.mongodb.client.model.ValidationLevel;
import com.mongodb.client.model.ValidationOptions;
import com.mycompany.controlador.Registro;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;

/**
 * Prepara una base de datos {@code Tolotolo} nueva la primera vez que se arranca la aplicación.
 * <p>
 * Si no hay ningún trabajador ni ningún cliente (nadie podría iniciar sesión), crea las
 * colecciones con sus validadores {@code $jsonSchema}, inserta los datos iniciales incluidos
 * dentro del JAR y crea los índices. Si la base ya tiene usuarios no toca nada, de modo que
 * nunca sobrescribe datos reales. Equivale a ejecutar los scripts de la carpeta
 * {@code Scripts_MongoDB_Tolotolo} (01 a 03), pero sin necesidad de instalar herramientas.
 */
final class InicializadorBD {

    /** Carpeta del classpath donde están los JSON iniciales y los validadores. */
    private static final String RUTA = "/com/mycompany/mongoquest/datos_iniciales/";
    /** Colecciones de la aplicación, en el orden en que se cargan. */
    private static final String[] COLECCIONES = {"Salas", "Clases", "Sesiones", "Clientes", "Trabajadores"};

    /** Clase de utilidad: no se instancia. */
    private InicializadorBD() {
    }

    /**
     * Carga los datos iniciales si la base de datos está vacía.
     *
     * @param bd base de datos {@code Tolotolo}
     * @throws com.mongodb.MongoException si el servidor no responde o rechaza una operación
     */
    static void preparar(MongoDatabase bd) {
        if (hayUsuarios(bd)) {
            return;
        }
        Registro.info("Base de datos sin usuarios: se cargan los datos iniciales");
        List<String> existentes = bd.listCollectionNames().into(new ArrayList<String>());
        for (String nombre : COLECCIONES) {
            try {
                if (!existentes.contains(nombre)) {
                    crearConValidador(bd, nombre);
                }
                MongoCollection<Document> coleccion = bd.getCollection(nombre);
                if (coleccion.countDocuments() == 0) {
                    List<Document> documentos = leerDocumentos(nombre);
                    if (!documentos.isEmpty()) {
                        coleccion.insertMany(documentos);
                    }
                }
            } catch (IOException e) {
                Registro.error("No se pudieron leer los datos iniciales de " + nombre, e);
            }
        }
        crearIndices(bd);
        Registro.info("Datos iniciales cargados");
    }

    /**
     * Comprueba si ya existe algún usuario (trabajador o cliente).
     *
     * @param bd base de datos
     * @return {@code true} si hay al menos uno
     */
    private static boolean hayUsuarios(MongoDatabase bd) {
        return bd.getCollection("Trabajadores").countDocuments() > 0
                || bd.getCollection("Clientes").countDocuments() > 0;
    }

    /**
     * Crea una colección con su validador {@code $jsonSchema} (nivel estricto, acción error).
     *
     * @param bd     base de datos
     * @param nombre nombre de la colección
     * @throws IOException si no se encuentra el fichero del validador
     */
    private static void crearConValidador(MongoDatabase bd, String nombre) throws IOException {
        BsonDocument validador = BsonDocument.parse(leerTexto("validador_" + nombre + ".json"));
        ValidationOptions opciones = new ValidationOptions()
                .validator(validador)
                .validationLevel(ValidationLevel.STRICT)
                .validationAction(ValidationAction.ERROR);
        bd.createCollection(nombre, new CreateCollectionOptions().validationOptions(opciones));
    }

    /**
     * Lee los documentos iniciales de una colección desde su JSON (formato mongoexport).
     *
     * @param nombre nombre de la colección
     * @return documentos a insertar
     * @throws IOException si no se encuentra el fichero
     */
    private static List<Document> leerDocumentos(String nombre) throws IOException {
        Document envoltorio = Document.parse("{\"docs\":" + leerTexto(nombre + ".json") + "}");
        return envoltorio.getList("docs", Document.class);
    }

    /**
     * Lee un recurso de texto del JAR.
     *
     * @param fichero nombre del fichero dentro de la carpeta de datos iniciales
     * @return su contenido en UTF-8
     * @throws IOException si el recurso no existe o no se puede leer
     */
    private static String leerTexto(String fichero) throws IOException {
        try (InputStream entrada = InicializadorBD.class.getResourceAsStream(RUTA + fichero)) {
            if (entrada == null) {
                throw new IOException("No se encuentra el recurso " + RUTA + fichero);
            }
            return new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /**
     * Crea los índices únicos y de rendimiento (los mismos que {@code 02_indices.js}).
     *
     * @param bd base de datos
     */
    private static void crearIndices(MongoDatabase bd) {
        indice(bd, "Salas", Indexes.ascending("nombre"), "idx_salas_nombre_unique", true);
        indice(bd, "Clases", Indexes.ascending("nombre"), "idx_clases_nombre_unique", true);
        indice(bd, "Clases", Indexes.ascending("salaid"), "idx_clases_salaid", false);
        indice(bd, "Sesiones", Indexes.ascending("claseid", "fecha"), "idx_sesiones_clase_fecha", false);
        indice(bd, "Sesiones", Indexes.ascending("monitor", "fecha"), "idx_sesiones_monitor_fecha", false);
        indice(bd, "Sesiones", Indexes.ascending("asistentes.clienteid"), "idx_sesiones_asistentes", false);
        indice(bd, "Clientes", Indexes.ascending("mail"), "idx_clientes_mail_unique", true);
        indice(bd, "Clientes", Indexes.ascending("dni"), "idx_clientes_dni_unique", true);
        indice(bd, "Clientes", Indexes.ascending("clases.id_clase"), "idx_clientes_clases", false);
        indice(bd, "Trabajadores", Indexes.ascending("mail"), "idx_trabajadores_mail_unique", true);
        indice(bd, "Trabajadores", Indexes.ascending("dni"), "idx_trabajadores_dni_unique", true);
        indice(bd, "Trabajadores", Indexes.ascending("tipo"), "idx_trabajadores_tipo", false);
    }

    /**
     * Crea un índice con nombre y, opcionalmente, único.
     *
     * @param bd        base de datos
     * @param coleccion colección donde se crea
     * @param claves    campos del índice
     * @param nombre    nombre del índice
     * @param unico     {@code true} para que no admita valores repetidos
     */
    private static void indice(MongoDatabase bd, String coleccion, Bson claves, String nombre, boolean unico) {
        bd.getCollection(coleccion).createIndex(claves, new IndexOptions().name(nombre).unique(unico));
    }
}
