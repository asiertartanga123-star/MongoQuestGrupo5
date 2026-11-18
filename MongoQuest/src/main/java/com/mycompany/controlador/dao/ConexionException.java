package com.mycompany.controlador.dao;

/**
 * No se puede conectar con el servidor de MongoDB (apagado, puerto equivocado o tiempo de espera agotado).
 */
public class ConexionException extends DatosException {

    /**
     * Crea la excepción.
     *
     * @param operacion tipo de operación que falló
     * @param causa     excepción original del driver
     */
    public ConexionException(String operacion, Throwable causa) {
        super(operacion, "No se puede conectar con MongoDB. Comprueba que esté iniciado.", causa);
    }
}
