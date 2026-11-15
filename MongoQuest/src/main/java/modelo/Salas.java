/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/** Representa una sala del gimnasio y el aforo máximo permitido. */
public class Salas {
    
    /** El identificador de la sala. */
    private String id;
    /** El nombre de la sala. */
    private String nombre;
    /** El aforo máximo permitido en la sala. */
    private int aforoMAX;

    /**
     * Crea una sala con identificador, nombre y capacidad máxima.
     *
     * @param id identificador de la sala
     * @param nombre nombre de la sala
     * @param aforoMAX número máximo de personas permitido en la sala
     */
    public Salas(String id, String nombre, int aforoMAX) {
        this.id = id;
        this.nombre = nombre;
        this.aforoMAX = aforoMAX;
    }

    // Getters y setters para mantener accesibles los datos de la sala.
    /**
     * Devuelve el identificador de la sala.
     *
     * @return el identificador de la sala
     */
    public String getId() {
        return id;
    }

    /**
     * Devuelve el nombre de la sala.
     *
     * @return el nombre de la sala
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Devuelve el aforo máximo permitido en la sala.
     *
     * @return el aforo máximo permitido en la sala
     */
    public int getAforoMAX() {
        return aforoMAX;
    }

    /**
     * Establece el identificador de la sala.
     *
     * @param id el identificador de la sala
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Establece el nombre de la sala.
     *
     * @param nombre el nombre de la sala
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Establece el aforo máximo permitido en la sala.
     *
     * @param aforoMAX el aforo máximo permitido en la sala
     */
    public void setAforoMAX(int aforoMAX) {
        this.aforoMAX = aforoMAX;
    }

    /**
     * Devuelve un resumen de la sala en forma de texto.
     *
     * @return texto con el identificador, el nombre y el aforo máximo
     */
    @Override
    public String toString() {
        // Resume los datos de la sala para facilitar su visualización.
        return "Salas{" + "id=" + id + ", nombre=" + nombre + ", aforoMAX=" + aforoMAX + '}';
    }
}
