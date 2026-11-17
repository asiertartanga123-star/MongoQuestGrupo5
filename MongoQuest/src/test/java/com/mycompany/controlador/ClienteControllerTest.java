package com.mycompany.controlador;

import com.mycompany.controlador.dao.daoImplementacio;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import modelo.Logros;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ClienteControllerTest {

    private daoImplementacio dao;
    private ClienteController controller;

    @BeforeEach
    public void setUp() {
        dao = mock(daoImplementacio.class);          // DAO falso: no toca la base de datos
        controller = new ClienteController(dao);
    }

    // ---------- texto() ----------

    @Test
    public void texto_nulo_devuelveVacio() {
        assertEquals("", controller.texto(null));
    }

    @Test
    public void texto_numero_devuelveSuTexto() {
        assertEquals("123", controller.texto(123));
    }

    @Test
    public void texto_cadena_devuelveLaMismaCadena() {
        assertEquals("abc", controller.texto("abc"));
    }

    // ---------- nombreCompleto() ----------

    @Test
    public void nombreCompleto_juntaNombreYApellido() {
        Document cliente = new Document("nombre", "Ana").append("apellido", "Lopez");
        assertEquals("Ana Lopez", controller.nombreCompleto(cliente));
    }

    // ---------- plazasDe() ----------

    @Test
    public void plazasDe_sinSesion_devuelveSinSesion() {
        ObjectId id = new ObjectId();
        when(dao.ultimaSesionDeClase(id)).thenReturn(null);

        assertEquals("sin sesión", controller.plazasDe(id));
    }

    @Test
    public void plazasDe_conAsistentes_devuelveApuntadosSobreMaximo() {
        ObjectId id = new ObjectId();
        Document sesion = new Document("plazaMax", 10)
                .append("asistentes", Arrays.asList(new Document(), new Document()));
        when(dao.ultimaSesionDeClase(id)).thenReturn(sesion);

        assertEquals("2/10", controller.plazasDe(id));
    }

    @Test
    public void plazasDe_sinAsistentes_devuelveCeroSobreMaximo() {
        ObjectId id = new ObjectId();
        Document sesion = new Document("plazaMax", 15)
                .append("asistentes", Collections.emptyList());
        when(dao.ultimaSesionDeClase(id)).thenReturn(sesion);

        assertEquals("0/15", controller.plazasDe(id));
    }

    // ---------- cargarCliente() ----------

    @Test
    public void cargarCliente_noExiste_devuelveNullYNoRevisaLogros() {
        when(dao.buscarClientePorMail("x@x.com")).thenReturn(null);

        assertNull(controller.cargarCliente("x@x.com"));
        verify(dao, never()).revisarLogros(anyString());
    }

    @Test
    public void cargarCliente_existe_revisaLogrosYDevuelveElClienteActualizado() {
        Document antes = new Document("nombre", "Ana");
        Document despues = new Document("nombre", "Ana").append("logros", Collections.emptyList());
        when(dao.buscarClientePorMail("ana@x.com")).thenReturn(antes, despues);

        Document resultado = controller.cargarCliente("ana@x.com");

        verify(dao).revisarLogros("ana@x.com");
        assertSame(despues, resultado);
    }

    @Test
    public void cargarCliente_existe_leeAlClienteDosVeces() {
        when(dao.buscarClientePorMail("ana@x.com")).thenReturn(new Document("nombre", "Ana"));

        controller.cargarCliente("ana@x.com");

        verify(dao, times(2)).buscarClientePorMail("ana@x.com");
    }

    // ---------- construirLogros() ----------

    @Test
    public void construirLogros_numeraDesdeUnoYCopiaLosDatos() {
        List<Document> logros = Arrays.asList(
                new Document("nombre", "Primera clase").append("fecha", "2026-10-01").append("semanas", 1),
                new Document("nombre", "Constante").append("fecha", "2026-10-15").append("semanas", 4));

        List<Logros> filas = controller.construirLogros(logros);

        assertEquals(2, filas.size());
        assertEquals("1", filas.get(0).getNumero());
        assertEquals("Primera clase", filas.get(0).getNombre());
        assertEquals("2", filas.get(1).getNumero());
        assertEquals("4", filas.get(1).getSemanas());
    }

    @Test
    public void construirLogros_listaVacia_devuelveListaVacia() {
        assertTrue(controller.construirLogros(Collections.emptyList()).isEmpty());
    }

    @Test
    public void construirLogros_faltanCampos_losDejaVacios() {
        List<Document> logros = Arrays.asList(new Document("nombre", "Sin fecha"));

        List<Logros> filas = controller.construirLogros(logros);

        assertEquals("Sin fecha", filas.get(0).getNombre());
        assertEquals("", filas.get(0).getFecha());
        assertEquals("", filas.get(0).getSemanas());
    }

    // ---------- hayMasClasesQueLasVisibles() ----------

    @Test
    public void hayMasClases_listaVacia_false() {
        assertFalse(controller.hayMasClasesQueLasVisibles(Collections.emptyList()));
    }

    @Test
    public void hayMasClases_tresOMenos_false() {
        List<Document> tres = Arrays.asList(new Document(), new Document(), new Document());
        assertFalse(controller.hayMasClasesQueLasVisibles(tres));
    }

    @Test
    public void hayMasClases_masDeTres_true() {
        List<Document> cuatro = Arrays.asList(new Document(), new Document(), new Document(), new Document());
        assertTrue(controller.hayMasClasesQueLasVisibles(cuatro));
    }

    // ---------- yaApuntado() ----------

    @Test
    public void yaApuntado_claseEnLaLista_true() {
        String[] actuales = {"Yoga", "Spinning", "", "", "", ""};
        assertTrue(controller.yaApuntado("Spinning", actuales));
    }

    @Test
    public void yaApuntado_claseNoEstaEnLaLista_false() {
        String[] actuales = {"Yoga", "", "", "", "", ""};
        assertFalse(controller.yaApuntado("Pilates", actuales));
    }

    @Test
    public void yaApuntado_listaVacia_false() {
        String[] vacia = {"", "", "", "", "", ""};
        assertFalse(controller.yaApuntado("Yoga", vacia));
    }

    @Test
    public void yaApuntado_listaConNulos_noFallaYDevuelveFalse() {
        String[] conNulos = {null, null, "Yoga", null, null, null};
        assertFalse(controller.yaApuntado("Pilates", conNulos));
    }

    // ---------- primerHuecoLibre() ----------

    @Test
    public void primerHuecoLibre_devuelveElPrimeroVacio() {
        String[] actuales = {"Yoga", "Spinning", "", "", "", ""};
        assertEquals(2, controller.primerHuecoLibre(actuales));
    }

    @Test
    public void primerHuecoLibre_todoVacio_devuelveCero() {
        String[] vacia = {"", "", "", "", "", ""};
        assertEquals(0, controller.primerHuecoLibre(vacia));
    }

    @Test
    public void primerHuecoLibre_huecoNulo_devuelveEsaPosicion() {
        String[] actuales = {"Yoga", null, "Spinning", "", "", ""};
        assertEquals(1, controller.primerHuecoLibre(actuales));
    }

    @Test
    public void primerHuecoLibre_listaLlena_devuelveMenosUno() {
        String[] llena = {"A", "B", "C", "D", "E", "F"};
        assertEquals(-1, controller.primerHuecoLibre(llena));
    }
}
