package com.mycompany.controlador;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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

    private static final Logger LOG = Logger.getLogger("tolotolo");
    private static final Logger LOG_BD = Logger.getLogger("tolotolo.bd");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Usuario que ha iniciado sesión; se incluye en cada operación de base de datos.
    private static volatile String usuario = "anónimo";

    // Carpeta de los logs: datos/logs, junto a donde se ejecuta la aplicación.
    private static final Path CARPETA = Paths.get("datos", "logs");

    // Formato de cada línea; la traza completa solo se escribe si conTraza es true.
    private static class Formato extends Formatter {

        private final boolean conTraza;

        Formato(boolean conTraza) {
            this.conTraza = conTraza;
        }

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
    private static class FormatoOperaciones extends Formatter {

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

    public static void info(String mensaje) {
        LOG.info(mensaje);
    }

    public static void aviso(String mensaje) {
        LOG.warning(mensaje);
    }

    public static void error(String mensaje, Throwable causa) {
        LOG.log(Level.SEVERE, mensaje, causa);
    }

    // ---------- Usuario actual ----------

    // Se llama al iniciar sesión (con el mail) y al cerrarla (con null).
    public static void setUsuario(String u) {
        usuario = (u == null || u.trim().isEmpty()) ? "anónimo" : u.trim();
    }

    public static String getUsuario() {
        return usuario;
    }

    // ---------- Operaciones contra la base de datos ----------

    // Operación terminada correctamente.
    public static void operacion(String tipo, String detalle) {
        LOG_BD.info(usuario + " | " + tipo + " | " + limpiar(detalle) + " | OK");
    }

    // Operación que ha fallado: se guarda la excepción.
    public static void operacionError(String tipo, String detalle, Throwable causa) {
        LOG_BD.warning(usuario + " | " + tipo + " | " + limpiar(detalle) + " | ERROR -> "
                + causa.getClass().getSimpleName() + ": " + limpiar(causa.getMessage()));
    }

    // Evita que un mensaje con saltos de línea rompa el formato de una línea por operación.
    private static String limpiar(String texto) {
        return texto == null ? "" : texto.replace('\n', ' ').replace('\r', ' ');
    }
}