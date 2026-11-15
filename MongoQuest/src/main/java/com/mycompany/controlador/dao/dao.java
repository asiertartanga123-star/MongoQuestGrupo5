package com.mycompany.controlador.dao;

import java.util.Date;
import java.util.List;
import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * Define el contrato de acceso a la capa de persistencia para clientes y monitores.
 */
public interface dao {

    String validarLogin(String mail, String contraseña);

    boolean existeMail(String mail);

    boolean mailValido(String mail);

    List<Document> listarClientes();

    void insertarCliente(Document cliente);

    void actualizarCliente(ObjectId id, String nombre, String apellido,
            String dni, String mail, String membresia);

    boolean eliminarCliente(ObjectId id);

    List<Document> listarMonitores();

    void insertarMonitor(Document monitor);

    void actualizarMonitor(ObjectId id, String nombre, String apellido,
            String dni, String mail, List<Document> direccion);

    boolean eliminarMonitor(ObjectId id);

    // ---------- VENTANA CLIENTE ----------
    Document buscarClientePorMail(String mail);

    List<Document> listarClases();

    Document ultimaSesionDeClase(ObjectId claseId);

    boolean apuntarseAClase(String mail, ObjectId claseId, String nombreClase);

    boolean darLogro(String mail, String nombreLogro, int semanas);

    void revisarLogros(String mail);

    void insertarClase(Document clase);

    void actualizarClase(ObjectId id, String nombre, ObjectId salaId);

    boolean eliminarClase(ObjectId id);

    List<Document> listarSalas();

    List<Document> listarSesiones(ObjectId claseId);

    void insertarSesion(Document sesion);

    void actualizarSesion(ObjectId id, Date fecha, int plazaMax, ObjectId monitorId);

    boolean eliminarSesion(ObjectId id);
}
