/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mycompany.controlador.dao;

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
    
    boolean mailValido(String mail);
    
    boolean existeMail(String mail);


    // ---------- VENTANA CLIENTE ----------

    Document buscarClientePorMail(String mail);

    List<Document> listarClases();

    Document ultimaSesionDeClase(ObjectId claseId);
    
    boolean apuntarseAClase(String mail, ObjectId claseId, String nombreClase);
    
    boolean darLogro(String mail, String nombreLogro, int semanas);

    void revisarLogros(String mail);
}