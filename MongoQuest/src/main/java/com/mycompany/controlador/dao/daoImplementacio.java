package com.mycompany.controlador.dao;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import com.mycompany.controlador.Registro;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

/**
 * Implementación concreta del acceso a datos para MongoDB.
 * <p>
 * Cada operación abre su propia conexión con el servidor y la cierra al terminar. Las operaciones de login, listados, altas, modificaciones y bajas se anotan en el log de operaciones mediante {@link Registro}.
 */
public class daoImplementacio implements dao {

    /** Dirección de conexión con el servidor MongoDB (local, puerto 27017). */
    private static final String URI = "mongodb://localhost:27017";
    /** Nombre de la base de datos que utiliza la aplicación. */
    private static final String BD = "Tolotolo";
    /** Expresión regular que define un correo válido; se aplica sobre el texto en minúsculas. */
    private static final Pattern MAIL_VALIDO =
            Pattern.compile("^[a-z0-9._%+-]+@[a-z0-9-]+(\\.[a-z0-9-]+)*\\.[a-z]{2,}$");
    /** Nombres de los logros que se pueden conseguir, de menor a mayor antigüedad. */
    private static final String[] LOGROS_NOMBRES = {"Constante", "Imparable", "Leyenda"};
    /** Semanas de antigüedad que exige cada logro; cada posición se corresponde con la de {@link #LOGROS_NOMBRES}. */
    private static final int[] LOGROS_SEMANAS = {4, 8, 12};

    @Override
    public String validarLogin(String mail, String contraseña) {
        try (MongoClient cliente = MongoClients.create(URI)) {
            MongoDatabase db = cliente.getDatabase(BD);
            Bson filtro = Filters.and(Filters.eq("mail", mail), Filters.eq("contraseña", contraseña));

            Document trabajador = db.getCollection("Trabajadores").find(filtro).first();
            if (trabajador != null) {
                Registro.operacion("SELECT Trabajadores", "login de " + mail);
                return trabajador.getString("tipo");
            }

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

    @Override
    public boolean existeMail(String mail) {
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

    @Override
    public boolean mailValido(String mail) {
        return mail != null && MAIL_VALIDO.matcher(mail.trim().toLowerCase()).matches();
    }

    @Override
    public List<Document> listarClientes() {
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
        try (MongoClient cliente = MongoClients.create(URI)) {
            cliente.getDatabase(BD).getCollection("Clientes").insertOne(c);
            Registro.operacion("INSERT Clientes", "dni " + c.getString("dni"));
        } catch (RuntimeException e) {
            Registro.operacionError("INSERT Clientes", "dni " + c.getString("dni"), e);
            throw e;
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
            Registro.operacion("UPDATE Clientes", "id " + id + " (dni " + dni + ")");
        } catch (RuntimeException e) {
            Registro.operacionError("UPDATE Clientes", "id " + id, e);
            throw e;
        }
    }

    @Override
    public boolean eliminarCliente(ObjectId id) {
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

    @Override
    public List<Document> listarMonitores() {
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
        try (MongoClient cliente = MongoClients.create(URI)) {
            cliente.getDatabase(BD).getCollection("Trabajadores").insertOne(m);
            Registro.operacion("INSERT Trabajadores", "dni " + m.getString("dni"));
        } catch (RuntimeException e) {
            Registro.operacionError("INSERT Trabajadores", "dni " + m.getString("dni"), e);
            throw e;
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
            Registro.operacion("UPDATE Trabajadores", "id " + id + " (dni " + dni + ")");
        } catch (RuntimeException e) {
            Registro.operacionError("UPDATE Trabajadores", "id " + id, e);
            throw e;
        }
    }

    @Override
    public boolean eliminarMonitor(ObjectId id) {
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

    @Override
    public Document buscarClientePorMail(String mail) {
        try (MongoClient cliente = MongoClients.create(URI)) {
            return cliente.getDatabase(BD)
                    .getCollection("Clientes")
                    .find(Filters.eq("mail", mail))
                    .first();
        }
    }

    @Override
    public List<Document> listarClases() {
        try (MongoClient cliente = MongoClients.create(URI)) {
            return cliente.getDatabase(BD)
                    .getCollection("Clases")
                    .find()
                    .into(new ArrayList<Document>());
        }
    }

    @Override
    public Document ultimaSesionDeClase(ObjectId claseId) {
        try (MongoClient cliente = MongoClients.create(URI)) {
            return cliente.getDatabase(BD)
                    .getCollection("Sesiones")
                    .find(Filters.eq("claseid", claseId))
                    .sort(Sorts.descending("fecha"))
                    .first();
        }
    }

    @Override
    public boolean apuntarseAClase(String mail, ObjectId claseId, String nombreClase) {
        try (MongoClient cliente = MongoClients.create(URI)) {
            Document nueva = new Document("id_clase", claseId).append("nombre", nombreClase);
            return cliente.getDatabase(BD)
                    .getCollection("Clientes")
                    .updateOne(Filters.eq("mail", mail), Updates.push("clases", nueva))
                    .getModifiedCount() > 0;
        }
    }

    @Override
    public boolean darLogro(String mail, String nombreLogro, int semanas) {
        try (MongoClient cliente = MongoClients.create(URI)) {
            Document logro = new Document("nombre", nombreLogro)
                    .append("fecha", LocalDate.now().toString())
                    .append("semanas", semanas);
            return cliente.getDatabase(BD)
                    .getCollection("Clientes")
                    .updateOne(
                            Filters.and(Filters.eq("mail", mail), Filters.ne("logros.nombre", nombreLogro)),
                            Updates.push("logros", logro))
                    .getModifiedCount() > 0;
        }
    }

    @Override
    public void revisarLogros(String mail) {
        Document cliente = buscarClientePorMail(mail);
        if (cliente == null) {
            return;
        }

        int semanas = semanasDesdeAlta(cliente);
        System.out.println("Semanas de " + mail + " en el gimnasio: " + semanas);

        for (int i = 0; i < LOGROS_NOMBRES.length; i++) {
            if (semanas >= LOGROS_SEMANAS[i]) {
                darLogro(mail, LOGROS_NOMBRES[i], LOGROS_SEMANAS[i]);
            }
        }
    }

    /**
     * Calcula las semanas completas transcurridas desde la fecha de alta del cliente.
     *
     * @param cliente documento del cliente
     * @return semanas de antigüedad, o 0 si el cliente no tiene una fecha de alta válida
     */
    private int semanasDesdeAlta(Document cliente) {
        LocalDate alta = aFecha(cliente.get("fechaAlta"));
        if (alta == null) {
            return 0;
        }
        return (int) ChronoUnit.WEEKS.between(alta, LocalDate.now());
    }

    /**
     * Convierte el valor guardado en {@code fechaAlta} en una fecha local. Acepta un {@code Date} de BSON o un texto que empiece por {@code yyyy-MM-dd}.
     *
     * @param valor valor leído de MongoDB
     * @return la fecha, o {@code null} si el valor es nulo o no se puede interpretar
     */
    private LocalDate aFecha(Object valor) {
        if (valor == null) {
            return null;
        }
        if (valor instanceof Date) {
            return ((Date) valor).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        try {
            String texto = valor.toString();
            return LocalDate.parse(texto.substring(0, 10));
        } catch (Exception e) {
            return null;
        }
    }

    // ---------- CLASES / SALAS / SESIONES ----------

    @Override
    public List<Document> listarClases() {
        try (MongoClient cliente = MongoClients.create(URI)) {
            return cliente.getDatabase(BD).getCollection("Clases").find().into(new ArrayList<Document>());
        }
    }

    @Override
    public void insertarClase(Document c) {
        try (MongoClient cliente = MongoClients.create(URI)) {
            cliente.getDatabase(BD).getCollection("Clases").insertOne(c);
        }
    }

    @Override
    public void actualizarClase(ObjectId id, String nombre, ObjectId salaId) {
        try (MongoClient cliente = MongoClients.create(URI)) {
            cliente.getDatabase(BD).getCollection("Clases").updateOne(Filters.eq("_id", id),
                    Updates.combine(Updates.set("nombre", nombre), Updates.set("salaid", salaId)));
        }
    }

    // Borra la clase y sus sesiones, para no dejar sesiones huérfanas
    @Override
    public boolean eliminarClase(ObjectId id) {
        try (MongoClient cliente = MongoClients.create(URI)) {
            MongoDatabase db = cliente.getDatabase(BD);
            db.getCollection("Sesiones").deleteMany(Filters.eq("claseid", id));
            return db.getCollection("Clases").deleteOne(Filters.eq("_id", id)).getDeletedCount() > 0;
        }
    }

    @Override
    public List<Document> listarSalas() {
        try (MongoClient cliente = MongoClients.create(URI)) {
            return cliente.getDatabase(BD).getCollection("Salas").find().into(new ArrayList<Document>());
        }
    }

    @Override
    public List<Document> listarSesiones(ObjectId claseId) {
        try (MongoClient cliente = MongoClients.create(URI)) {
            return cliente.getDatabase(BD).getCollection("Sesiones")
                    .find(Filters.eq("claseid", claseId))
                    .sort(new Document("fecha", 1))
                    .into(new ArrayList<Document>());
        }
    }

    @Override
    public void insertarSesion(Document s) {
        try (MongoClient cliente = MongoClients.create(URI)) {
            cliente.getDatabase(BD).getCollection("Sesiones").insertOne(s);
        }
    }

    @Override
    public void actualizarSesion(ObjectId id, Date fecha, int plazaMax, ObjectId monitorId) {
        try (MongoClient cliente = MongoClients.create(URI)) {
            cliente.getDatabase(BD).getCollection("Sesiones").updateOne(Filters.eq("_id", id),
                    Updates.combine(Updates.set("fecha", fecha), Updates.set("plazaMax", plazaMax),
                            Updates.set("monitor", monitorId)));
        }
    }

    @Override
    public boolean eliminarSesion(ObjectId id) {
        try (MongoClient cliente = MongoClients.create(URI)) {
            return cliente.getDatabase(BD).getCollection("Sesiones")
                    .deleteOne(Filters.eq("_id", id)).getDeletedCount() > 0;
        }
    }
}