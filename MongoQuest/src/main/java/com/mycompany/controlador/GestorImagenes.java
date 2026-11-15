package com.mycompany.controlador;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import javafx.scene.image.Image;

/**
 * Gestiona el almacenamiento y carga de imágenes de usuarios en el directorio local
 * de la aplicación.
 */
public class GestorImagenes {

    // Carpeta de las fotos: datos/imagesUsu, junto a donde se ejecuta la aplicación.
    /** Carpeta donde se guardan las fotos de los usuarios. */
    private static final Path CARPETA = Paths.get("datos", "imagesUsu");

    // Solo los formatos que JavaFX sabe mostrar.
    /** Extensiones de imagen admitidas. */
    private static final String[] EXTENSIONES = {"png", "jpg", "jpeg", "gif", "bmp"};

    /**
     * Normaliza un DNI para usarlo como nombre de archivo.
     *
     * @param dni valor del documento de identidad del usuario
     * @return nombre de archivo seguro con letras y números en mayúsculas
     */
    private static String nombreArchivo(String dni) {
        return dni.trim().toUpperCase().replaceAll("[^A-Z0-9]", "_");
    }

    /**
     * Guarda una imagen en el disco asociada a un usuario.
     *
     * @param dni documento del usuario propietario de la imagen
     * @param origen fichero local a copiar
     * @throws IOException si la imagen no tiene un formato soportado o no se puede escribir
     */
    public static void guardar(String dni, File origen) throws IOException {
        String nombre = origen.getName().toLowerCase();
        String extension = nombre.substring(nombre.lastIndexOf('.') + 1);

        if (!esExtensionValida(extension)) {
            Registro.aviso("Imagen rechazada, formato no admitido (." + extension + ") para " + dni);
            throw new IOException("Formato de imagen no admitido: ." + extension);
        }

        Files.createDirectories(CARPETA);
        borrar(dni);   // quita la anterior, aunque tuviera otra extensión

        Path destino = CARPETA.resolve(nombreArchivo(dni) + "." + extension);
        Files.copy(origen.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);
        Registro.info("Imagen guardada: " + destino.getFileName());
    }

    /**
     * Carga la imagen asociada a un usuario si existe en disco.
     *
     * @param dni documento del usuario
     * @return imagen JavaFX o {@code null} si no existe ninguna foto asociada
     */
    public static Image cargar(String dni) {
        for (String extension : EXTENSIONES) {
            Path ruta = CARPETA.resolve(nombreArchivo(dni) + "." + extension);
            if (Files.exists(ruta)) {
                Image imagen = new Image(ruta.toUri().toString());
                if (imagen.isError()) {
                    Registro.error("La imagen está dañada o no se puede leer: " + ruta.getFileName(),
                            imagen.getException());
                    return null;
                }
                return imagen;
            }
        }
        return null;
    }

    /**
     * Borra la imagen asociada al usuario, si existe.
     *
     * @param dni documento del usuario cuya imagen se elimina
     * @throws IOException si ocurre un problema al eliminar el archivo
     */
    public static void borrar(String dni) throws IOException {
        for (String extension : EXTENSIONES) {
            if (Files.deleteIfExists(CARPETA.resolve(nombreArchivo(dni) + "." + extension))) {
                Registro.info("Imagen borrada de " + dni);
            }
        }
    }

    /**
     * Renombra la imagen del usuario cuando cambia su identificador.
     *
     * @param dniViejo identificador previo del usuario
     * @param dniNuevo nuevo identificador del usuario
     * @throws IOException si falla el movimiento de la imagen
     */
    public static void renombrar(String dniViejo, String dniNuevo) throws IOException {
        if (nombreArchivo(dniViejo).equals(nombreArchivo(dniNuevo))) {
            return;
        }
        for (String extension : EXTENSIONES) {
            Path vieja = CARPETA.resolve(nombreArchivo(dniViejo) + "." + extension);
            if (Files.exists(vieja)) {
                Files.move(vieja, CARPETA.resolve(nombreArchivo(dniNuevo) + "." + extension),
                        StandardCopyOption.REPLACE_EXISTING);
                Registro.info("Imagen renombrada: " + dniViejo + " -> " + dniNuevo);
            }
        }
    }

    /**
     * Comprueba si la extensión del archivo está permitida por la aplicación.
     *
     * @param extension extensión a validar
     * @return {@code true} si la extensión es admitida
     */
    private static boolean esExtensionValida(String extension) {
        for (String e : EXTENSIONES) {
            if (e.equals(extension)) {
                return true;
            }
        }
        return false;
    }
}