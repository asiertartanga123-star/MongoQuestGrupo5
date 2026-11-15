/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/** Agrupa los datos postales de una persona o instalación. */
public class Direccion {
    
    /** La calle. */
    private String calle;
    // Código postal numérico y municipio de la dirección.
    /** El código postal. */
    private int CP;
    /** La ciudad. */
    private String ciudad;

    /**
     * Construye una dirección con calle, código postal y ciudad.
     *
     * @param calle nombre de la calle
     * @param CP código postal
     * @param ciudad ciudad o municipio
     */
    public Direccion(String calle, int CP, String ciudad) {
        this.calle = calle;
        this.CP = CP;
        this.ciudad = ciudad;
    }

    // Getters y setters para consultar o actualizar los componentes postales.
    /**
     * Devuelve la calle.
     *
     * @return la calle
     */
    public String getCalle() {
        return calle;
    }

    /**
     * Devuelve el código postal.
     *
     * @return el código postal
     */
    public int getCP() {
        return CP;
    }

    /**
     * Devuelve la ciudad.
     *
     * @return la ciudad
     */
    public String getCiudad() {
        return ciudad;
    }

    /**
     * Establece la calle.
     *
     * @param calle la calle
     */
    public void setCalle(String calle) {
        this.calle = calle;
    }

    /**
     * Establece el código postal.
     *
     * @param CP el código postal
     */
    public void setCP(int CP) {
        this.CP = CP;
    }

    /**
     * Establece la ciudad.
     *
     * @param ciudad la ciudad
     */
    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    /**
     * Devuelve la dirección en formato legible.
     *
     * @return texto con la calle, el código postal y la ciudad
     */
    @Override
    public String toString() {
        // Presenta la dirección en formato legible.
        return "Direccion{" + "calle=" + calle + ", CP=" + CP + ", ciudad=" + ciudad + '}';
    }
}
