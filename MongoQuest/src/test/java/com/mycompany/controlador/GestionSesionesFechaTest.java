package com.mycompany.controlador;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** Pruebas de {@link GestionSesionesController#proximaFecha}; no necesitan MongoDB. */
public class GestionSesionesFechaTest {

    // Miércoles 18-11-2026 a las 16:30
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 18, 16, 30);

    @Test
    public void mismoDiaHoraFuturaEsHoy() {
        Assertions.assertEquals(LocalDateTime.of(2026, 11, 18, 18, 0),
                GestionSesionesController.proximaFecha(AHORA, DayOfWeek.WEDNESDAY, 18));
    }

    @Test
    public void mismoDiaHoraPasadaEsSemanaSiguiente() {
        Assertions.assertEquals(LocalDateTime.of(2026, 11, 25, 10, 0),
                GestionSesionesController.proximaFecha(AHORA, DayOfWeek.WEDNESDAY, 10));
    }

    @Test
    public void mismaHoraExactaEsSemanaSiguiente() {
        Assertions.assertEquals(LocalDateTime.of(2026, 11, 25, 16, 0),
                GestionSesionesController.proximaFecha(LocalDateTime.of(2026, 11, 18, 16, 0), DayOfWeek.WEDNESDAY, 16));
    }

    @Test
    public void otroDiaEsElProximo() {
        Assertions.assertEquals(LocalDateTime.of(2026, 11, 23, 9, 0),
                GestionSesionesController.proximaFecha(AHORA, DayOfWeek.MONDAY, 9));
        Assertions.assertEquals(LocalDateTime.of(2026, 11, 21, 22, 0),
                GestionSesionesController.proximaFecha(AHORA, DayOfWeek.SATURDAY, 22));
    }
}
