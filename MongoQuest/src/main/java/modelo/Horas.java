/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import java.time.LocalDate;

/** Representa el día y el intervalo horario asignado a una clase. */
class Horas {
    
    /** El día de la clase. */
    private LocalDate dia;
    // Se guardan como texto los horarios de inicio y fin.
    /** La hora de inicio, en formato de texto. */
    private String horaIncio;
    /** La hora de fin, en formato de texto. */
    private String horaFin;

    /**
     * Crea un tramo horario para un día concreto.
     *
     * @param dia día en que se celebra la clase
     * @param horaIncio hora de inicio, en formato de texto
     * @param horaFin hora de fin, en formato de texto
     */
    public Horas(LocalDate dia, String horaIncio, String horaFin) {
        this.dia = dia;
        this.horaIncio = horaIncio;
        this.horaFin = horaFin;
    }

    // Getters y setters para los tres valores del horario.
    /**
     * Devuelve el día de la clase.
     *
     * @return el día de la clase
     */
    public LocalDate getDia() {
        return dia;
    }

    /**
     * Devuelve la hora de inicio, en formato de texto.
     *
     * @return la hora de inicio, en formato de texto
     */
    public String getHoraIncio() {
        return horaIncio;
    }

    /**
     * Devuelve la hora de fin, en formato de texto.
     *
     * @return la hora de fin, en formato de texto
     */
    public String getHoraFin() {
        return horaFin;
    }

    /**
     * Establece el día de la clase.
     *
     * @param dia el día de la clase
     */
    public void setDia(LocalDate dia) {
        this.dia = dia;
    }

    /**
     * Establece la hora de inicio, en formato de texto.
     *
     * @param horaIncio la hora de inicio, en formato de texto
     */
    public void setHoraIncio(String horaIncio) {
        this.horaIncio = horaIncio;
    }

    /**
     * Establece la hora de fin, en formato de texto.
     *
     * @param horaFin la hora de fin, en formato de texto
     */
    public void setHoraFin(String horaFin) {
        this.horaFin = horaFin;
    }

    /**
     * Devuelve una representación legible del horario.
     *
     * @return texto con el día, la hora de inicio y la de fin
     */
    @Override
    public String toString() {
        // Representación legible del horario.
        return "Horas{" + "dia=" + dia + ", horaIncio=" + horaIncio + ", horaFin=" + horaFin + '}';
    }
}
