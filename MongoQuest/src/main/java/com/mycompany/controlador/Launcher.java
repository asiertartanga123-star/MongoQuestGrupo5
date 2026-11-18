package com.mycompany.controlador;

/**
 * Clase de lanzamiento alternativo para la aplicación JavaFX.
 * <p>
 * Se usa como punto de entrada del artefacto ejecutable generado por Maven
 * para invocar la clase principal de la interfaz gráfica.
 */
public class Launcher {

    /** Crea el lanzador; solo se usa su método {@code main}. */
    public Launcher() {
    }
    /**
     * Ejecuta la aplicación JavaFX desde el punto de entrada del lanzador.
     *
     * @param args argumentos pasados por la línea de comandos
     */
    public static void main(String[] args) {
        // Reemplaza 'AppPrincipal' por el nombre de tu clase de JavaFX
        App.main(args); 
    }
}
