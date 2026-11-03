/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mycompany.controlador.dao;

import java.util.List;
import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * Contrato entre los controladores y la capa que persiste datos en MongoDB.
 * Define las operaciones de autenticación y gestión disponibles.
 */
public interface dao {

    /** Devuelve el rol del usuario si las credenciales son válidas; si no, null. */
    String validarLogin(String usuario, String password);

    /** Recupera los documentos de la colección de clientes. */
    List<Document> listarClientes();

    /** Elimina el cliente identificado por su ObjectId. */
    boolean eliminarCliente(ObjectId id);

    /** Actualiza los campos editables de un cliente existente. */
    void actualizarCliente(ObjectId id, String nombre, String apellido, String dni, String mail, String membresia);

    /** Inserta un nuevo documento de cliente. */
    void insertarCliente(Document cliente);

    /** Recupera los trabajadores cuyo tipo es monitor. */
    List<Document> listarMonitores();

    /** Inserta un nuevo documento de monitor. */
    void insertarMonitor(Document monitor);

    /** Actualiza los datos personales y la dirección de un monitor. */
    void actualizarMonitor(ObjectId id, String nombre, String apellido, String dni, String mail, List<Document> direccion);

    /** Elimina el monitor identificado por su ObjectId. */
    boolean eliminarMonitor(ObjectId id);
}
