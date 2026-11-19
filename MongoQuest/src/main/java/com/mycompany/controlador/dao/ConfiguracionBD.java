package com.mycompany.controlador.dao;

import com.mycompany.controlador.Registro;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Decide a qué servidor MongoDB se conecta la aplicación.
 * <p>
 * Se busca la dirección (URI) en este orden y se usa la primera que exista:
 * <ol>
 *   <li>La propiedad de sistema {@code -Dmongo.uri=...}.</li>
 *   <li>La variable de entorno {@code MONGO_URI}.</li>
 *   <li>El fichero {@code config.properties} (clave {@code mongo.uri}) situado en la carpeta
 *       desde la que se ejecuta la aplicación, junto al JAR.</li>
 *   <li>Por defecto, el MongoDB local: {@code mongodb://localhost:27017}.</li>
 * </ol>
 * Así el mismo JAR sirve para un MongoDB instalado en el propio equipo o para uno remoto
 * (por ejemplo MongoDB Atlas) sin recompilar.
 */
final class ConfiguracionBD {

    /** Dirección usada si no se configura nada; si no responde en 3 segundos se da por caído. */
    static final String URI_POR_DEFECTO = "mongodb://localhost:27017/?serverSelectionTimeoutMS=3000";
    /** Nombre del fichero de configuración opcional. */
    private static final String FICHERO = "config.properties";
    /** Clave que contiene la URI dentro del fichero de configuración. */
    private static final String CLAVE = "mongo.uri";

    /** Clase de utilidad: no se instancia. */
    private ConfiguracionBD() {
    }

    /**
     * Devuelve la URI de conexión que hay que usar.
     *
     * @return la URI configurada o, si no hay ninguna, la del MongoDB local
     */
    static String uri() {
        String valor = System.getProperty(CLAVE);
        String origen = "propiedad -D" + CLAVE;
        if (vacio(valor)) {
            valor = System.getenv("MONGO_URI");
            origen = "variable de entorno MONGO_URI";
        }
        if (vacio(valor)) {
            valor = leerFichero();
            origen = "fichero " + FICHERO;
        }
        if (vacio(valor)) {
            Registro.info("Conexión a MongoDB: servidor local por defecto");
            return URI_POR_DEFECTO;
        }
        // No se escribe la URI en el log porque puede contener la contraseña.
        Registro.info("Conexión a MongoDB configurada desde " + origen);
        return valor.trim();
    }

    /**
     * Lee la URI del fichero {@code config.properties} si existe.
     *
     * @return la URI del fichero, o {@code null} si no existe o no se puede leer
     */
    private static String leerFichero() {
        Path ruta = Paths.get(FICHERO);
        if (!Files.isRegularFile(ruta)) {
            return null;
        }
        Properties propiedades = new Properties();
        try (BufferedReader lector = Files.newBufferedReader(ruta, StandardCharsets.UTF_8)) {
            propiedades.load(lector);
        } catch (IOException e) {
            Registro.aviso("No se pudo leer " + FICHERO + ": " + e.getMessage());
            return null;
        }
        return propiedades.getProperty(CLAVE);
    }

    /**
     * Indica si un texto es nulo o está en blanco.
     *
     * @param texto texto a comprobar
     * @return {@code true} si no tiene contenido
     */
    private static boolean vacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }
}
