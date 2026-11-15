/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mycompany.controlador.dao;

import java.util.Date;
import java.util.List;
import org.bson.Document;
import org.bson.types.ObjectId;

/**
 *
 * @author Asier.Prieto
 */
public interface dao {

    String validarLogin(String usuario, String password);

    List<Document> listarClientes();

    boolean eliminarCliente(ObjectId id);

    void actualizarCliente(ObjectId id, String nombre, String apellido, String dni, String mail, String membresia);

    void insertarCliente(Document cliente);

    List<Document> listarMonitores();

    void insertarMonitor(Document monitor);

    void actualizarMonitor(ObjectId id, String nombre, String apellido, String dni, String mail, List<Document> direccion);

    boolean eliminarMonitor(ObjectId id);

    // ---------- CLASES / SALAS / SESIONES ----------
    List<Document> listarClases();

    void insertarClase(Document clase);

    void actualizarClase(ObjectId id, String nombre, ObjectId salaId);

    boolean eliminarClase(ObjectId id);

    List<Document> listarSalas();

    List<Document> listarSesiones(ObjectId claseId);

    void insertarSesion(Document sesion);

    void actualizarSesion(ObjectId id, Date fecha, int plazaMax, ObjectId monitorId);

    boolean eliminarSesion(ObjectId id);
}
