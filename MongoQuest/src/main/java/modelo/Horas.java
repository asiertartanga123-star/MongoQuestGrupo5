/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import java.time.LocalDate;

/** Representa el día y el intervalo horario asignado a una clase. */
class Horas {
    
    private LocalDate dia;
    // Se guardan como texto los horarios de inicio y fin.
    private String horaIncio;
    private String horaFin;

    /** Crea un tramo horario para un día concreto. */
    public Horas(LocalDate dia, String horaIncio, String horaFin) {
        this.dia = dia;
        this.horaIncio = horaIncio;
        this.horaFin = horaFin;
    }

    // Getters y setters para los tres valores del horario.
    public LocalDate getDia() {
        return dia;
    }

    public String getHoraIncio() {
        return horaIncio;
    }

    public String getHoraFin() {
        return horaFin;
    }

    public void setDia(LocalDate dia) {
        this.dia = dia;
    }

    public void setHoraIncio(String horaIncio) {
        this.horaIncio = horaIncio;
    }

    public void setHoraFin(String horaFin) {
        this.horaFin = horaFin;
    }

    @Override
    public String toString() {
        // Representación legible del horario.
        return "Horas{" + "dia=" + dia + ", horaIncio=" + horaIncio + ", horaFin=" + horaFin + '}';
    }
}
