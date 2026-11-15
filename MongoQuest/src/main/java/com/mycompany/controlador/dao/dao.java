package com.mycompany.controlador.dao;

import java.util.List;
import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * Define el contrato de acceso a la capa de persistencia para clientes y monitores.
 */
public interface dao {

    /**
     * Comprueba las credenciales de un usuario. Busca primero entre los trabajadores y después entre los clientes.
     *
     * @param mail correo electrónico del usuario
     * @param contraseña contraseña del usuario
     * @return el tipo del trabajador ({@code "gerente"} o {@code "monitor"}), {@code "cliente"} si coincide con un cliente, o {@code null} si no hay coincidencia
     */
    String validarLogin(String mail, String contraseña);

    /**
     * Comprueba si ya hay un cliente o un trabajador registrado con ese correo.
     *
     * @param mail correo electrónico a buscar
     * @return {@code true} si el correo ya existe
     */
    boolean existeMail(String mail);

    /**
     * Comprueba que un correo electrónico tenga un formato válido. Se ignoran las mayúsculas y los espacios de los extremos.
     *
     * @param mail correo electrónico a validar
     * @return {@code true} si el formato es válido; {@code false} si no lo es o si es {@code null}
     */
    boolean mailValido(String mail);

    /**
     * Obtiene todos los clientes de la base de datos.
     *
     * @return lista de documentos de la colección {@code Clientes}
     */
    List<Document> listarClientes();

    /**
     * Inserta un cliente nuevo.
     *
     * @param cliente documento con los datos del cliente
     */
    void insertarCliente(Document cliente);

    /**
     * Actualiza el nombre, el apellido, el DNI, el correo y la membresía de un cliente.
     *
     * @param id identificador (_id) del cliente a modificar
     * @param nombre nuevo nombre
     * @param apellido nuevo apellido
     * @param dni nuevo DNI
     * @param mail nuevo correo electrónico
     * @param membresia nueva membresía
     */
    void actualizarCliente(ObjectId id, String nombre, String apellido,
            String dni, String mail, String membresia);

    /**
     * Elimina un cliente.
     *
     * @param id identificador (_id) del cliente
     * @return {@code true} si se ha eliminado algún documento
     */
    boolean eliminarCliente(ObjectId id);

    /**
     * Obtiene los trabajadores cuyo tipo es monitor.
     *
     * @return lista de documentos de la colección {@code Trabajadores}
     */
    List<Document> listarMonitores();

    /**
     * Inserta un monitor nuevo.
     *
     * @param monitor documento con los datos del monitor
     */
    void insertarMonitor(Document monitor);

    /**
     * Actualiza el nombre, el apellido, el DNI, el correo y la dirección de un monitor.
     *
     * @param id identificador (_id) del monitor a modificar
     * @param nombre nuevo nombre
     * @param apellido nuevo apellido
     * @param dni nuevo DNI
     * @param mail nuevo correo electrónico
     * @param direccion nueva dirección, como lista de documentos con calle, cp y ciudad
     */
    void actualizarMonitor(ObjectId id, String nombre, String apellido,
            String dni, String mail, List<Document> direccion);

    /**
     * Elimina un monitor.
     *
     * @param id identificador (_id) del monitor
     * @return {@code true} si se ha eliminado algún documento
     */
    boolean eliminarMonitor(ObjectId id);

    // ---------- VENTANA CLIENTE ----------
    /**
     * Busca un cliente por su correo electrónico.
     *
     * @param mail correo electrónico del cliente
     * @return documento del cliente, o {@code null} si no existe
     */
    Document buscarClientePorMail(String mail);

    /**
     * Obtiene todas las clases del gimnasio.
     *
     * @return lista de documentos de la colección {@code Clases}
     */
    List<Document> listarClases();

    /**
     * Busca la sesión más reciente (por fecha) de una clase.
     *
     * @param claseId identificador (_id) de la clase
     * @return documento de la sesión, o {@code null} si la clase no tiene sesiones
     */
    Document ultimaSesionDeClase(ObjectId claseId);

    /**
     * Apunta a un cliente a una clase, añadiéndola a su lista de clases.
     *
     * @param mail correo electrónico del cliente
     * @param claseId identificador (_id) de la clase
     * @param nombreClase nombre de la clase
     * @return {@code true} si se ha modificado el cliente
     */
    boolean apuntarseAClase(String mail, ObjectId claseId, String nombreClase);

    /**
     * Concede un logro a un cliente con la fecha de hoy, siempre que todavía no lo tenga.
     *
     * @param mail correo electrónico del cliente
     * @param nombreLogro nombre del logro
     * @param semanas semanas de antigüedad asociadas al logro
     * @return {@code true} si el logro se ha añadido
     */
    boolean darLogro(String mail, String nombreLogro, int semanas);

    /**
     * Calcula las semanas que lleva el cliente en el gimnasio y le concede los logros que le corresponden por esa antigüedad. No hace nada si el cliente no existe.
     *
     * @param mail correo electrónico del cliente
     */
    void revisarLogros(String mail);
}