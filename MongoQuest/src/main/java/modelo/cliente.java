package modelo;

import java.time.LocalDate;
import java.util.ArrayList;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 * Representa a un cliente y la información necesaria para su cuenta y membresía.
 */
public class cliente {
    
    // Datos de identificación y acceso del cliente.
    /** El identificador del cliente. */
    private String id;
    /** El DNI del cliente. */
    private String dni;
    /** El nombre del cliente. */
    private String nombre;
    /** El apellido del cliente. */
    private String apellido;
    /** La fecha de alta del cliente. */
    private LocalDate fechaAlta;
    /** El correo electrónico del cliente. */
    private String mail;
    /** La contraseña de acceso del cliente. */
    private String contraseña;

    // Relación del cliente con su membresía y las clases a las que pertenece.
    /** La membresía del cliente. */
    private Membresia membresia;
    /** La lista de clases a las que pertenece el cliente. */
    private ArrayList<Clases> clases = new ArrayList<>();

    /**
     * Crea un cliente con sus datos principales; la lista de clases empieza vacía.
     *
     * @param id identificador del cliente
     * @param dni DNI del cliente
     * @param nombre nombre del cliente
     * @param apellido apellido del cliente
     * @param fechaAlta fecha de alta
     * @param mail correo electrónico
     * @param contraseña contraseña de acceso
     * @param membresia tipo de membresía
     */
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

    // Los getters y setters permiten consultar y actualizar cada dato del cliente.
    /**
     * Devuelve el identificador del cliente.
     *
     * @return el identificador del cliente
     */
    public String getId() {
        return id;
    }

    /**
     * Devuelve el DNI del cliente.
     *
     * @return el DNI del cliente
     */
    public String getDni() {
        return dni;
    }

    /**
     * Devuelve el nombre del cliente.
     *
     * @return el nombre del cliente
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Devuelve el apellido del cliente.
     *
     * @return el apellido del cliente
     */
    public String getApellido() {
        return apellido;
    }

    /**
     * Devuelve la fecha de alta del cliente.
     *
     * @return la fecha de alta del cliente
     */
    public LocalDate getFechaAlta() {
        return fechaAlta;
    }

    /**
     * Devuelve el correo electrónico del cliente.
     *
     * @return el correo electrónico del cliente
     */
    public String getMail() {
        return mail;
    }

    /**
     * Devuelve la contraseña de acceso del cliente.
     *
     * @return la contraseña de acceso del cliente
     */
    public String getContraseña() {
        return contraseña;
    }

    /**
     * Devuelve la membresía del cliente.
     *
     * @return la membresía del cliente
     */
    public Membresia getMembresia() {
        return membresia;
    }

    /**
     * Devuelve la lista de clases a las que pertenece el cliente.
     *
     * @return la lista de clases a las que pertenece el cliente
     */
    public ArrayList<Clases> getClases() {
        return clases;
    }

    /**
     * Establece el identificador del cliente.
     *
     * @param id el identificador del cliente
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Establece el DNI del cliente.
     *
     * @param dni el DNI del cliente
     */
    public void setDni(String dni) {
        this.dni = dni;
    }

    /**
     * Establece el nombre del cliente.
     *
     * @param nombre el nombre del cliente
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Establece el apellido del cliente.
     *
     * @param apellido el apellido del cliente
     */
    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    /**
     * Establece la fecha de alta del cliente.
     *
     * @param fechaAlta la fecha de alta del cliente
     */
    public void setFechaAlta(LocalDate fechaAlta) {
        this.fechaAlta = fechaAlta;
    }

    /**
     * Establece el correo electrónico del cliente.
     *
     * @param mail el correo electrónico del cliente
     */
    public void setMail(String mail) {
        this.mail = mail;
    }

    /**
     * Establece la contraseña de acceso del cliente.
     *
     * @param contraseña la contraseña de acceso del cliente
     */
    public void setContraseña(String contraseña) {
        this.contraseña = contraseña;
    }

    /**
     * Establece la membresía del cliente.
     *
     * @param membresia la membresía del cliente
     */
    public void setMembresia(Membresia membresia) {
        this.membresia = membresia;
    }

    /**
     * Establece la lista de clases a las que pertenece el cliente.
     *
     * @param clases la lista de clases a las que pertenece el cliente
     */
    public void setClases(ArrayList<Clases> clases) {
        this.clases = clases;
    }

    /**
     * Devuelve una representación del cliente en forma de texto, útil para depuración y registros.
     *
     * @return texto con todos los datos del cliente
     */
    @Override
    public String toString() {
        // Devuelve una representación legible para depuración y registros.
        return "cliente{" + "id=" + id + ", dni=" + dni + ", nombre=" + nombre + ", apellido=" + apellido + ", fechaAlta=" + fechaAlta + ", mail=" + mail + ", contrase\u00f1a=" + contraseña + ", membresia=" + membresia + ", clases=" + clases + '}';
    }
    
}
