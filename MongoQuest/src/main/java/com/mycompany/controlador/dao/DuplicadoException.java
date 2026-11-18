package com.mycompany.controlador.dao;

/**
 * Se intentó guardar un valor que tiene que ser único y ya existe
 * (por ejemplo un DNI o un correo repetido; índice único de MongoDB, código 11000).
 */
public class DuplicadoException extends DatosException {

    /**
     * Crea la excepción.
     *
     * @param operacion tipo de operación que falló
     * @param causa     excepción original del driver
     */
    public DuplicadoException(String operacion, Throwable causa) {
        super(operacion, "Ya existe un registro con ese DNI o ese correo.", causa);
    }
}
