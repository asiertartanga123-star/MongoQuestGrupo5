package com.mycompany.controlador.dao;

/**
 * Error de la capa de acceso a datos.
 * <p>
 * El DAO traduce las excepciones del driver de MongoDB a esta excepción (o a una de sus
 * subclases) para que la capa de interfaz no dependa de MongoDB: los controladores solo
 * conocen {@code DatosException}, {@link DuplicadoException}, {@link ValidacionException}
 * y {@link ConexionException}.
 */
public class DatosException extends RuntimeException {

    /** Tipo de operación que falló (por ejemplo {@code "INSERT Clientes"}). */
    private final String operacion;

    /**
     * Crea la excepción.
     *
     * @param operacion tipo de operación que falló
     * @param mensaje   descripción del error, pensada para mostrarla al usuario
     * @param causa     excepción original del driver
     */
    public DatosException(String operacion, String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.operacion = operacion;
    }

    /**
     * Devuelve el tipo de operación que falló.
     *
     * @return el tipo de operación, por ejemplo {@code "DELETE Clases"}
     */
    public String getOperacion() {
        return operacion;
    }
}
