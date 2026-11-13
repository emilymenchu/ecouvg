package gt.edu.uvg.ecouvg.servicio;
import gt.edu.uvg.ecouvg.dto.*;
import gt.edu.uvg.ecouvg.modelo.*;
import gt.edu.uvg.ecouvg.repositorio.*;
import org.junit.jupiter.api.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.*;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ServicioMisionArcadeTest {
    RepositorioUsuario usuarios;
    RepositorioMisionArcade misiones;
    Usuario usuario;
    @BeforeEach void preparar() throws Exception {
        usuarios=mock(RepositorioUsuario.class);misiones=mock(RepositorioMisionArcade.class);
        usuario=new Usuario("Daniel","26961","ejemplo@example.org","hash");
        var id=Usuario.class.getDeclaredField("id");id.setAccessible(true);id.set(usuario,7L);
        when(usuarios.findByCarnet("26961")).thenReturn(Optional.of(usuario));
        when(usuarios.bloquearParaSeguimiento(7L)).thenReturn(Optional.of(usuario));
    }
    ServicioMisionArcade servicio(String instante) {
        return new ServicioMisionArcade(usuarios,misiones,Clock.fixed(Instant.parse(instante),ZoneOffset.UTC));
    }
    @Test void cincoCompletanSinOtorgarPuntos() {
        var estado=servicio("2026-11-13T12:00:00Z").completar("26961",new SolicitudMisionArcade("eco-run",5));
        assertTrue(estado.completada());assertEquals(1,estado.semanasRacha());assertEquals(LocalDate.of(2026,11,9),estado.semana());
        assertEquals(0,usuario.getPuntos());verify(misiones).save(any(MisionArcade.class));
    }
    @Test void cuatroOJuegoInventadoNoCompletan() {
        var s=servicio("2026-11-13T12:00:00Z");
        assertThrows(ResponseStatusException.class,()->s.completar("26961",new SolicitudMisionArcade("eco-snake",4)));
        assertThrows(ResponseStatusException.class,()->s.completar("26961",new SolicitudMisionArcade("inventado",5)));
        assertThrows(ResponseStatusException.class,()->s.completar("26961",new SolicitudMisionArcade("eco-snake",999)));
        verifyNoInteractions(misiones);
    }
    @Test void reintentosNoDuplicanLaSemana() {
        when(misiones.existsByUsuarioIdAndSemana(7L,LocalDate.of(2026,11,9))).thenReturn(false,true);
        var s=servicio("2026-11-13T12:00:00Z");
        s.completar("26961",new SolicitudMisionArcade("eco-snake",5));s.completar("26961",new SolicitudMisionArcade("eco-breakout",5));
        verify(misiones,times(1)).save(any(MisionArcade.class));assertEquals(1,usuario.getSemanasRacha());
    }
    @Test void actividadDeEvidenciaEnMismaSemanaNoSumaOtraSemana() {
        usuario.registroActividadValida(LocalDate.of(2026,11,10));
        var e=servicio("2026-11-13T12:00:00Z").completar("26961",new SolicitudMisionArcade("eco-run",5));
        assertEquals(1,e.semanasRacha());assertTrue(e.completada());
    }
    @Test void lunesUsaGuatemalaYNoElCambioDeDiaUTC() {
        var domingo=servicio("2026-11-16T05:59:00Z").completar("26961",new SolicitudMisionArcade("eco-run",5));
        assertEquals(LocalDate.of(2026,11,9),domingo.semana());
        var lunes=servicio("2026-11-16T06:01:00Z").completar("26961",new SolicitudMisionArcade("eco-run",5));
        assertEquals(LocalDate.of(2026,11,16),lunes.semana());assertEquals(2,lunes.semanasRacha());
    }
    @Test void semanaSaltadaReiniciaRacha() {
        usuario.registroActividadValida(LocalDate.of(2026,10,26));
        assertEquals(1,servicio("2026-11-13T12:00:00Z").completar("26961",new SolicitudMisionArcade("eco-run",5)).semanasRacha());
    }
}
