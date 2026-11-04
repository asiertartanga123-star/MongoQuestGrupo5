package com.mycompany.controlador.dao;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

public class daoImplementacio implements dao {

    // En esta versión la instancia MongoDB es local y usa la base de datos Tolotolo.
    private static final String URI = "mongodb://localhost:27017";
    private static final String BD = "Tolotolo";

    // texto@dominio.extensión, sin espacios ni caracteres raros
    private static final Pattern MAIL_VALIDO =
            Pattern.compile("^[a-z0-9._%+-]+@[a-z0-9-]+(\\.[a-z0-9-]+)*\\.[a-z]{2,}$");

    @Override
    public String validarLogin(String mail, String contraseña) {
        // Cada operación abre y cierra su cliente Mongo para liberar recursos.
        try (MongoClient cliente = MongoClients.create(URI)) {
            MongoDatabase db = cliente.getDatabase(BD);
            // Trabajadores conserva su rol; los clientes reciben el rol fijo "cliente".
            Bson filtro = Filters.and(
                    Filters.eq("mail", mail),
                    Filters.eq("contraseña", contraseña)
            );

            Document trabajador = db.getCollection("Trabajadores").find(filtro).first();
            if (trabajador != null) {
                return trabajador.getString("tipo");   // "gerente" o "monitor"
            }

            // Si no se encuentra entre los trabajadores, se busca en la colección Clientes.
            Document cli = db.getCollection("Clientes").find(filtro).first();
            if (cli != null) {
                return "cliente";
            }

            return null;
        }
    }

    // ---------- Validaciones para el registro ----------

    @Override
    public boolean existeMail(String mail) {
        // El mail debe ser único entre clientes y trabajadores.
        try (MongoClient cliente = MongoClients.create(URI)) {
            MongoDatabase db = cliente.getDatabase(BD);
            return db.getCollection("Clientes").find(Filters.eq("mail", mail)).first() != null
                    || db.getCollection("Trabajadores").find(Filters.eq("mail", mail)).first() != null;
        }
    }

    @Override
    public boolean mailValido(String mail) {
        return mail != null && MAIL_VALIDO.matcher(mail.trim().toLowerCase()).matches();
    }

    // ---------- Operaciones de persistencia para clientes ----------

    @Override
    public List<Document> listarClientes() {
        // Convierte el cursor de Mongo en una lista que puede consumir la vista.
        try (MongoClient cliente = MongoClients.create(URI)) {
            return cliente.getDatabase(BD)
                    .getCollection("Clientes")
                    .find()
                    .into(new ArrayList<Document>());
        }
    }

    @Override
    public void insertarCliente(Document c) {
        // Inserta el documento preparado por el formulario de alta.
        try (MongoClient cliente = MongoClients.create(URI)) {
            cliente.getDatabase(BD)
                    .getCollection("Clientes")
                    .insertOne(c);
        }
    }

    @Override
    public void actualizarCliente(ObjectId id, String nombre, String apellido,
            String dni, String mail, String membresia) {
        // Actualiza únicamente los campos editables, localizando el documento por _id.
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
        // Indica si MongoDB encontró y eliminó al menos un documento.
        try (MongoClient cliente = MongoClients.create(URI)) {
            return cliente.getDatabase(BD)
                    .getCollection("Clientes")
                    .deleteOne(Filters.eq("_id", id))
                    .getDeletedCount() > 0;
        }
    }

    // ---------- Operaciones de persistencia para monitores ----------

    @Override
    public List<Document> listarMonitores() {
        // Los monitores comparten colección con otros trabajadores y se filtran por tipo.
        try (MongoClient cliente = MongoClients.create(URI)) {
            return cliente.getDatabase(BD)
                    .getCollection("Trabajadores")
                    .find(Filters.eq("tipo", "monitor"))
                    .into(new ArrayList<Document>());
        }
    }

    @Override
    public void insertarMonitor(Document m) {
        // Los nuevos monitores se guardan en la colección general de trabajadores.
        try (MongoClient cliente = MongoClients.create(URI)) {
            cliente.getDatabase(BD)
                    .getCollection("Trabajadores")
                    .insertOne(m);
        }
    }

    @Override
    public void actualizarMonitor(ObjectId id, String nombre, String apellido,
            String dni, String mail, List<Document> direccion) {
        // La dirección se almacena como una lista de documentos BSON.
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
        // El ObjectId identifica de forma única al trabajador que se quiere borrar.
        try (MongoClient cliente = MongoClients.create(URI)) {
            return cliente.getDatabase(BD)
                    .getCollection("Trabajadores")
                    .deleteOne(Filters.eq("_id", id))
                    .getDeletedCount() > 0;
        }
    }
}