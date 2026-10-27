/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Ekain.Bedoya
 */
public class Salas {
    
    private String id;
    private String nombre;
    private int aforoMAX;

    public Salas(String id, String nombre, int aforoMAX) {
        this.id = id;
        this.nombre = nombre;
        this.aforoMAX = aforoMAX;
    }

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
        return "Salas{" + "id=" + id + ", nombre=" + nombre + ", aforoMAX=" + aforoMAX + '}';
    }
}
