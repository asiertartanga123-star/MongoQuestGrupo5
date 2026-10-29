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

    @Override
    public String validarLogin(String mail, String contraseña) {
        try (MongoClient cliente = MongoClients.create("mongodb://localhost:27017")) {
            MongoDatabase db = cliente.getDatabase("Tolotolo");
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

    // añade estos imports: java.util.ArrayList, java.util.List, org.bson.types.ObjectId
    @Override
    public List<Document> listarClientes() {
        try (MongoClient cliente = MongoClients.create("mongodb://localhost:27017")) {
            return cliente.getDatabase("Tolotolo")
                    .getCollection("Clientes")
                    .find()
                    .into(new ArrayList<>());
        }
    }

    @Override
    public boolean eliminarCliente(ObjectId id) {
        try (MongoClient cliente = MongoClients.create("mongodb://localhost:27017")) {
            return cliente.getDatabase("Tolotolo")
                    .getCollection("Clientes")
                    .deleteOne(Filters.eq("_id", id))
                    .getDeletedCount() > 0;
        }
    }

    @Override
    public void actualizarCliente(ObjectId id, String nombre, String apellido,
            String dni, String mail, String membresia) {
        try (MongoClient cliente = MongoClients.create("mongodb://localhost:27017")) {
            cliente.getDatabase("tolotolo")
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
    public void insertarCliente(Document c) {
        try (MongoClient cliente = MongoClients.create("mongodb://localhost:27017")) {
            cliente.getDatabase("tolotolo")
                    .getCollection("Clientes")
                    .insertOne(c);
        }
    }
}
