package com.mycompany.controlador;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import javafx.scene.image.Image;

public class GestorImagenes {

    private static final Path CARPETA = Paths.get("src", "main", "resources",
            "com", "mycompany", "mongoquest", "imagesUsu");
    private static final String[] EXTENSIONES = {"png", "jpg", "jpeg"};

    // El DNI solo puede llevar letras y números en el nombre del archivo
    private static String nombreArchivo(String dni) {
        return dni.trim().toUpperCase().replaceAll("[^A-Z0-9]", "_");
    }

    // Copia la imagen a imagesUsu/<DNI>.<extensión>, sustituyendo la anterior
    public static void guardar(String dni, File origen) throws IOException {
        System.out.println("Guardando en: " + CARPETA.toAbsolutePath());
        Files.createDirectories(CARPETA);
        borrar(dni);   // quita la anterior, aunque tuviera otra extensión

        String nombre = origen.getName().toLowerCase();
        String extension = nombre.substring(nombre.lastIndexOf('.') + 1);
        Files.copy(origen.toPath(), CARPETA.resolve(nombreArchivo(dni) + "." + extension),
                StandardCopyOption.REPLACE_EXISTING);
    }

    // Devuelve la imagen del usuario, o null si no tiene
    public static Image cargar(String dni) {
        for (String extension : EXTENSIONES) {
            Path ruta = CARPETA.resolve(nombreArchivo(dni) + "." + extension);
            if (Files.exists(ruta)) {
                return new Image(ruta.toUri().toString());
            }
        }
        return null;
    }

    // Borra la imagen del usuario, si existe
    public static void borrar(String dni) throws IOException {
        for (String extension : EXTENSIONES) {
            Files.deleteIfExists(CARPETA.resolve(nombreArchivo(dni) + "." + extension));
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
            }
        }
    }
}