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
    private String id;
    private String claseId;
    private LocalDate fecha;
    private int plazaMax;
    private String monitorId;
    // La lista guarda los identificadores de los clientes inscritos.
    private ArrayList<String> asistentes = new ArrayList<>();

    /** Crea una sesión con su clase, fecha, aforo y monitor. */
    public Sesiones(String id, String claseId, LocalDate fecha, int plazaMax, String monitorId) {
        this.id = id;
        this.claseId = claseId;
        this.fecha = fecha;
        this.plazaMax = plazaMax;
        this.monitorId = monitorId;
    }

    // Getters y setters para consultar o modificar los datos de la sesión.
    public String getId() {
        return id;
    }

    public String getClaseId() {
        return claseId;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public int getPlazaMax() {
        return plazaMax;
    }

    public String getMonitorId() {
        return monitorId;
    }

    public ArrayList getAsistentes() {
        return asistentes;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setClaseId(String claseId) {
        this.claseId = claseId;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public void setPlazaMax(int plazaMax) {
        this.plazaMax = plazaMax;
    }

    public void setMonitorId(String monitorId) {
        this.monitorId = monitorId;
    }

    public void setAsistentes(ArrayList asistentes) {
        this.asistentes = asistentes;
    }

    /** Indica si queda al menos una plaza libre. */
    public boolean hayPlazas() {
        return asistentes.size() < plazaMax;
    }

    /**
     * Inscribe al cliente si hay capacidad y todavía no figura como asistente.
     * @return true si el cliente se ha añadido; false si no puede inscribirse
     */
    public boolean apuntar(String clienteId) {
        if (!hayPlazas() || asistentes.contains(clienteId)) {
            return false;
        }
        return asistentes.add(clienteId);
    }

    /** Elimina al cliente de la lista de asistentes si estaba inscrito. */
    public boolean desapuntar(String clienteId) {
        return asistentes.remove(clienteId);
    }
    
    @Override
    public String toString() {
        // Permite ver el estado completo de la sesión durante el desarrollo.
        return "Sesiones{" + "id=" + id + ", claseId=" + claseId + ", fecha=" + fecha + ", plazaMax=" + plazaMax + ", monitorId=" + monitorId + ", asistentes=" + asistentes + '}';
    }
}
