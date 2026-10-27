package com.mycompany.controlador.dao;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;

public class daoImplementacio implements dao {

    @Override
    public boolean validarLogin(String usuario, String password) {
        try (MongoClient cliente = MongoClients.create("mongodb://localhost:27017")) {
            MongoCollection<Document> trabajadores = cliente
                    .getDatabase("Tolotolo")
                    .getCollection("Trabajadores");

            Document trabajador = trabajadores.find(
                    Filters.and(
                            Filters.eq("usuario", usuario),
                            Filters.eq("password", password)
                    )
            ).first();

            return trabajador != null;
        }
    }
}