/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Ekain.Bedoya
 */
public class Direccion {
    
    private String calle;
    private int CP;
    private String ciudad;

    public Direccion(String calle, int CP, String ciudad) {
        this.calle = calle;
        this.CP = CP;
        this.ciudad = ciudad;
    }

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
        return "Direccion{" + "calle=" + calle + ", CP=" + CP + ", ciudad=" + ciudad + '}';
    }
}
