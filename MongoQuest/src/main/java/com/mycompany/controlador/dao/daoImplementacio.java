package com.mycompany.controlador.dao;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
<<<<<<< HEAD
import com.mycompany.controlador.Registro;
=======
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
>>>>>>> Ekain
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
 * Esta clase se encarga de autenticar usuarios y mantener las colecciones
 * {@code Clientes} y {@code Trabajadores} sincronizadas con la base de datos
 * local de la aplicación.
 */
public class daoImplementacio implements dao {

    // En esta versión la instancia MongoDB es local y usa la base de datos Tolotolo.
    private static final String URI = "mongodb://localhost:27017";
    private static final String BD = "Tolotolo";

<<<<<<< HEAD
    // texto@dominio.extensión, sin espacios ni caracteres raros
    private static final Pattern MAIL_VALIDO =
            Pattern.compile("^[a-z0-9._%+-]+@[a-z0-9-]+(\\.[a-z0-9-]+)*\\.[a-z]{2,}$");

    /**
     * Valida las credenciales de acceso del usuario dentro de MongoDB.
     *
     * @param mail correo electrónico asociado al usuario
     * @param contraseña contraseña en texto plano almacenada en la base de datos
     * @return rol recuperado del documento autenticado o {@code null} si no existe usuario
     */
=======
    private static final Pattern MAIL_VALIDO =
            Pattern.compile("^[a-z0-9._%+-]+@[a-z0-9-]+(\\.[a-z0-9-]+)*\\.[a-z]{2,}$");

    // Logros automáticos: al llegar a estas semanas seguidas se da el logro
    private static final String[] LOGROS_NOMBRES = {"Constante", "Imparable", "Leyenda"};
    private static final int[] LOGROS_SEMANAS = {4, 8, 12};

>>>>>>> Ekain
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

    /**
     * Comprueba si un correo ya está registrado en una colección de usuarios.
     *
     * @param mail dirección de correo a verificar
     * @return {@code true} si la dirección ya existe en clientes o trabajadores
     */
    @Override
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

    /**
     * Verifica si un correo tiene el formato permitido por la aplicación.
     *
     * @param mail correo a comprobar
     * @return {@code true} si el formato es válido
     */
    @Override
    public boolean mailValido(String mail) {
        return mail != null && MAIL_VALIDO.matcher(mail.trim().toLowerCase()).matches();
    }

    // ---------- Operaciones de persistencia para clientes ----------

    /**
     * Recupera todos los clientes almacenados en la colección {@code Clientes}.
     *
     * @return lista de documentos BSON con los clientes registrados
     */
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

    /**
     * Inserta un nuevo cliente en la base de datos.
     *
     * @param c documento BSON que contiene todos los datos del cliente
     */
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

    /**
     * Actualiza la información editable de un cliente existente.
     *
     * @param id identificador del cliente
     * @param nombre nuevo nombre
     * @param apellido nuevo apellido
     * @param dni nuevo DNI
     * @param mail nuevo correo
     * @param membresia nueva membresía asignada
     */
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

    /**
     * Elimina un cliente de la colección correspondiente.
     *
     * @param id identificador del cliente a quitar
     * @return {@code true} si el borrado afectó a un documento
     */
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

<<<<<<< HEAD
    // ---------- Operaciones de persistencia para monitores ----------
=======
    // ---------- MONITORES ----------
>>>>>>> Ekain

    /**
     * Recupera la lista de monitores activos del sistema.
     *
     * @return conjunto de documentos BSON de monitores
     */
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

    /**
     * Guarda un monitor dentro de la colección de trabajadores.
     *
     * @param m documento BSON con los datos del monitor
     */
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

    /**
     * Actualiza la información de un monitor identificado por su ObjectId.
     *
     * @param id identificador del monitor
     * @param nombre nuevo nombre
     * @param apellido nuevo apellido
     * @param dni nuevo DNI
     * @param mail nuevo correo
     * @param direccion nueva dirección del monitor
     */
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

    /**
     * Elimina un monitor del sistema.
     *
     * @param id identificador del monitor
     * @return {@code true} si se eliminó correctamente
     */
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

        int semanas = semanasDesdeAlta(cliente);
        System.out.println("Semanas de " + mail + " en el gimnasio: " + semanas);

        for (int i = 0; i < LOGROS_NOMBRES.length; i++) {
            if (semanas >= LOGROS_SEMANAS[i]) {
                darLogro(mail, LOGROS_NOMBRES[i], LOGROS_SEMANAS[i]);
            }
        }
    }

    // Semanas completas que lleva el cliente en el gimnasio (desde su fechaAlta hasta hoy)
    private int semanasDesdeAlta(Document cliente) {
        LocalDate alta = aFecha(cliente.get("fechaAlta"));
        if (alta == null) {
            return 0;
        }
        return (int) ChronoUnit.WEEKS.between(alta, LocalDate.now());
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