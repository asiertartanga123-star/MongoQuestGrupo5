package com.mycompany.controlador;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import javafx.scene.image.Image;

public class GestorImagenes {

    // Carpeta de las fotos: datos/imagesUsu, junto a donde se ejecuta la aplicación.
    private static final Path CARPETA = Paths.get("datos", "imagesUsu");

    // Solo los formatos que JavaFX sabe mostrar.
    private static final String[] EXTENSIONES = {"png", "jpg", "jpeg", "gif", "bmp"};

    // El DNI solo puede llevar letras y números en el nombre del archivo
    private static String nombreArchivo(String dni) {
        return dni.trim().toUpperCase().replaceAll("[^A-Z0-9]", "_");
    }

    // Copia la imagen a imagesUsu/<DNI>.<extensión>, sustituyendo la anterior
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

    // Devuelve la imagen del usuario, o null si no tiene
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

    // Borra la imagen del usuario, si existe
    public static void borrar(String dni) throws IOException {
        for (String extension : EXTENSIONES) {
            if (Files.deleteIfExists(CARPETA.resolve(nombreArchivo(dni) + "." + extension))) {
                Registro.info("Imagen borrada de " + dni);
            }
        }
    }

    // Cuando cambia el DNI, la imagen pasa a llamarse con el nuevo
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

    private static boolean esExtensionValida(String extension) {
        for (String e : EXTENSIONES) {
            if (e.equals(extension)) {
                return true;
            }
        }
        return false;
    }
}