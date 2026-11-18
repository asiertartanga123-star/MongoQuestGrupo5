package com.mycompany.controlador;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mycompany.controlador.dao.daoImplementacio;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Apuntarse a una clase mete al cliente en los asistentes de su sesión y respeta el aforo.
 * Usa una clase temporal con una sesión sin plazas (luego con 1) y la borra al acabar.
 */
public class ApuntarseSesionTest {

    private static final daoImplementacio DAO = new daoImplementacio();
    private static Document clase;
    private static List<Document> clientes;

    @BeforeAll
    public static void preparar() {
        boolean activo;
        try (Socket s = new Socket()) {
            s.connect(new InetSocketAddress("localhost", 27017), 500);
            activo = true;
        } catch (Exception e) {
            activo = false;
        }
        Assumptions.assumeTrue(activo, "MongoDB no está arrancado en localhost:27017");
        clientes = DAO.listarClientes();
        Assumptions.assumeTrue(!clientes.isEmpty(), "No hay clientes en la base de datos");
        Assumptions.assumeTrue(!DAO.listarMonitores().isEmpty(), "No hay monitores");

        clase = new Document("nombre", "PRUEBA aforo " + System.currentTimeMillis())
                .append("horas", new ArrayList<Document>())
                .append("salaid", DAO.listarSalas().get(0).getObjectId("_id"));
        DAO.insertarClase(clase);
        DAO.insertarSesion(new Document("claseid", clase.getObjectId("_id"))
                .append("fecha", new Date(System.currentTimeMillis() + 86400000L))
                .append("plazaMax", 0)
                .append("monitor", DAO.listarMonitores().get(0).getObjectId("_id"))
                .append("asistentes", new ArrayList<Document>()));
    }

    // Borra la clase (y su sesión) y la quita de la lista de los clientes.
    @AfterAll
    public static void limpiar() {
        if (clase == null) {
            return;
        }
        ObjectId id = clase.getObjectId("_id");
        try (MongoClient c = MongoClients.create("mongodb://localhost:27017")) {
            c.getDatabase("Tolotolo").getCollection("Clientes").updateMany(
                    Filters.eq("clases.id_clase", id), Updates.pull("clases", new Document("id_clase", id)));
        }
        DAO.eliminarClase(id);
    }

    @Test
    public void sinPlazasNoEntraYConPlazaEntraEnLosAsistentes() {
        ObjectId id = clase.getObjectId("_id");
        String nombre = clase.getString("nombre");
        String mail = clientes.get(0).getString("mail");
        Document sesion = DAO.ultimaSesionDeClase(id);

        // Sesión con 0 plazas: no entra y no se le añade la clase.
        Assertions.assertFalse(DAO.apuntarseAClase(mail, id, nombre), "Sin plazas no debería entrar");
        Assertions.assertTrue(DAO.ultimaSesionDeClase(id).getList("asistentes", Document.class).isEmpty());
        Assertions.assertFalse(tieneLaClase(mail, id), "Sin plazas no se le debería añadir la clase");

        // Con 1 plaza: entra, queda en los asistentes de la sesión y tiene la clase.
        DAO.actualizarSesion(sesion.getObjectId("_id"), sesion.getDate("fecha"), 1, sesion.getObjectId("monitor"));
        Assertions.assertTrue(DAO.apuntarseAClase(mail, id, nombre));
        List<Document> asistentes = DAO.ultimaSesionDeClase(id).getList("asistentes", Document.class);
        Assertions.assertEquals(1, asistentes.size(), "El cliente debería estar en los asistentes de la sesión");
        Assertions.assertEquals(clientes.get(0).getObjectId("_id"), asistentes.get(0).getObjectId("clienteid"));
        Assertions.assertTrue(tieneLaClase(mail, id));

        // Sala llena (1/1): un segundo intento no duplica la plaza.
        Assertions.assertFalse(DAO.apuntarseAClase(mail, id, nombre));
        Assertions.assertEquals(1, DAO.ultimaSesionDeClase(id).getList("asistentes", Document.class).size());
    }

    // Indica si la clase está en la lista de clases del cliente.
    private boolean tieneLaClase(String mail, ObjectId id) {
        List<Document> clases = DAO.buscarClientePorMail(mail).getList("clases", Document.class);
        if (clases != null) {
            for (Document d : clases) {
                if (id.equals(d.getObjectId("id_clase"))) {
                    return true;
                }
            }
        }
        return false;
    }
}
