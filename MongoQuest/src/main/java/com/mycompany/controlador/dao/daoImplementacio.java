package com.mycompany.controlador.dao;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;

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
}