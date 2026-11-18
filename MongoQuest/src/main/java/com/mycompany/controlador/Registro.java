package com.mycompany.controlador;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Registro de eventos:
 * general.0.log (info y avisos), errores.0.log (errores graves con traza)
 * y operaciones.0.log (operaciones contra la base de datos).
 */
public class Registro {

    /** Logger principal: recibe los mensajes generales y los errores graves. */
    private static final Logger LOG = Logger.getLogger("tolotolo");
    /** Logger de las operaciones contra la base de datos; escribe solo en el log de operaciones. */
    private static final Logger LOG_BD = Logger.getLogger("tolotolo.bd");
    /** Formato de fecha y hora con el que empieza cada línea de log. */
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Usuario que ha iniciado sesión; se incluye en cada operación de base de datos.
    /** Usuario en sesión (su correo); vale {@code "anónimo"} mientras nadie haya iniciado sesión. */
    private static volatile String usuario = "anónimo";

    // Carpeta de los logs: datos/logs, junto a donde se ejecuta la aplicación.
    /** Carpeta donde se guardan los archivos de log. */
    private static final Path CARPETA = Paths.get("datos", "logs");

    // Formato de cada línea; la traza completa solo se escribe si conTraza es true.
    /** Formato de cada línea de los logs general y de errores. */
    private static class Formato extends Formatter {

        /** Si es {@code true}, se escribe la traza completa de la excepción; si no, solo su tipo y mensaje. */
        private final boolean conTraza;

        /**
         * Crea el formato.
         *
         * @param conTraza {@code true} para incluir la traza completa de las excepciones
         */
        Formato(boolean conTraza) {
            this.conTraza = conTraza;
        }

        /**
         * Da formato a un registro: fecha, nivel y mensaje, y la excepción si la hay.
         *
         * @param r registro que se va a escribir
         * @return línea de texto ya formateada
         */
        @Override
        public String format(LogRecord r) {
            LocalDateTime hora = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(r.getMillis()), ZoneId.systemDefault());
            StringBuilder sb = new StringBuilder();
            sb.append(hora.format(FECHA))
              .append(" [").append(r.getLevel().getName()).append("] ")
              .append(formatMessage(r));

            Throwable causa = r.getThrown();
            if (causa != null) {
                if (conTraza) {
                    sb.append(System.lineSeparator());
                    StringWriter sw = new StringWriter();
                    causa.printStackTrace(new PrintWriter(sw));
                    sb.append(sw);
                } else {
                    sb.append(" -> ").append(causa.getClass().getSimpleName())
                      .append(": ").append(causa.getMessage());
                    sb.append(System.lineSeparator());
                }
            } else {
                sb.append(System.lineSeparator());
            }
            return sb.toString();
        }
    }

    // Formato del log de operaciones: fecha | usuario | tipo | detalle | resultado
    /** Formato del log de operaciones: fecha, usuario, tipo, detalle y resultado. */
    private static class FormatoOperaciones extends Formatter {

        /**
         * Da formato a un registro de operación: fecha y mensaje.
         *
         * @param r registro que se va a escribir
         * @return línea de texto ya formateada
         */
        @Override
        public String format(LogRecord r) {
            LocalDateTime hora = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(r.getMillis()), ZoneId.systemDefault());
            return hora.format(FECHA) + " | " + formatMessage(r) + System.lineSeparator();
        }
    }

    static {
        LOG.setLevel(Level.INFO);
        LOG_BD.setLevel(Level.INFO);
        LOG_BD.setUseParentHandlers(false);   // las operaciones van solo a su archivo
        try {
            Files.createDirectories(CARPETA);
            // FileHandler espera "/" como separador, también en Windows.
            String base = CARPETA.toString().replace('\\', '/');

            // General: info y avisos. Los errores graves NO se escriben aquí.
            FileHandler general = new FileHandler(base + "/general.%g.log", 1000000, 3, true);
            general.setEncoding("UTF-8");
            general.setLevel(Level.INFO);
            general.setFilter(new Filter() {
                @Override
                public boolean isLoggable(LogRecord r) {
                    return r.getLevel().intValue() < Level.SEVERE.intValue();
                }
            });
            general.setFormatter(new Formato(false));
            LOG.addHandler(general);

            // Errores: solo nivel SEVERE, con la traza completa.
            FileHandler errores = new FileHandler(base + "/errores.%g.log", 1000000, 3, true);
            errores.setEncoding("UTF-8");
            errores.setLevel(Level.SEVERE);
            errores.setFormatter(new Formato(true));
            LOG.addHandler(errores);

            // Operaciones: todo lo que se hace contra la base de datos.
            FileHandler operaciones = new FileHandler(base + "/operaciones.%g.log", 1000000, 3, true);
            operaciones.setEncoding("UTF-8");
            operaciones.setLevel(Level.INFO);
            operaciones.setFormatter(new FormatoOperaciones());
            LOG_BD.addHandler(operaciones);
        } catch (IOException e) {
            System.err.println("No se pudieron crear los archivos de log: " + e.getMessage());
        }
    }

    /**
     * Registra un mensaje informativo en el log general.
     *
     * @param mensaje texto descriptivo del evento
     */
    public static void info(String mensaje) {
        LOG.info(mensaje);
    }

    /**
     * Registra un aviso en el log general.
     *
     * @param mensaje texto descriptivo del aviso
     */
    public static void aviso(String mensaje) {
        LOG.warning(mensaje);
    }

    /**
     * Registra un error grave con su traza de excepción.
     *
     * @param mensaje texto que describe el error
     * @param causa excepción asociada al problema
     */
    public static void error(String mensaje, Throwable causa) {
        LOG.log(Level.SEVERE, mensaje, causa);
    }

    // ---------- Lectura del log de operaciones ----------

    /**
     * Lee el log de operaciones actual ({@code datos/logs/operaciones.0.log}) para mostrarlo
     * en la aplicación. Cada línea tiene el formato
     * {@code fecha | usuario | tipo | detalle | resultado}.
     *
     * @return las líneas del log, de la más antigua a la más reciente; vacía si aún no existe
     * @throws IOException si el archivo existe pero no se puede leer
     */
    public static List<String> leerOperaciones() throws IOException {
        Path archivo = CARPETA.resolve("operaciones.0.log");
        if (!Files.exists(archivo)) {
            return new ArrayList<String>();
        }
        return Files.readAllLines(archivo, StandardCharsets.UTF_8);
    }

    // ---------- Usuario actual ----------

    // Se llama al iniciar sesión (con el mail) y al cerrarla (con null).
    /**
     * Establece el usuario actual para asociarlo a cada operación registrada.
     *
     * @param u correo o identificador del usuario activo, o {@code null} para resetearlo
     */
    public static void setUsuario(String u) {
        usuario = (u == null || u.trim().isEmpty()) ? "anónimo" : u.trim();
    }

    /**
     * Devuelve el correo o identificador del usuario activo.
     *
     * @return usuario actual en sesión o {@code anónimo}
     */
    public static String getUsuario() {
        return usuario;
    }

    // ---------- Operaciones contra la base de datos ----------

    // Operación terminada correctamente.
    /**
     * Guarda en el log de operaciones una operación de Base de Datos correcta.
     *
     * @param tipo tipo de operación ejecutada
     * @param detalle descripción detallada de la acción
     */
    public static void operacion(String tipo, String detalle) {
        LOG_BD.info(usuario + " | " + tipo + " | " + limpiar(detalle) + " | OK");
    }

    // Operación que ha fallado: se guarda la excepción.
    /**
     * Guarda una operación fallida junto con la excepción generada.
     *
     * @param tipo tipo de operación ejecutada
     * @param detalle descripción del fallo
     * @param causa excepción que produjo el error
     */
    public static void operacionError(String tipo, String detalle, Throwable causa) {
        LOG_BD.warning(usuario + " | " + tipo + " | " + limpiar(detalle) + " | ERROR -> "
                + causa.getClass().getSimpleName() + ": " + limpiar(causa.getMessage()));
    }

    // Evita que un mensaje con saltos de línea rompa el formato de una línea por operación.
    /**
     * Quita los saltos de línea de un texto para que cada operación ocupe una sola línea del log.
     *
     * @param texto texto a limpiar; puede ser {@code null}
     * @return el texto sin saltos de línea, o una cadena vacía si era {@code null}
     */
    private static String limpiar(String texto) {
        return texto == null ? "" : texto.replace('\n', ' ').replace('\r', ' ');
    }
}