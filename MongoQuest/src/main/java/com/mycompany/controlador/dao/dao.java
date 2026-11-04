package com.mycompany.controlador.dao;

import java.util.List;
import org.bson.Document;
import org.bson.types.ObjectId;

public interface dao {

    String validarLogin(String mail, String contraseña);

    // Validaciones para el registro
    boolean existeMail(String mail);
    boolean mailValido(String mail);

    // Clientes
    List<Document> listarClientes();
    void insertarCliente(Document cliente);
    void actualizarCliente(ObjectId id, String nombre, String apellido,
            String dni, String mail, String membresia);
    boolean eliminarCliente(ObjectId id);

    // Monitores
    List<Document> listarMonitores();
    void insertarMonitor(Document monitor);
    void actualizarMonitor(ObjectId id, String nombre, String apellido,
            String dni, String mail, List<Document> direccion);
    boolean eliminarMonitor(ObjectId id);
}