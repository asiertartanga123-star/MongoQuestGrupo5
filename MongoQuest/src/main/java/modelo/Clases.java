/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import java.util.ArrayList;

/** Describe una actividad del gimnasio, sus horarios y la sala asignada. */
public class Clases {
    
    private String id;
    private String nombre;
    // Una clase puede celebrarse en varios horarios.
    private ArrayList<Horas> horas = new ArrayList<>();
    private String salaId;

    /** Crea una clase identificada y asociada a una sala. */
    public Clases(String id, String nombre, String salaId) {
        this.id = id;
        this.nombre = nombre;
        this.salaId = salaId;
    }

    // Getters y setters de los datos de la clase.
    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public ArrayList<Horas> getHoras() {
        return horas;
    }

    public String getSalaId() {
        return salaId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setHoras(ArrayList<Horas> horas) {
        this.horas = horas;
    }

    public void setSalaId(String salaId) {
        this.salaId = salaId;
    }

    @Override
    public String toString() {
        // Resume la clase y sus relaciones en texto.
        return "Clases{" + "id=" + id + ", nombre=" + nombre + ", horas=" + horas + ", salaId=" + salaId + '}';
    }
   
}
