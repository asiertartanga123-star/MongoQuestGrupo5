package com.mycompany.controlador.dao;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;

public class daoImplementacio implements dao {

    // Colección donde están los trabajadores
    private final MongoCollection<Document> trabajadores;

    public daoImplementacio() {
        // Conexión a MongoDB local
        MongoClient client;
        client = MongoClients.create("mongodb://localhost:27017");

        // Base de datos "Tolotolo" y colección "Trabajadores"
        trabajadores = client.getDatabase("Tolotolo").getCollection("Trabajadores");
    }

    @Override
    public boolean validarLogin(String usuario, String password) {
        // Busca un documento que tenga ese usuario Y esa contraseña.
        // IMPORTANTE: cambia "usuario" y "password" por los nombres
        // reales de los campos que tengas en Compass.
        Document doc = trabajadores.find(
                Filters.and(
                        Filters.eq("usuario", usuario),
                        Filters.eq("password", password)
                )
        ).first();

        // Si no encuentra nada, doc es null y el login es incorrecto
        return doc != null;
    }
}