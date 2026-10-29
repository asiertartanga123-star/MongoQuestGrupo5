/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import java.time.LocalDate;

/**
 *
 * @author Ekain.Bedoya
 */
class Horas {
    
    private LocalDate dia;
    private String horaIncio;
    private String horaFin;

    public Horas(LocalDate dia, String horaIncio, String horaFin) {
        this.dia = dia;
        this.horaIncio = horaIncio;
        this.horaFin = horaFin;
    }

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
        return "Horas{" + "dia=" + dia + ", horaIncio=" + horaIncio + ", horaFin=" + horaFin + '}';
    }
}
