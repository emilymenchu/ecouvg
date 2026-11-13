package gt.edu.uvg.ecouvg.modelo;

import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UsuarioRachaTest {
    private final Usuario usuario = new Usuario("Prueba", "123", "prueba@example.com", "clave");

    @Test void actividadEnLaMismaSemanaActualizaLaImagenSinDuplicarLaRacha() {
        LocalDate lunes = LocalDate.of(2026, 11, 9);
        usuario.registroActividadValida(lunes);
        assertEquals("Eco racha 3.png", usuario.obtenerImagenEco(lunes.plusDays(2)));
        usuario.registroActividadValida(lunes.plusDays(2));
        assertEquals(1, usuario.getSemanasRacha());
        assertEquals("Eco sentada feliz.png", usuario.obtenerImagenEco(lunes.plusDays(2)));
        usuario.registroActividadValida(lunes); // Una actividad anterior no retrocede la fecha.
        assertEquals("Eco sentada feliz.png", usuario.obtenerImagenEco(lunes.plusDays(2)));
    }

    @Test void semanasConsecutivasAcumulanYUnaInterrupcionReiniciaLaRacha() {
        LocalDate lunes = LocalDate.of(2026, 11, 9);
        usuario.registroActividadValida(lunes);
        usuario.registroActividadValida(lunes.plusWeeks(1));
        assertEquals(2, usuario.obtenerRacha(lunes.plusWeeks(2)));
        assertEquals(0, usuario.obtenerRacha(lunes.plusWeeks(3)));
        assertEquals("Eco muerto.png", usuario.obtenerImagenEco(lunes.plusWeeks(3)));
        usuario.registroActividadValida(lunes.plusWeeks(3));
        assertEquals(1, usuario.getSemanasRacha());
    }

    @Test void consultarLaImagenNoModificaLaRacha() {
        LocalDate lunes = LocalDate.of(2026, 11, 9);
        assertEquals("Eco sentada feliz.png", usuario.obtenerImagenEco(lunes));
        usuario.registroActividadValida(lunes);
        assertEquals("Eco racha 6.png", usuario.obtenerImagenEco(lunes.plusDays(10)));
        assertEquals(1, usuario.getSemanasRacha());
        assertEquals(1, usuario.obtenerRacha(lunes));
    }

    @Test void rachaVigenteUtilizaLaFechaDeGuatemala() {
        usuario.registroActividadValida(LocalDate.now(ZoneId.of("America/Guatemala")));
        assertEquals(1, usuario.getRachaVigente());
    }

    @Test void canjearEcoPuntosConservaLaExperienciaYElNivel() {
        usuario.sumarPuntos(750);
        usuario.gastarEcoPuntos(100);
        assertEquals(650, usuario.getPuntos());
        assertEquals(750, usuario.getPuntosTotales());
        assertEquals(4, usuario.getNivel());
        assertThrows(IllegalArgumentException.class, () -> usuario.gastarEcoPuntos(651));
        assertThrows(IllegalArgumentException.class, () -> usuario.gastarEcoPuntos(0));
        assertEquals(650, usuario.getPuntos());
    }
}
