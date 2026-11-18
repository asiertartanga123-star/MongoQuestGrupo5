package com.mycompany.controlador.dao;

/**
 * El documento no cumple el esquema de validación ($jsonSchema) de su colección
 * (código 121 de MongoDB): falta un campo obligatorio o tiene un tipo incorrecto.
 */
public class ValidacionException extends DatosException {

    /**
     * Crea la excepción.
     *
     * @param operacion tipo de operación que falló
     * @param causa     excepción original del driver
     */
    public ValidacionException(String operacion, Throwable causa) {
        super(operacion, "Los datos no cumplen las reglas de la base de datos (faltan campos o tienen un formato incorrecto).", causa);
    }
}
