package com.mycompany.controlador;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import javafx.scene.image.Image;

/**
 * Guarda, carga, renombra y borra las fotos de perfil de los usuarios.
 */
public final class GestorImagenes {

    private static final String CARPETA = "datos/imagesUsu";
    private static final String RUTA_RECURSOS = "/com/mycompany/mongoquest/imagesUsu/";
    private static final String[] EXTENSIONES = {".png", ".jpg", ".jpeg", ".gif", ".bmp"};

    private GestorImagenes() {
    }

    public static Image cargar(String dni) {
        if (dni == null || dni.isEmpty()) {
            return null;
        }

        // 1. Buscar en la carpeta externa datos/imagesUsu
        Path dir = Paths.get(CARPETA);
        if (Files.isDirectory(dir)) {
            for (int i = 0; i < EXTENSIONES.length; i++) {
                String ext = EXTENSIONES[i];
                Path candidato = dir.resolve(dni + ext);
                if (Files.isRegularFile(candidato)) {
                    return new Image(candidato.toUri().toString());
                }
            }
        }

        // 2. Buscar en los recursos internos (src/main/resources/com/mycompany/mongoquest/imagesUsu)
        for (int i = 0; i < EXTENSIONES.length; i++) {
            String ext = EXTENSIONES[i];
            URL recurso = GestorImagenes.class.getResource(RUTA_RECURSOS + dni + ext);
            if (recurso != null) {
                return new Image(recurso.toExternalForm());
            }
        }

        return null;
    }

    public static void guardar(String dni, File origen) throws IOException {
        Path dir = Paths.get(CARPETA);
        Files.createDirectories(dir);
        borrar(dni);
        Path destino = dir.resolve(dni + extension(origen.getName()));
        Files.copy(origen.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);
    }

    public static void renombrar(String dniAnterior, String dniNuevo) throws IOException {
        if (dniAnterior == null || dniNuevo == null || dniAnterior.equals(dniNuevo)) {
            return;
        }
        Path dir = Paths.get(CARPETA);
        if (!Files.isDirectory(dir)) {
            return;
        }
        for (int i = 0; i < EXTENSIONES.length; i++) {
            String ext = EXTENSIONES[i];
            Path anterior = dir.resolve(dniAnterior + ext);
            if (Files.isRegularFile(anterior)) {
                Files.move(anterior, dir.resolve(dniNuevo + ext), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    public static void borrar(String dni) throws IOException {
        if (dni == null || dni.isEmpty()) {
            return;
        }
        Path dir = Paths.get(CARPETA);
        if (!Files.isDirectory(dir)) {
            return;
        }
        try (DirectoryStream<Path> archivos = Files.newDirectoryStream(dir, dni + ".*")) {
            for (Path archivo : archivos) {
                Files.deleteIfExists(archivo);
            }
        }
    }

    private static String extension(String nombre) {
        int punto = nombre.lastIndexOf('.');
        if (punto == -1) {
            return ".png";
        } else {
            return nombre.substring(punto).toLowerCase();
        }
    }
}