package gt.edu.uvg.ecouvg.controlador;
import gt.edu.uvg.ecouvg.dto.*;
import gt.edu.uvg.ecouvg.servicio.ServicioMisionArcade;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.time.LocalDate;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ControladorMisionArcadeTest {
    @Test void utilizaIdentidadAutenticadaYEstadoDelServidor() throws Exception {
        var servicio=mock(ServicioMisionArcade.class);var estado=new EstadoMisionArcade(true,5,2,LocalDate.of(2026,11,9));
        when(servicio.consultar("26961")).thenReturn(estado);
        when(servicio.completar(eq("26961"),any())).thenReturn(estado);
        var mvc=MockMvcBuilders.standaloneSetup(new ControladorMisionArcade(servicio)).build();
        var auth=new UsernamePasswordAuthenticationToken("26961","no-usado");
        mvc.perform(get("/api/arcade/mision").principal(auth)).andExpect(status().isOk()).andExpect(jsonPath("$.completada").value(true));
        mvc.perform(post("/api/arcade/mision").principal(auth).contentType("application/json")
            .content("{\"juego\":\"eco-run\",\"aciertos\":5}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.semanasRacha").value(2));
        verify(servicio).completar("26961",new SolicitudMisionArcade("eco-run",5));
    }
}
