/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import java.time.LocalDate;
import java.util.ArrayList;

/** Representa a un trabajador del gimnasio y el rol que desempeña. */
public class Trabajadores {
    
    // Datos personales y credenciales del trabajador.
    /** El identificador del trabajador. */
    private String id;
    /** El DNI del trabajador. */
    private String dni;
    /** El apellido del trabajador. */
    private String apellido;
    /** La fecha de alta del trabajador. */
    private LocalDate fechaAlta;
    /** El correo electrónico del trabajador. */
    private String mail;
    /** La contraseña de acceso del trabajador. */
    private String contraseña;

    // El tipo diferencia al gerente del monitor; también se guardan sus clases.
    /** El tipo de trabajador (gerente o monitor). */
    private Tipo tipo;
    /** La lista de clases asignadas al trabajador. */
    private ArrayList<Clases> clases = new ArrayList<>();

    /**
     * Inicializa un trabajador con su información personal y su tipo. La lista de clases empieza vacía.
     *
     * @param id identificador del trabajador
     * @param dni DNI del trabajador
     * @param apellido apellido del trabajador
     * @param fechaAlta fecha de alta
     * @param mail correo electrónico
     * @param contraseña contraseña de acceso
     * @param tipo tipo de trabajador (gerente o monitor)
     */
    public Trabajadores(String id, String dni, String apellido, LocalDate fechaAlta, String mail, String contraseña, Tipo tipo) {
        this.id = id;
        this.dni = dni;
        this.apellido = apellido;
        this.fechaAlta = fechaAlta;
        this.mail = mail;
        this.contraseña = contraseña;
        this.tipo = tipo;
    }

    // Getters y setters para leer o modificar los datos del trabajador.
    /**
     * Devuelve el identificador del trabajador.
     *
     * @return el identificador del trabajador
     */
    public String getId() {
        return id;
    }

    /**
     * Devuelve el DNI del trabajador.
     *
     * @return el DNI del trabajador
     */
    public String getDni() {
        return dni;
    }

    /**
     * Devuelve el apellido del trabajador.
     *
     * @return el apellido del trabajador
     */
    public String getApellido() {
        return apellido;
    }

    /**
     * Devuelve la fecha de alta del trabajador.
     *
     * @return la fecha de alta del trabajador
     */
    public LocalDate getFechaAlta() {
        return fechaAlta;
    }

    /**
     * Devuelve el correo electrónico del trabajador.
     *
     * @return el correo electrónico del trabajador
     */
    public String getMail() {
        return mail;
    }

    /**
     * Devuelve la contraseña de acceso del trabajador.
     *
     * @return la contraseña de acceso del trabajador
     */
    public String getContraseña() {
        return contraseña;
    }

    /**
     * Devuelve el tipo de trabajador (gerente o monitor).
     *
     * @return el tipo de trabajador (gerente o monitor)
     */
    public Tipo getTipo() {
        return tipo;
    }

    /**
     * Devuelve la lista de clases asignadas al trabajador.
     *
     * @return la lista de clases asignadas al trabajador
     */
    public ArrayList<Clases> getClases() {
        return clases;
    }

    /**
     * Establece el identificador del trabajador.
     *
     * @param id el identificador del trabajador
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Establece el DNI del trabajador.
     *
     * @param dni el DNI del trabajador
     */
    public void setDni(String dni) {
        this.dni = dni;
    }

    /**
     * Establece el apellido del trabajador.
     *
     * @param apellido el apellido del trabajador
     */
    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    /**
     * Establece la fecha de alta del trabajador.
     *
     * @param fechaAlta la fecha de alta del trabajador
     */
    public void setFechaAlta(LocalDate fechaAlta) {
        this.fechaAlta = fechaAlta;
    }

    /**
     * Establece el correo electrónico del trabajador.
     *
     * @param mail el correo electrónico del trabajador
     */
    public void setMail(String mail) {
        this.mail = mail;
    }

    /**
     * Establece la contraseña de acceso del trabajador.
     *
     * @param contraseña la contraseña de acceso del trabajador
     */
    public void setContraseña(String contraseña) {
        this.contraseña = contraseña;
    }

    /**
     * Establece el tipo de trabajador (gerente o monitor).
     *
     * @param tipo el tipo de trabajador (gerente o monitor)
     */
    public void setTipo(Tipo tipo) {
        this.tipo = tipo;
    }

    /**
     * Establece la lista de clases asignadas al trabajador.
     *
     * @param clases la lista de clases asignadas al trabajador
     */
    public void setClases(ArrayList<Clases> clases) {
        this.clases = clases;
    }

    /**
     * Devuelve una representación del trabajador en forma de texto, útil para depuración.
     *
     * @return texto con todos los datos del trabajador
     */
    @Override
    public String toString() {
        // Facilita inspeccionar el objeto durante el desarrollo.
        return "Trabajadores{" + "id=" + id + ", dni=" + dni + ", apellido=" + apellido + ", fechaAlta=" + fechaAlta + ", mail=" + mail + ", contrase\u00f1a=" + contraseña + ", tipo=" + tipo + ", clases=" + clases + '}';
    }

    
}
