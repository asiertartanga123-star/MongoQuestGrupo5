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

/** Registro de eventos: general.0.log (info y avisos) y errores.0.log (solo errores graves). */
public class Registro {

    private static final Logger LOG = Logger.getLogger("tolotolo");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Carpeta de los logs, la misma que se ve en NetBeans dentro de resources.
    private static final Path CARPETA = Paths.get("src", "main", "resources",
            "com", "mycompany", "mongoquest", "logs");

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

    static {
        LOG.setLevel(Level.INFO);
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
}