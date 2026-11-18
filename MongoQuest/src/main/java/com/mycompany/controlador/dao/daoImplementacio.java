package com.mycompany.controlador.dao;

import com.mongodb.ErrorCategory;
import com.mongodb.MongoException;
import com.mongodb.MongoSocketException;
import com.mongodb.MongoTimeoutException;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.TransactionBody;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import com.mycompany.controlador.Registro;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

/**
 * Implementación concreta del acceso a datos para MongoDB.
 * <p>
 * Todas las instancias comparten un único {@link MongoClient} (que ya gestiona su propio
 * conjunto de conexiones) y se cierra al salir de la aplicación. Cada operación se anota en
 * el log de operaciones mediante {@link Registro}: fecha, usuario, tipo, detalle y resultado,
 * o la excepción si falla. Los errores del driver se traducen a {@link DatosException} y sus
 * subclases, que se propagan hasta los controladores.
 */
public class daoImplementacio implements dao {

    /** Dirección del servidor MongoDB; si no responde en 3 segundos se da por caído. */
    private static final String URI = "mongodb://localhost:27017/?serverSelectionTimeoutMS=3000";
    /** Nombre de la base de datos que utiliza la aplicación. */
    private static final String BD = "Tolotolo";
    /** Código de error de MongoDB cuando un documento no cumple el $jsonSchema. */
    private static final int ERROR_VALIDACION = 121;
    /** Expresión regular que define un correo válido; se aplica sobre el texto en minúsculas. */
    private static final Pattern MAIL_VALIDO =
            Pattern.compile("^[a-z0-9._%+-]+@[a-z0-9-]+(\\.[a-z0-9-]+)*\\.[a-z]{2,}$");
    /** Nombres de los logros que se pueden conseguir, de menor a mayor antigüedad. */
    private static final String[] LOGROS_NOMBRES = {"Constante", "Imparable", "Leyenda"};
    /** Semanas de antigüedad que exige cada logro; cada posición se corresponde con la de {@link #LOGROS_NOMBRES}. */
    private static final int[] LOGROS_SEMANAS = {4, 8, 12};

    /** Cliente de MongoDB compartido por toda la aplicación; se crea la primera vez que se usa. */
    private static MongoClient conexion;
    /** Si el servidor admite transacciones (solo en replica set); {@code null} hasta comprobarlo. */
    private static Boolean admiteTransacciones;

    // ---------- Conexión, errores y transacciones ----------

    /**
     * Devuelve la base de datos, creando la conexión compartida la primera vez.
     *
     * @return la base de datos {@code Tolotolo}
     */
    private static synchronized MongoDatabase bd() {
        if (conexion == null) {
            conexion = MongoClients.create(URI);
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    conexion.close();
                }
            }));
        }
        return conexion.getDatabase(BD);
    }

    /**
     * Anota el error en el log de operaciones y lo traduce a una excepción de la capa de datos.
     *
     * @param operacion tipo de operación que falló
     * @param detalle   datos de la operación para el log
     * @param e         excepción del driver
     * @return la excepción propia que hay que lanzar
     */
    private DatosException fallo(String operacion, String detalle, MongoException e) {
        Registro.operacionError(operacion, detalle, e);
        if (e instanceof MongoTimeoutException || e instanceof MongoSocketException) {
            return new ConexionException(operacion, e);
        }
        if (ErrorCategory.fromErrorCode(e.getCode()) == ErrorCategory.DUPLICATE_KEY) {
            return new DuplicadoException(operacion, e);
        }
        if (e.getCode() == ERROR_VALIDACION) {
            return new ValidacionException(operacion, e);
        }
        return new DatosException(operacion, "Error en la base de datos: " + e.getMessage(), e);
    }

    /**
     * Pasos de una operación que tocan varias colecciones y deben hacerse todos o ninguno.
     *
     * @param <T> tipo del resultado
     */
    private interface Pasos<T> {

        /**
         * Ejecuta los pasos.
         *
         * @param s sesión de la transacción, o {@code null} si el servidor no admite transacciones
         * @return el resultado de la operación
         */
        T ejecutar(ClientSession s);
    }

    /**
     * Ejecuta los pasos dentro de una transacción si el servidor la admite (replica set).
     * Un MongoDB instalado como servidor único no admite transacciones: entonces los pasos se
     * ejecutan seguidos y se anota un aviso en el log.
     *
     * @param <T>   tipo del resultado
     * @param pasos operaciones a ejecutar
     * @return el resultado de los pasos
     */
    private <T> T enTransaccion(final Pasos<T> pasos) {
        if (!admiteTransacciones()) {
            return pasos.ejecutar(null);
        }
        try (final ClientSession s = conexion.startSession()) {
            return s.withTransaction(new TransactionBody<T>() {
                @Override
                public T execute() {
                    return pasos.ejecutar(s);
                }
            });
        }
    }

    /**
     * Comprueba una vez si el servidor forma parte de un replica set (y por tanto admite transacciones).
     *
     * @return {@code true} si se pueden usar transacciones
     */
    private static synchronized boolean admiteTransacciones() {
        if (admiteTransacciones == null) {
            Document hola = bd().runCommand(new Document("hello", 1));
            admiteTransacciones = hola.containsKey("setName");
            if (!admiteTransacciones) {
                Registro.aviso("MongoDB sin replica set: las operaciones en varias colecciones se hacen sin transacción");
            }
        }
        return admiteTransacciones;
    }

    /**
     * Borra varios documentos, dentro de la sesión si la hay.
     *
     * @param s       sesión de la transacción o {@code null}
     * @param col     nombre de la colección
     * @param filtro  documentos a borrar
     * @return número de documentos borrados
     */
    private long borrarVarios(ClientSession s, String col, Bson filtro) {
        MongoCollection<Document> c = bd().getCollection(col);
        return (s == null ? c.deleteMany(filtro) : c.deleteMany(s, filtro)).getDeletedCount();
    }

    /**
     * Borra un documento, dentro de la sesión si la hay.
     *
     * @param s      sesión de la transacción o {@code null}
     * @param col    nombre de la colección
     * @param filtro documento a borrar
     * @return {@code true} si se borró
     */
    private boolean borrarUno(ClientSession s, String col, Bson filtro) {
        MongoCollection<Document> c = bd().getCollection(col);
        return (s == null ? c.deleteOne(filtro) : c.deleteOne(s, filtro)).getDeletedCount() > 0;
    }

    /**
     * Actualiza varios documentos, dentro de la sesión si la hay.
     *
     * @param s       sesión de la transacción o {@code null}
     * @param col     nombre de la colección
     * @param filtro  documentos a actualizar
     * @param cambios cambios a aplicar
     * @return número de documentos modificados
     */
    private long actualizarVarios(ClientSession s, String col, Bson filtro, Bson cambios) {
        MongoCollection<Document> c = bd().getCollection(col);
        return (s == null ? c.updateMany(filtro, cambios) : c.updateMany(s, filtro, cambios)).getModifiedCount();
    }

    // ---------- Login y usuarios ----------

    @Override
    public String validarLogin(String mail, String contraseña) {
        try {
            Bson filtro = Filters.and(Filters.eq("mail", mail), Filters.eq("contraseña", contraseña));
            Document trabajador = bd().getCollection("Trabajadores").find(filtro).first();
            if (trabajador != null) {
                Registro.operacion("SELECT Trabajadores", "login de " + mail);
                return trabajador.getString("tipo");
            }
            Document cli = bd().getCollection("Clientes").find(filtro).first();
            if (cli != null) {
                Registro.operacion("SELECT Clientes", "login de " + mail);
                return "cliente";
            }
            Registro.operacion("SELECT Login", "sin coincidencias para " + mail);
            return null;
        } catch (MongoException e) {
            throw fallo("SELECT Login", "login de " + mail, e);
        }
    }

    @Override
    public boolean existeMail(String mail) {
        try {
            boolean existe = bd().getCollection("Clientes").find(Filters.eq("mail", mail)).first() != null
                    || bd().getCollection("Trabajadores").find(Filters.eq("mail", mail)).first() != null;
            Registro.operacion("SELECT Clientes/Trabajadores", "existe mail " + mail + ": " + existe);
            return existe;
        } catch (MongoException e) {
            throw fallo("SELECT Clientes/Trabajadores", "existe mail " + mail, e);
        }
    }

    @Override
    public boolean mailValido(String mail) {
        return mail != null && MAIL_VALIDO.matcher(mail.trim().toLowerCase()).matches();
    }

    // ---------- Clientes ----------

    @Override
    public List<Document> listarClientes() {
        try {
            List<Document> lista = bd().getCollection("Clientes").find().into(new ArrayList<Document>());
            Registro.operacion("SELECT Clientes", "listado (" + lista.size() + " registros)");
            return lista;
        } catch (MongoException e) {
            throw fallo("SELECT Clientes", "listado", e);
        }
    }

    @Override
    public Document buscarClientePorMail(String mail) {
        try {
            Document c = bd().getCollection("Clientes").find(Filters.eq("mail", mail)).first();
            Registro.operacion("SELECT Clientes", "buscar " + mail + (c == null ? " (no existe)" : ""));
            return c;
        } catch (MongoException e) {
            throw fallo("SELECT Clientes", "buscar " + mail, e);
        }
    }

    @Override
    public void insertarCliente(Document c) {
        try {
            bd().getCollection("Clientes").insertOne(c);
            Registro.operacion("INSERT Clientes", "dni " + c.getString("dni"));
        } catch (MongoException e) {
            throw fallo("INSERT Clientes", "dni " + c.getString("dni"), e);
        }
    }

    @Override
    public void actualizarCliente(ObjectId id, String nombre, String apellido,
            String dni, String mail, String membresia) {
        try {
            bd().getCollection("Clientes").updateOne(Filters.eq("_id", id),
                    Updates.combine(
                            Updates.set("nombre", nombre),
                            Updates.set("apellido", apellido),
                            Updates.set("dni", dni),
                            Updates.set("mail", mail),
                            Updates.set("membresia", membresia)));
            Registro.operacion("UPDATE Clientes", "id " + id + " (dni " + dni + ")");
        } catch (MongoException e) {
            throw fallo("UPDATE Clientes", "id " + id, e);
        }
    }

    // Borra al cliente y lo quita de los asistentes de las sesiones, todo o nada.
    @Override
    public boolean eliminarCliente(final ObjectId id) {
        try {
            boolean eliminado = enTransaccion(new Pasos<Boolean>() {
                @Override
                public Boolean ejecutar(ClientSession s) {
                    actualizarVarios(s, "Sesiones", Filters.eq("asistentes.clienteid", id),
                            Updates.pull("asistentes", new Document("clienteid", id)));
                    return borrarUno(s, "Clientes", Filters.eq("_id", id));
                }
            });
            Registro.operacion("DELETE Clientes", "id " + id + " y sus plazas en sesiones (eliminado: " + eliminado + ")");
            return eliminado;
        } catch (MongoException e) {
            throw fallo("DELETE Clientes", "id " + id, e);
        }
    }

    @Override
    public boolean apuntarseAClase(String mail, ObjectId claseId, String nombreClase) {
        String detalle = "apuntar " + mail + " a " + nombreClase;
        try {
            Document socio = bd().getCollection("Clientes").find(Filters.eq("mail", mail)).first();
            if (socio == null) {
                Registro.operacion("UPDATE Clientes/Sesiones", detalle + ": el cliente no existe");
                return false;
            }
            ObjectId clienteId = socio.getObjectId("_id");

            // 1. Plaza en la última sesión de la clase. El filtro comprueba en la misma
            //    operación que queda sitio y que el cliente no está ya, así dos clientes
            //    a la vez no pueden pasarse del aforo.
            Document sesion = ultimaSesionDeClase(claseId);
            if (sesion != null) {
                Document hayPlaza = new Document("$lt", Arrays.asList(
                        new Document("$size", new Document("$ifNull", Arrays.asList("$asistentes", new ArrayList<Object>()))),
                        "$plazaMax"));
                long dentro = bd().getCollection("Sesiones").updateOne(
                        Filters.and(Filters.eq("_id", sesion.getObjectId("_id")),
                                Filters.ne("asistentes.clienteid", clienteId),
                                Filters.expr(hayPlaza)),
                        Updates.push("asistentes", new Document("clienteid", clienteId)))
                        .getModifiedCount();
                if (dentro == 0) {
                    Registro.operacion("UPDATE Sesiones", detalle + ": sesión completa, no entra");
                    return false;
                }
            }

            // 2. La clase en la lista del cliente; si falla, se devuelve la plaza (compensación).
            Document nueva = new Document("id_clase", claseId).append("nombre", nombreClase);
            boolean guardado;
            try {
                guardado = bd().getCollection("Clientes")
                        .updateOne(Filters.eq("_id", clienteId), Updates.push("clases", nueva))
                        .getModifiedCount() > 0;
            } catch (MongoException e) {
                guardado = false;
                Registro.operacionError("UPDATE Clientes", detalle, e);
            }
            if (!guardado && sesion != null) {
                bd().getCollection("Sesiones").updateOne(Filters.eq("_id", sesion.getObjectId("_id")),
                        Updates.pull("asistentes", new Document("clienteid", clienteId)));
                Registro.operacion("UPDATE Sesiones", detalle + ": se devuelve la plaza");
                return false;
            }
            Registro.operacion("UPDATE Clientes/Sesiones", detalle);
            return guardado;
        } catch (MongoException e) {
            throw fallo("UPDATE Clientes/Sesiones", detalle, e);
        }
    }

    @Override
    public boolean darLogro(String mail, String nombreLogro, int semanas) {
        try {
            Document logro = new Document("nombre", nombreLogro)
                    .append("fecha", LocalDate.now().toString())
                    .append("semanas", semanas);
            boolean dado = bd().getCollection("Clientes").updateOne(
                    Filters.and(Filters.eq("mail", mail), Filters.ne("logros.nombre", nombreLogro)),
                    Updates.push("logros", logro)).getModifiedCount() > 0;
            if (dado) {
                Registro.operacion("UPDATE Clientes", "logro " + nombreLogro + " para " + mail);
            }
            return dado;
        } catch (MongoException e) {
            throw fallo("UPDATE Clientes", "logro " + nombreLogro + " para " + mail, e);
        }
    }

    @Override
    public void revisarLogros(String mail) {
        Document cliente = buscarClientePorMail(mail);
        if (cliente == null) {
            return;
        }
        int semanas = semanasDesdeAlta(cliente);
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
            return LocalDate.parse(valor.toString().substring(0, 10));
        } catch (RuntimeException e) {
            return null;
        }
    }

    // ---------- Trabajadores ----------

    @Override
    public Document buscarTrabajadorPorMail(String mail) {
        try {
            Document t = bd().getCollection("Trabajadores").find(Filters.eq("mail", mail)).first();
            Registro.operacion("SELECT Trabajadores", "buscar " + mail + (t == null ? " (no existe)" : ""));
            return t;
        } catch (MongoException e) {
            throw fallo("SELECT Trabajadores", "buscar " + mail, e);
        }
    }

    @Override
    public List<Document> listarMonitores() {
        try {
            List<Document> lista = bd().getCollection("Trabajadores")
                    .find(Filters.eq("tipo", "monitor")).into(new ArrayList<Document>());
            Registro.operacion("SELECT Trabajadores", "listado de monitores (" + lista.size() + " registros)");
            return lista;
        } catch (MongoException e) {
            throw fallo("SELECT Trabajadores", "listado de monitores", e);
        }
    }

    @Override
    public void insertarMonitor(Document m) {
        try {
            bd().getCollection("Trabajadores").insertOne(m);
            Registro.operacion("INSERT Trabajadores", "dni " + m.getString("dni"));
        } catch (MongoException e) {
            throw fallo("INSERT Trabajadores", "dni " + m.getString("dni"), e);
        }
    }

    @Override
    public void actualizarMonitor(ObjectId id, String nombre, String apellido,
            String dni, String mail, List<Document> direccion) {
        try {
            bd().getCollection("Trabajadores").updateOne(Filters.eq("_id", id),
                    Updates.combine(
                            Updates.set("nombre", nombre),
                            Updates.set("apellido", apellido),
                            Updates.set("dni", dni),
                            Updates.set("mail", mail),
                            Updates.set("direccion", direccion)));
            Registro.operacion("UPDATE Trabajadores", "id " + id + " (dni " + dni + ")");
        } catch (MongoException e) {
            throw fallo("UPDATE Trabajadores", "id " + id, e);
        }
    }

    @Override
    public boolean eliminarMonitor(ObjectId id) {
        try {
            boolean eliminado = bd().getCollection("Trabajadores")
                    .deleteOne(Filters.eq("_id", id)).getDeletedCount() > 0;
            Registro.operacion("DELETE Trabajadores", "id " + id + " (eliminado: " + eliminado + ")");
            return eliminado;
        } catch (MongoException e) {
            throw fallo("DELETE Trabajadores", "id " + id, e);
        }
    }

    // ---------- Clases, salas y sesiones ----------

    @Override
    public List<Document> listarClases() {
        try {
            List<Document> lista = bd().getCollection("Clases").find().into(new ArrayList<Document>());
            Registro.operacion("SELECT Clases", "listado (" + lista.size() + " registros)");
            return lista;
        } catch (MongoException e) {
            throw fallo("SELECT Clases", "listado", e);
        }
    }

    @Override
    public void insertarClase(Document c) {
        try {
            bd().getCollection("Clases").insertOne(c);
            Registro.operacion("INSERT Clases", "clase " + c.getString("nombre"));
        } catch (MongoException e) {
            throw fallo("INSERT Clases", "clase " + c.getString("nombre"), e);
        }
    }

    @Override
    public void actualizarClase(ObjectId id, String nombre, ObjectId salaId) {
        try {
            bd().getCollection("Clases").updateOne(Filters.eq("_id", id),
                    Updates.combine(Updates.set("nombre", nombre), Updates.set("salaid", salaId)));
            Registro.operacion("UPDATE Clases", "id " + id + " (" + nombre + ")");
        } catch (MongoException e) {
            throw fallo("UPDATE Clases", "id " + id, e);
        }
    }

    @Override
    public void actualizarHorasClase(ObjectId id, List<Document> horas) {
        try {
            bd().getCollection("Clases").updateOne(Filters.eq("_id", id), Updates.set("horas", horas));
            Registro.operacion("UPDATE Clases", "horario de " + id + " (" + horas.size() + " franjas)");
        } catch (MongoException e) {
            throw fallo("UPDATE Clases", "horario de " + id, e);
        }
    }

    // Borra la clase, sus sesiones y la quita de la lista de los clientes, todo o nada.
    @Override
    public boolean eliminarClase(final ObjectId id) {
        try {
            boolean eliminada = enTransaccion(new Pasos<Boolean>() {
                @Override
                public Boolean ejecutar(ClientSession s) {
                    borrarVarios(s, "Sesiones", Filters.eq("claseid", id));
                    actualizarVarios(s, "Clientes", Filters.eq("clases.id_clase", id),
                            Updates.pull("clases", new Document("id_clase", id)));
                    return borrarUno(s, "Clases", Filters.eq("_id", id));
                }
            });
            Registro.operacion("DELETE Clases", "id " + id + " con sus sesiones (eliminada: " + eliminada + ")");
            return eliminada;
        } catch (MongoException e) {
            throw fallo("DELETE Clases", "id " + id, e);
        }
    }

    @Override
    public List<Document> listarSalas() {
        try {
            List<Document> lista = bd().getCollection("Salas").find().into(new ArrayList<Document>());
            Registro.operacion("SELECT Salas", "listado (" + lista.size() + " registros)");
            return lista;
        } catch (MongoException e) {
            throw fallo("SELECT Salas", "listado", e);
        }
    }

    @Override
    public Document ultimaSesionDeClase(ObjectId claseId) {
        try {
            Document s = bd().getCollection("Sesiones").find(Filters.eq("claseid", claseId))
                    .sort(Sorts.descending("fecha")).first();
            Registro.operacion("SELECT Sesiones", "última sesión de " + claseId + (s == null ? " (ninguna)" : ""));
            return s;
        } catch (MongoException e) {
            throw fallo("SELECT Sesiones", "última sesión de " + claseId, e);
        }
    }

    @Override
    public List<Document> listarSesiones(ObjectId claseId) {
        try {
            List<Document> lista = bd().getCollection("Sesiones").find(Filters.eq("claseid", claseId))
                    .sort(Sorts.ascending("fecha")).into(new ArrayList<Document>());
            Registro.operacion("SELECT Sesiones", "sesiones de " + claseId + " (" + lista.size() + " registros)");
            return lista;
        } catch (MongoException e) {
            throw fallo("SELECT Sesiones", "sesiones de " + claseId, e);
        }
    }

    @Override
    public void insertarSesion(Document s) {
        try {
            bd().getCollection("Sesiones").insertOne(s);
            Registro.operacion("INSERT Sesiones", "clase " + s.get("claseid") + " el " + s.get("fecha"));
        } catch (MongoException e) {
            throw fallo("INSERT Sesiones", "clase " + s.get("claseid"), e);
        }
    }

    @Override
    public void actualizarSesion(ObjectId id, Date fecha, int plazaMax, ObjectId monitorId) {
        try {
            bd().getCollection("Sesiones").updateOne(Filters.eq("_id", id),
                    Updates.combine(Updates.set("fecha", fecha), Updates.set("plazaMax", plazaMax),
                            Updates.set("monitor", monitorId)));
            Registro.operacion("UPDATE Sesiones", "id " + id + " (" + plazaMax + " plazas)");
        } catch (MongoException e) {
            throw fallo("UPDATE Sesiones", "id " + id, e);
        }
    }

    @Override
    public boolean eliminarSesion(ObjectId id) {
        try {
            boolean eliminada = bd().getCollection("Sesiones")
                    .deleteOne(Filters.eq("_id", id)).getDeletedCount() > 0;
            Registro.operacion("DELETE Sesiones", "id " + id + " (eliminada: " + eliminada + ")");
            return eliminada;
        } catch (MongoException e) {
            throw fallo("DELETE Sesiones", "id " + id, e);
        }
    }
}
