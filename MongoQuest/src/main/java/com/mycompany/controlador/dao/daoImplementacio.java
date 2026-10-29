package com.mycompany.controlador.dao;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

public class daoImplementacio implements dao {

    private static final String URI = "mongodb://localhost:27017";
    private static final String BD = "Tolotolo";

    @Override
    public String validarLogin(String mail, String contraseña) {
        try (MongoClient cliente = MongoClients.create(URI)) {
            MongoDatabase db = cliente.getDatabase(BD);
            Bson filtro = Filters.and(
                    Filters.eq("mail", mail),
                    Filters.eq("contraseña", contraseña)
            );

            Document trabajador = db.getCollection("Trabajadores").find(filtro).first();
            if (trabajador != null) {
                return trabajador.getString("tipo");   // "gerente" o "monitor"
            }

            Document cli = db.getCollection("Clientes").find(filtro).first();
            if (cli != null) {
                return "cliente";
            }

            return null;
        }
    }

    // ---------- CLIENTES ----------

    @Override
    public List<Document> listarClientes() {
        try (MongoClient cliente = MongoClients.create(URI)) {
            return cliente.getDatabase(BD)
                    .getCollection("Clientes")
                    .find()
                    .into(new ArrayList<Document>());
        }
    }

    @Override
    public void insertarCliente(Document c) {
        try (MongoClient cliente = MongoClients.create(URI)) {
            cliente.getDatabase(BD)
                    .getCollection("Clientes")
                    .insertOne(c);
        }
    }

    @Override
    public void actualizarCliente(ObjectId id, String nombre, String apellido,
            String dni, String mail, String membresia) {
        try (MongoClient cliente = MongoClients.create(URI)) {
            cliente.getDatabase(BD)
                    .getCollection("Clientes")
                    .updateOne(Filters.eq("_id", id),
                            Updates.combine(
                                    Updates.set("nombre", nombre),
                                    Updates.set("apellido", apellido),
                                    Updates.set("dni", dni),
                                    Updates.set("mail", mail),
                                    Updates.set("membresia", membresia)));
        }
    }

    @Override
    public boolean eliminarCliente(ObjectId id) {
        try (MongoClient cliente = MongoClients.create(URI)) {
            return cliente.getDatabase(BD)
                    .getCollection("Clientes")
                    .deleteOne(Filters.eq("_id", id))
                    .getDeletedCount() > 0;
        }
    }

    

    @Override
    public List<Document> listarMonitores() {
        try (MongoClient cliente = MongoClients.create(URI)) {
            return cliente.getDatabase(BD)
                    .getCollection("Trabajadores")
                    .find(Filters.in("tipo", "monitor"))
                    .into(new ArrayList<Document>());
        }
    }

    @Override
    public void insertarMonitor(Document m) {
        try (MongoClient cliente = MongoClients.create(URI)) {
            cliente.getDatabase(BD)
                    .getCollection("Trabajadores")
                    .insertOne(m);
        }
    }

    @Override
    public void actualizarMonitor(ObjectId id, String nombre, String apellido,
            String dni, String mail, List<Document> direccion) {
        try (MongoClient cliente = MongoClients.create(URI)) {
            cliente.getDatabase(BD)
                    .getCollection("Trabajadores")
                    .updateOne(Filters.eq("_id", id),
                            Updates.combine(
                                    Updates.set("nombre", nombre),
                                    Updates.set("apellido", apellido),
                                    Updates.set("dni", dni),
                                    Updates.set("mail", mail),
                                    Updates.set("direccion", direccion)));
        }
    }

    @Override
    public boolean eliminarMonitor(ObjectId id) {
        try (MongoClient cliente = MongoClients.create(URI)) {
            return cliente.getDatabase(BD)
                    .getCollection("Trabajadores")
                    .deleteOne(Filters.eq("_id", id))
                    .getDeletedCount() > 0;
        }
    }
}