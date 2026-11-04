/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/** Agrupa los datos postales de una persona o instalación. */
public class Direccion {
    
    private String calle;
    // Código postal numérico y municipio de la dirección.
    private int CP;
    private String ciudad;

    /** Construye una dirección con calle, código postal y ciudad. */
    public Direccion(String calle, int CP, String ciudad) {
        this.calle = calle;
        this.CP = CP;
        this.ciudad = ciudad;
    }

    // Getters y setters para consultar o actualizar los componentes postales.
    public String getCalle() {
        return calle;
    }

    public int getCP() {
        return CP;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCalle(String calle) {
        this.calle = calle;
    }

    public void setCP(int CP) {
        this.CP = CP;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    @Override
    public String toString() {
        // Presenta la dirección en formato legible.
        return "Direccion{" + "calle=" + calle + ", CP=" + CP + ", ciudad=" + ciudad + '}';
    }
}
