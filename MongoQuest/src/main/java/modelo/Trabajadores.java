/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import java.time.LocalDate;
import java.util.ArrayList;

/**
 *
 * @author Ekain.Bedoya
 */
public class Trabajadores {
    
    private String id;
    private String dni;
    private String apellido;
    private LocalDate fechaAlta;
    private String mail;
    private String contraseña;
    private Tipo tipo;
    private ArrayList<Clases> clases = new ArrayList<>();

    public Trabajadores(String id, String dni, String apellido, LocalDate fechaAlta, String mail, String contraseña, Tipo tipo) {
        this.id = id;
        this.dni = dni;
        this.apellido = apellido;
        this.fechaAlta = fechaAlta;
        this.mail = mail;
        this.contraseña = contraseña;
        this.tipo = tipo;
    }

    public String getId() {
        return id;
    }

    public String getDni() {
        return dni;
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

    public Tipo getTipo() {
        return tipo;
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

    public void setTipo(Tipo tipo) {
        this.tipo = tipo;
    }

    public void setClases(ArrayList<Clases> clases) {
        this.clases = clases;
    }

    @Override
    public String toString() {
        return "Trabajadores{" + "id=" + id + ", dni=" + dni + ", apellido=" + apellido + ", fechaAlta=" + fechaAlta + ", mail=" + mail + ", contrase\u00f1a=" + contraseña + ", tipo=" + tipo + ", clases=" + clases + '}';
    }

    
}
