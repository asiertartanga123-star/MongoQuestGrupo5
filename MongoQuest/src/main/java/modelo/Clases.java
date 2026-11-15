/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import java.util.ArrayList;

/** Describe una actividad del gimnasio, sus horarios y la sala asignada. */
public class Clases {
    
    /** El identificador de la clase. */
    private String id;
    /** El nombre de la clase. */
    private String nombre;
    // Una clase puede celebrarse en varios horarios.
    /** La lista de horarios en los que se celebra la clase. */
    private ArrayList<Horas> horas = new ArrayList<>();
    /** El identificador de la sala asignada a la clase. */
    private String salaId;

    /**
     * Crea una clase identificada y asociada a una sala. La lista de horarios empieza vacía.
     *
     * @param id identificador de la clase
     * @param nombre nombre de la clase
     * @param salaId identificador de la sala donde se imparte
     */
    public Clases(String id, String nombre, String salaId) {
        this.id = id;
        this.nombre = nombre;
        this.salaId = salaId;
    }

    // Getters y setters de los datos de la clase.
    /**
     * Devuelve el identificador de la clase.
     *
     * @return el identificador de la clase
     */
    public String getId() {
        return id;
    }

    /**
     * Devuelve el nombre de la clase.
     *
     * @return el nombre de la clase
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Devuelve la lista de horarios en los que se celebra la clase.
     *
     * @return la lista de horarios en los que se celebra la clase
     */
    public ArrayList<Horas> getHoras() {
        return horas;
    }

    /**
     * Devuelve el identificador de la sala asignada a la clase.
     *
     * @return el identificador de la sala asignada a la clase
     */
    public String getSalaId() {
        return salaId;
    }

    /**
     * Establece el identificador de la clase.
     *
     * @param id el identificador de la clase
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Establece el nombre de la clase.
     *
     * @param nombre el nombre de la clase
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Establece la lista de horarios en los que se celebra la clase.
     *
     * @param horas la lista de horarios en los que se celebra la clase
     */
    public void setHoras(ArrayList<Horas> horas) {
        this.horas = horas;
    }

    /**
     * Establece el identificador de la sala asignada a la clase.
     *
     * @param salaId el identificador de la sala asignada a la clase
     */
    public void setSalaId(String salaId) {
        this.salaId = salaId;
    }

    /**
     * Devuelve un resumen de la clase y sus relaciones en forma de texto.
     *
     * @return texto con el identificador, el nombre, los horarios y la sala
     */
    @Override
    public String toString() {
        // Resume la clase y sus relaciones en texto.
        return "Clases{" + "id=" + id + ", nombre=" + nombre + ", horas=" + horas + ", salaId=" + salaId + '}';
    }
   
}
