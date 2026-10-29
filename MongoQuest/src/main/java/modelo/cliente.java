package modelo;

import java.time.LocalDate;
import java.util.ArrayList;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Ekain.Bedoya
 */
public class cliente {
    
    private String id;
    private String dni;
    private String nombre;
    private String apellido;
    private LocalDate fechaAlta;
    private String mail;
    private String contraseña;
    private Membresia membresia;
    private ArrayList<Clases> clases = new ArrayList<>();

    public cliente(String id, String dni, String nombre, String apellido, LocalDate fechaAlta, String mail, String contraseña, Membresia membresia) {
        this.id = id;
        this.dni = dni;
        this.nombre = nombre;
        this.apellido = apellido;
        this.fechaAlta = fechaAlta;
        this.mail = mail;
        this.contraseña = contraseña;
        this.membresia = membresia;
    }

    public String getId() {
        return id;
    }

    public String getDni() {
        return dni;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public LocalDate getFechaAlta() {
        return fechaAlta;
    }

    public String getMail() {
        return mail;
    }

    public String getContraseña() {
        return contraseña;
    }

    public Membresia getMembresia() {
        return membresia;
    }

    public ArrayList<Clases> getClases() {
        return clases;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public void setFechaAlta(LocalDate fechaAlta) {
        this.fechaAlta = fechaAlta;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public void setContraseña(String contraseña) {
        this.contraseña = contraseña;
    }

    public void setMembresia(Membresia membresia) {
        this.membresia = membresia;
    }

    public void setClases(ArrayList<Clases> clases) {
        this.clases = clases;
    }

    @Override
    public String toString() {
        return "cliente{" + "id=" + id + ", dni=" + dni + ", nombre=" + nombre + ", apellido=" + apellido + ", fechaAlta=" + fechaAlta + ", mail=" + mail + ", contrase\u00f1a=" + contraseña + ", membresia=" + membresia + ", clases=" + clases + '}';
    }
    
}
