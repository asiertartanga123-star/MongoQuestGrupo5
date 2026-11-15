/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import java.time.LocalDate;
import java.util.ArrayList;

/** Representa una convocatoria concreta de una clase en una fecha. */
public class Sesiones {

    // Identificadores de la sesión, la clase y el monitor responsable.
    /** El identificador de la sesión. */
    private String id;
    /** El identificador de la clase a la que pertenece la sesión. */
    private String claseId;
    /** La fecha de la sesión. */
    private LocalDate fecha;
    /** El número máximo de plazas de la sesión. */
    private int plazaMax;
    /** El identificador del monitor responsable de la sesión. */
    private String monitorId;
    // La lista guarda los identificadores de los clientes inscritos.
    /** La lista de identificadores de los clientes inscritos. */
    private ArrayList<String> asistentes = new ArrayList<>();

    /**
     * Crea una sesión con su clase, fecha, aforo y monitor. La lista de asistentes empieza vacía.
     *
     * @param id identificador de la sesión
     * @param claseId identificador de la clase
     * @param fecha fecha en la que se celebra
     * @param plazaMax número máximo de plazas
     * @param monitorId identificador del monitor responsable
     */
    public Sesiones(String id, String claseId, LocalDate fecha, int plazaMax, String monitorId) {
        this.id = id;
        this.claseId = claseId;
        this.fecha = fecha;
        this.plazaMax = plazaMax;
        this.monitorId = monitorId;
    }

    // Getters y setters para consultar o modificar los datos de la sesión.
    /**
     * Devuelve el identificador de la sesión.
     *
     * @return el identificador de la sesión
     */
    public String getId() {
        return id;
    }

    /**
     * Devuelve el identificador de la clase a la que pertenece la sesión.
     *
     * @return el identificador de la clase a la que pertenece la sesión
     */
    public String getClaseId() {
        return claseId;
    }

    /**
     * Devuelve la fecha de la sesión.
     *
     * @return la fecha de la sesión
     */
    public LocalDate getFecha() {
        return fecha;
    }

    /**
     * Devuelve el número máximo de plazas de la sesión.
     *
     * @return el número máximo de plazas de la sesión
     */
    public int getPlazaMax() {
        return plazaMax;
    }

    /**
     * Devuelve el identificador del monitor responsable de la sesión.
     *
     * @return el identificador del monitor responsable de la sesión
     */
    public String getMonitorId() {
        return monitorId;
    }

    /**
     * Devuelve la lista de identificadores de los clientes inscritos.
     *
     * @return la lista de identificadores de los clientes inscritos
     */
    public ArrayList getAsistentes() {
        return asistentes;
    }

    /**
     * Establece el identificador de la sesión.
     *
     * @param id el identificador de la sesión
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Establece el identificador de la clase a la que pertenece la sesión.
     *
     * @param claseId el identificador de la clase a la que pertenece la sesión
     */
    public void setClaseId(String claseId) {
        this.claseId = claseId;
    }

    /**
     * Establece la fecha de la sesión.
     *
     * @param fecha la fecha de la sesión
     */
    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    /**
     * Establece el número máximo de plazas de la sesión.
     *
     * @param plazaMax el número máximo de plazas de la sesión
     */
    public void setPlazaMax(int plazaMax) {
        this.plazaMax = plazaMax;
    }

    /**
     * Establece el identificador del monitor responsable de la sesión.
     *
     * @param monitorId el identificador del monitor responsable de la sesión
     */
    public void setMonitorId(String monitorId) {
        this.monitorId = monitorId;
    }

    /**
     * Establece la lista de identificadores de los clientes inscritos.
     *
     * @param asistentes la lista de identificadores de los clientes inscritos
     */
    public void setAsistentes(ArrayList asistentes) {
        this.asistentes = asistentes;
    }

    /**
     * Indica si queda al menos una plaza libre.
     *
     * @return {@code true} si hay menos asistentes que plazas
     */
    public boolean hayPlazas() {
        return asistentes.size() < plazaMax;
    }

    /**
     * Inscribe al cliente si hay capacidad y todavía no figura como asistente.
     *
     * @param clienteId identificador del cliente que quiere inscribirse
     * @return {@code true} si el cliente se ha añadido; {@code false} si no puede inscribirse
     */
    public boolean apuntar(String clienteId) {
        if (!hayPlazas() || asistentes.contains(clienteId)) {
            return false;
        }
        return asistentes.add(clienteId);
    }

    /**
     * Elimina al cliente de la lista de asistentes si estaba inscrito.
     *
     * @param clienteId identificador del cliente que se quiere desapuntar
     * @return {@code true} si el cliente estaba inscrito y se ha eliminado
     */
    public boolean desapuntar(String clienteId) {
        return asistentes.remove(clienteId);
    }
    
    /**
     * Devuelve el estado completo de la sesión en forma de texto.
     *
     * @return texto con todos los datos de la sesión
     */
    @Override
    public String toString() {
        // Permite ver el estado completo de la sesión durante el desarrollo.
        return "Sesiones{" + "id=" + id + ", claseId=" + claseId + ", fecha=" + fecha + ", plazaMax=" + plazaMax + ", monitorId=" + monitorId + ", asistentes=" + asistentes + '}';
    }
}
