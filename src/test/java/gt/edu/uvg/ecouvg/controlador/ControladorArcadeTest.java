package gt.edu.uvg.ecouvg.controlador;

import gt.edu.uvg.ecouvg.dto.MaterialArcade;
import gt.edu.uvg.ecouvg.servicio.ServicioArcade;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.View;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Contrato MVC aislado; la seguridad sigue definida en SecurityConfig. */
class ControladorArcadeTest {
    @Test void muestraPantallaYDevuelveMateriales() throws Exception {
        var servicio=mock(ServicioArcade.class);
        when(servicio.obtenerMateriales()).thenReturn(List.of(new MaterialArcade("papel","Papel",List.of("Hoja"),"Limpio y seco")));
        var mvc=MockMvcBuilders.standaloneSetup(new ControladorArcade(servicio)).setSingleView(mock(View.class)).build();
        mvc.perform(get("/arcade")).andExpect(status().isOk()).andExpect(view().name("arcade"));
        mvc.perform(get("/api/arcade/materiales")).andExpect(status().isOk())
            .andExpect(jsonPath("$[0].nombre").value("Papel"))
            .andExpect(jsonPath("$[0].ejemplos[0]").value("Hoja"));
    }
    @Test void informaCuandoNoHayCatalogo() throws Exception {
        var servicio=mock(ServicioArcade.class);
        when(servicio.obtenerMateriales()).thenThrow(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE));
        var mvc=MockMvcBuilders.standaloneSetup(new ControladorArcade(servicio)).build();
        mvc.perform(get("/api/arcade/materiales")).andExpect(status().isServiceUnavailable());
    }
}
