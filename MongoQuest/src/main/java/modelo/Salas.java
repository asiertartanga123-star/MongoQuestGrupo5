/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/** Representa una sala del gimnasio y el aforo máximo permitido. */
public class Salas {
    
    private String id;
    private String nombre;
    private int aforoMAX;

    /** Crea una sala con identificador, nombre y capacidad máxima. */
    public Salas(String id, String nombre, int aforoMAX) {
        this.id = id;
        this.nombre = nombre;
        this.aforoMAX = aforoMAX;
    }

    // Getters y setters para mantener accesibles los datos de la sala.
    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public int getAforoMAX() {
        return aforoMAX;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setAforoMAX(int aforoMAX) {
        this.aforoMAX = aforoMAX;
    }

    @Override
    public String toString() {
        // Resume los datos de la sala para facilitar su visualización.
        return "Salas{" + "id=" + id + ", nombre=" + nombre + ", aforoMAX=" + aforoMAX + '}';
    }
}
