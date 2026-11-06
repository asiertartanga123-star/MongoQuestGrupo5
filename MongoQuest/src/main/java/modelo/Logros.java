package com.mycompany.controlador;

// Una fila de la tabla de Logros
public class Logros {

    private String numero;
    private String nombre;
    private String fecha;
    private String semanas;

    public Logros (String numero, String nombre, String fecha, String semanas) {
        this.numero = numero;
        this.nombre = nombre;
        this.fecha = fecha;
        this.semanas = semanas;
    }

    public String getNumero() {
        return numero;
    }

    public String getNombre() {
        return nombre;
    }

    public String getFecha() {
        return fecha;
    }

    public String getSemanas() {
        return semanas;
    }
}
