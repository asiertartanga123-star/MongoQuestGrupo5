package com.mycompany.controlador.dao;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

public class daoImplementacio implements dao {

    private static final String URI = "mongodb://localhost:27017";
    private static final String BD = "Tolotolo";

    private static final Pattern MAIL_VALIDO =
            Pattern.compile("^[a-z0-9._%+-]+@[a-z0-9-]+(\\.[a-z0-9-]+)*\\.[a-z]{2,}$");

    // Logros automáticos: al llegar a estas semanas seguidas se da el logro
    private static final String[] LOGROS_NOMBRES = {"Constante", "Imparable", "Leyenda"};
    private static final int[] LOGROS_SEMANAS = {4, 8, 12};

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

    // ---------- MONITORES ----------

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

    // ---------- VENTANA CLIENTE ----------

    // Busca un cliente por su mail (el que usó para iniciar sesión)
    @Override
    public Document buscarClientePorMail(String mail) {
        try (MongoClient cliente = MongoClients.create(URI)) {
            return cliente.getDatabase(BD)
                    .getCollection("Clientes")
                    .find(Filters.eq("mail", mail))
                    .first();
        }
    }

    // Todas las clases del gimnasio
    @Override
    public List<Document> listarClases() {
        try (MongoClient cliente = MongoClients.create(URI)) {
            return cliente.getDatabase(BD)
                    .getCollection("Clases")
                    .find()
                    .into(new ArrayList<Document>());
        }
    }

    // La sesión más reciente de una clase (de ahí sale el "n/n" de plazas)
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

    // Apunta al cliente a una clase: la añade al array "clases" del cliente
    @Override
    public boolean apuntarseAClase(String mail, ObjectId claseId, String nombreClase) {
        try (MongoClient cliente = MongoClients.create(URI)) {
            Document nueva = new Document("id_clase", claseId)
                    .append("nombre", nombreClase);
            return cliente.getDatabase(BD)
                    .getCollection("Clientes")
                    .updateOne(Filters.eq("mail", mail), Updates.push("clases", nueva))
                    .getModifiedCount() > 0;
        }
    }

    // ---------- MAIL ----------

    @Override
    public boolean existeMail(String mail) {
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

    // ---------- LOGROS ----------

    // Da un logro al cliente si todavía no lo tiene
    @Override
    public boolean darLogro(String mail, String nombreLogro, int semanas) {
        try (MongoClient cliente = MongoClients.create(URI)) {
            Document logro = new Document("nombre", nombreLogro)
                    .append("fecha", LocalDate.now().toString())
                    .append("semanas", semanas);
            return cliente.getDatabase(BD)
                    .getCollection("Clientes")
                    .updateOne(
                            Filters.and(Filters.eq("mail", mail),
                                    Filters.ne("logros.nombre", nombreLogro)),
                            Updates.push("logros", logro))
                    .getModifiedCount() > 0;
        }
    }

    // Mira la racha del cliente y le da los logros que se haya ganado
    @Override
    public void revisarLogros(String mail) {
        Document cliente = buscarClientePorMail(mail);
        if (cliente == null) {
            return;
        }

        int racha = rachaSemanas(cliente);
        for (int i = 0; i < LOGROS_NOMBRES.length; i++) {
            if (racha >= LOGROS_SEMANAS[i]) {
                darLogro(mail, LOGROS_NOMBRES[i], LOGROS_SEMANAS[i]);
            }
        }
    }

    // Semanas seguidas (hasta la semana actual) en las que el cliente tiene alguna sesión
    private int rachaSemanas(Document cliente) {
        Set<LocalDate> semanasConClase = new HashSet<LocalDate>();

        try (MongoClient mongo = MongoClients.create(URI)) {
            List<Document> sesiones = mongo.getDatabase(BD)
                    .getCollection("Sesiones")
                    .find()
                    .into(new ArrayList<Document>());

            for (Document sesion : sesiones) {
                List<Document> asistentes = sesion.getList("asistentes", Document.class);
                if (asistentes == null) {
                    continue;
                }
                for (Document a : asistentes) {
                    if (esDelCliente(a, cliente)) {
                        LocalDate dia = aFecha(sesion.get("fecha"));
                        if (dia != null) {
                            // Guardamos el lunes de esa semana
                            semanasConClase.add(dia.with(DayOfWeek.MONDAY));
                        }
                        break;
                    }
                }
            }
        }

        // Contamos hacia atrás desde la semana actual
        // (si esta semana aún no ha ido, la racha cuenta desde la anterior)
        LocalDate semana = LocalDate.now().with(DayOfWeek.MONDAY);
        if (!semanasConClase.contains(semana)) {
            semana = semana.minusWeeks(1);
        }
        int racha = 0;
        while (semanasConClase.contains(semana)) {
            racha++;
            semana = semana.minusWeeks(1);
        }
        return racha;
    }

    // ¿Este asistente es el cliente? (ajusta aquí el campo si tus asistentes son distintos)
    private boolean esDelCliente(Document asistente, Document cliente) {
        ObjectId id = cliente.getObjectId("_id");
        String mail = cliente.getString("mail");
        return mail.equals(asistente.get("mail"))
                || id.equals(asistente.get("id_cliente"))
                || id.equals(asistente.get("clienteid"))
                || id.equals(asistente.get("_id"));
    }

    // Convierte la fecha de la sesión (Date o texto "2026-11-09...") en LocalDate
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
}