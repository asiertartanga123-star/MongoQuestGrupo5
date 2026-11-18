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

    /** Carpeta externa donde se guardan las fotos subidas por los usuarios. */
    private static final String CARPETA = "datos/imagesUsu";
    /** Ruta, dentro de los recursos de la aplicación, de las fotos que vienen de serie. */
    private static final String RUTA_RECURSOS = "/com/mycompany/mongoquest/imagesUsu/";
    /** Extensiones de imagen que se buscan, por orden de preferencia. */
    private static final String[] EXTENSIONES = {".png", ".jpg", ".jpeg", ".gif", ".bmp"};

    /** Clase de utilidades: solo tiene métodos estáticos y no se instancia. */
    private GestorImagenes() {
    }

    /**
     * Carga la foto de perfil de un usuario. Busca primero en la carpeta externa
     * {@code datos/imagesUsu} y, si no está, en los recursos de la aplicación.
     *
     * @param dni DNI del usuario, que es el nombre del archivo de la foto
     * @return la imagen, o {@code null} si el DNI está vacío o no hay foto
     */
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

    /**
     * Copia una foto a la carpeta externa con el DNI como nombre, sustituyendo la anterior.
     *
     * @param dni    DNI del usuario
     * @param origen archivo de imagen elegido por el usuario
     * @throws IOException si no se puede crear la carpeta o copiar el archivo
     */
    public static void guardar(String dni, File origen) throws IOException {
        Path dir = Paths.get(CARPETA);
        Files.createDirectories(dir);
        borrar(dni);
        Path destino = dir.resolve(dni + extension(origen.getName()));
        Files.copy(origen.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Renombra la foto de un usuario cuando cambia su DNI. No hace nada si los DNI son iguales.
     *
     * @param dniAnterior DNI con el que está guardada la foto
     * @param dniNuevo    DNI nuevo del usuario
     * @throws IOException si no se puede mover el archivo
     */
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

    /**
     * Borra todas las fotos de un usuario de la carpeta externa, sea cual sea su extensión.
     *
     * @param dni DNI del usuario
     * @throws IOException si no se puede borrar algún archivo
     */
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

    /**
     * Devuelve la extensión de un nombre de archivo, en minúsculas.
     *
     * @param nombre nombre del archivo
     * @return la extensión con el punto (por ejemplo {@code .jpg}), o {@code .png} si no tiene
     */
    private static String extension(String nombre) {
        int punto = nombre.lastIndexOf('.');
        if (punto == -1) {
            return ".png";
        } else {
            return nombre.substring(punto).toLowerCase();
        }
    }
}