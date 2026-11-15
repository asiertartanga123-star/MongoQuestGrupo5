package com.mycompany.controlador.dao;

import java.util.List;
import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * Define el contrato de acceso a la capa de persistencia de clientes y monitores.
 * <p>
 * Las implementaciones de esta interfaz gestionan la autenticación y las operaciones
 * CRUD sobre MongoDB para la aplicación de gestión del gimnasio.
 */
public interface dao {

    /**
     * Valida las credenciales de acceso de un usuario.
     *
     * @param mail correo electrónico introducido en el formulario de login
     * @param contraseña contraseña introducida por el usuario
     * @return rol del usuario autenticado ({@code gerente}, {@code monitor} o
     *         {@code cliente}); o {@code null} si no existe coincidencia
     */
    String validarLogin(String mail, String contraseña);

    /**
     * Comprueba si un correo ya existe en la base de datos.
     *
     * @param mail correo a comprobar
     * @return {@code true} si el correo está ya registrado en clientes o trabajadores
     */
    boolean existeMail(String mail);

    /**
     * Valida el formato del correo.
     *
     * @param mail correo electrónico a validar
     * @return {@code true} si la dirección cumple el patrón esperado
     */
    boolean mailValido(String mail);

    /**
     * Obtiene el listado completo de clientes almacenados.
     *
     * @return listado de documentos BSON con los datos del cliente
     */
    List<Document> listarClientes();

    /**
     * Inserta un nuevo cliente en la colección de clientes.
     *
     * @param cliente documento BSON con la información del cliente
     */
    void insertarCliente(Document cliente);

    /**
     * Actualiza los datos editables de un cliente existente.
     *
     * @param id identificador del cliente a modificar
     * @param nombre nuevo nombre
     * @param apellido nuevo apellido
     * @param dni nuevo documento nacional de identidad
     * @param mail nuevo correo electrónico
     * @param membresia nueva membresía del cliente
     */
    void actualizarCliente(ObjectId id, String nombre, String apellido,
            String dni, String mail, String membresia);

    /**
     * Elimina un cliente de la base de datos.
     *
     * @param id identificador del cliente
     * @return {@code true} si se eliminó al menos un documento
     */
    boolean eliminarCliente(ObjectId id);

    /**
     * Obtiene la lista de monitores registrados.
     *
     * @return documentos BSON con la información de cada monitor
     */
    List<Document> listarMonitores();

    /**
     * Inserta un monitor en la colección de trabajadores.
     *
     * @param monitor documento BSON con la información del monitor
     */
    void insertarMonitor(Document monitor);

    /**
     * Actualiza los datos editables de un monitor.
     *
     * @param id identificador del monitor
     * @param nombre nuevo nombre
     * @param apellido nuevo apellido
     * @param dni nuevo número de identificación
     * @param mail nuevo correo
     * @param direccion nueva dirección del monitor
     */
    void actualizarMonitor(ObjectId id, String nombre, String apellido,
            String dni, String mail, List<Document> direccion);

    /**
     * Elimina un monitor de la base de datos.
     *
     * @param id identificador del monitor
     * @return {@code true} si se ha borrado correctamente
     */
    boolean eliminarMonitor(ObjectId id);
}