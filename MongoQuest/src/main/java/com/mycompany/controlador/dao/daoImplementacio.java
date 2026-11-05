package com.mycompany.controlador.dao;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mycompany.controlador.Registro;
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
                Registro.operacion("SELECT Trabajadores", "login de " + mail);
                return trabajador.getString("tipo");   // "gerente" o "monitor"
            }

            // Si no se encuentra entre los trabajadores, se busca en la colección Clientes.
            Document cli = db.getCollection("Clientes").find(filtro).first();
            if (cli != null) {
                Registro.operacion("SELECT Clientes", "login de " + mail);
                return "cliente";
            }

            Registro.operacion("SELECT Login", "sin coincidencias para " + mail);
            return null;
        } catch (RuntimeException e) {
            Registro.operacionError("SELECT Login", "login de " + mail, e);
            throw e;
        }
    }

    // ---------- Validaciones para el registro ----------

    
    public boolean existeMail(String mail) {
        // El mail debe ser único entre clientes y trabajadores.
        try (MongoClient cliente = MongoClients.create(URI)) {
            MongoDatabase db = cliente.getDatabase(BD);
            boolean existe = db.getCollection("Clientes").find(Filters.eq("mail", mail)).first() != null
                    || db.getCollection("Trabajadores").find(Filters.eq("mail", mail)).first() != null;
            Registro.operacion("SELECT Clientes/Trabajadores", "existe mail " + mail + ": " + existe);
            return existe;
        } catch (RuntimeException e) {
            Registro.operacionError("SELECT Clientes/Trabajadores", "existe mail " + mail, e);
            throw e;
        }
    }


    public boolean mailValido(String mail) {
        return mail != null && MAIL_VALIDO.matcher(mail.trim().toLowerCase()).matches();
    }

    // ---------- Operaciones de persistencia para clientes ----------

    @Override
    public List<Document> listarClientes() {
        // Convierte el cursor de Mongo en una lista que puede consumir la vista.
        try (MongoClient cliente = MongoClients.create(URI)) {
            List<Document> lista = cliente.getDatabase(BD)
                    .getCollection("Clientes")
                    .find()
                    .into(new ArrayList<Document>());
            Registro.operacion("SELECT Clientes", "listado (" + lista.size() + " registros)");
            return lista;
        } catch (RuntimeException e) {
            Registro.operacionError("SELECT Clientes", "listado", e);
            throw e;
        }
    }

    @Override
    public void insertarCliente(Document c) {
        // Inserta el documento preparado por el formulario de alta.
        try (MongoClient cliente = MongoClients.create(URI)) {
            cliente.getDatabase(BD)
                    .getCollection("Clientes")
                    .insertOne(c);
            Registro.operacion("INSERT Clientes", "dni " + c.getString("dni"));
        } catch (RuntimeException e) {
            Registro.operacionError("INSERT Clientes", "dni " + c.getString("dni"), e);
            throw e;
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
            Registro.operacion("UPDATE Clientes", "id " + id + " (dni " + dni + ")");
        } catch (RuntimeException e) {
            Registro.operacionError("UPDATE Clientes", "id " + id, e);
            throw e;
        }
    }

    @Override
    public boolean eliminarCliente(ObjectId id) {
        // Indica si MongoDB encontró y eliminó al menos un documento.
        try (MongoClient cliente = MongoClients.create(URI)) {
            boolean eliminado = cliente.getDatabase(BD)
                    .getCollection("Clientes")
                    .deleteOne(Filters.eq("_id", id))
                    .getDeletedCount() > 0;
            Registro.operacion("DELETE Clientes", "id " + id + " (eliminado: " + eliminado + ")");
            return eliminado;
        } catch (RuntimeException e) {
            Registro.operacionError("DELETE Clientes", "id " + id, e);
            throw e;
        }
    }

    // ---------- Operaciones de persistencia para monitores ----------

    @Override
    public List<Document> listarMonitores() {
        // Los monitores comparten colección con otros trabajadores y se filtran por tipo.
        try (MongoClient cliente = MongoClients.create(URI)) {
            List<Document> lista = cliente.getDatabase(BD)
                    .getCollection("Trabajadores")
                    .find(Filters.eq("tipo", "monitor"))
                    .into(new ArrayList<Document>());
            Registro.operacion("SELECT Trabajadores", "listado de monitores (" + lista.size() + " registros)");
            return lista;
        } catch (RuntimeException e) {
            Registro.operacionError("SELECT Trabajadores", "listado de monitores", e);
            throw e;
        }
    }

    @Override
    public void insertarMonitor(Document m) {
        // Los nuevos monitores se guardan en la colección general de trabajadores.
        try (MongoClient cliente = MongoClients.create(URI)) {
            cliente.getDatabase(BD)
                    .getCollection("Trabajadores")
                    .insertOne(m);
            Registro.operacion("INSERT Trabajadores", "dni " + m.getString("dni"));
        } catch (RuntimeException e) {
            Registro.operacionError("INSERT Trabajadores", "dni " + m.getString("dni"), e);
            throw e;
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
            Registro.operacion("UPDATE Trabajadores", "id " + id + " (dni " + dni + ")");
        } catch (RuntimeException e) {
            Registro.operacionError("UPDATE Trabajadores", "id " + id, e);
            throw e;
        }
    }

    @Override
    public boolean eliminarMonitor(ObjectId id) {
        // El ObjectId identifica de forma única al trabajador que se quiere borrar.
        try (MongoClient cliente = MongoClients.create(URI)) {
            boolean eliminado = cliente.getDatabase(BD)
                    .getCollection("Trabajadores")
                    .deleteOne(Filters.eq("_id", id))
                    .getDeletedCount() > 0;
            Registro.operacion("DELETE Trabajadores", "id " + id + " (eliminado: " + eliminado + ")");
            return eliminado;
        } catch (RuntimeException e) {
            Registro.operacionError("DELETE Trabajadores", "id " + id, e);
            throw e;
        }
    }
}