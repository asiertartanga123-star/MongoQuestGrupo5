package modelo;

/**
 * Una fila de la tabla de logros de un cliente.
 */
public class Logros {

    /** Número del logro. */
    private String numero;
    /** Nombre del logro. */
    private String nombre;
    /** Fecha en la que se consiguió. */
    private String fecha;
    /** Semanas necesarias o transcurridas para conseguirlo. */
    private String semanas;

    /**
     * Crea un logro.
     *
     * @param numero  número del logro
     * @param nombre  nombre del logro
     * @param fecha   fecha en la que se consiguió
     * @param semanas semanas asociadas al logro
     */
    public Logros(String numero, String nombre, String fecha, String semanas) {
        this.numero = numero;
        this.nombre = nombre;
        this.fecha = fecha;
        this.semanas = semanas;
    }

    /**
     * @return número del logro
     */
    public String getNumero() {
        return numero;
    }

    /**
     * @return nombre del logro
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * @return fecha en la que se consiguió
     */
    public String getFecha() {
        return fecha;
    }

    /**
     * @return semanas asociadas al logro
     */
    public String getSemanas() {
        return semanas;
    }
}
