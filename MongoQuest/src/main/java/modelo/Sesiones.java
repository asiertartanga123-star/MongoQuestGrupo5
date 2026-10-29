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
public class Sesiones {

    private String id;
    private String claseId;
    private LocalDate fecha;
    private int plazaMax;
    private String monitorId;
    private ArrayList<String> asistentes = new ArrayList<>(); // ids de cliente

    public Sesiones(String id, String claseId, LocalDate fecha, int plazaMax, String monitorId) {
        this.id = id;
        this.claseId = claseId;
        this.fecha = fecha;
        this.plazaMax = plazaMax;
        this.monitorId = monitorId;
    }

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

    public boolean hayPlazas() {
        return asistentes.size() < plazaMax;
    }

    public boolean apuntar(String clienteId) {
        if (!hayPlazas() || asistentes.contains(clienteId)) {
            return false;
        }
        return asistentes.add(clienteId);
    }

    public boolean desapuntar(String clienteId) {
        return asistentes.remove(clienteId);
    }
    
    @Override
    public String toString() {
        return "Sesiones{" + "id=" + id + ", claseId=" + claseId + ", fecha=" + fecha + ", plazaMax=" + plazaMax + ", monitorId=" + monitorId + ", asistentes=" + asistentes + '}';
    }
}
