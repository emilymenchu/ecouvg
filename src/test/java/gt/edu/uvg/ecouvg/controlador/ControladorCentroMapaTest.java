package gt.edu.uvg.ecouvg.controlador;

import gt.edu.uvg.ecouvg.dto.CentroMapaDTO;
import gt.edu.uvg.ecouvg.servicio.ServicioCentro;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Pruebas de contrato HTTP; el modo standalone no prueba los filtros de seguridad. */
class ControladorCentroMapaTest {
    ServicioCentro servicio;
    MockMvc mvc;
    @BeforeEach void preparar() {
        servicio = mock(ServicioCentro.class);
        mvc = MockMvcBuilders.standaloneSetup(new ControladorCentro(servicio)).build();
    }
    @Test void devuelveJsonYValoresPorDefecto() throws Exception {
        when(servicio.obtenerCentrosMapa("", "")).thenReturn(List.of(
            new CentroMapaDTO(7L, "Cayalá", "Zona 16", 14.6, -90.4,
                "Por confirmar", List.of("Cartón"))));
        mvc.perform(get("/api/centros/mapa"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith("application/json"))
            .andExpect(jsonPath("$[0].id").value(7))
            .andExpect(jsonPath("$[0].ubicable").value(true))
            .andExpect(jsonPath("$[0].materiales[0]").value("Cartón"))
            .andExpect(jsonPath("$[0].residuosAceptados").doesNotExist());
    }
    @Test void transmiteFiltrosYDevuelveListaVacia() throws Exception {
        when(servicio.obtenerCentrosMapa("zona 15", "Vidrio")).thenReturn(List.of());
        mvc.perform(get("/api/centros/mapa").param("q", "zona 15").param("material", "Vidrio"))
            .andExpect(status().isOk()).andExpect(content().json("[]"));
        verify(servicio).obtenerCentrosMapa("zona 15", "Vidrio");
    }
    @Test void identificaCentroNoUbicable() throws Exception {
        when(servicio.obtenerCentrosMapa("", "")).thenReturn(List.of(
            new CentroMapaDTO(9L, "Pendiente", "Zona 15", Double.NaN, -90.4, null, List.of())));
        mvc.perform(get("/api/centros/mapa"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].nombre").value("Pendiente"))
            .andExpect(jsonPath("$[0].ubicable").value(false))
            .andExpect(jsonPath("$[0].latitud").isEmpty())
            .andExpect(jsonPath("$[0].longitud").isEmpty());
    }
    @Test void rechazaParametrosLargosAntesDeConsultar() throws Exception {
        mvc.perform(get("/api/centros/mapa").param("q", "x".repeat(121)))
            .andExpect(status().isBadRequest());
        mvc.perform(get("/api/centros/mapa").param("material", "x".repeat(121)))
            .andExpect(status().isBadRequest());
        verifyNoInteractions(servicio);
    }
    @Test void detalleDevuelveSoloElCentroSolicitado() throws Exception {
        when(servicio.obtenerCentroMapa(7L)).thenReturn(java.util.Optional.of(
            new CentroMapaDTO(7L, "Cayalá", "Zona 16", 14.6, -90.4, null, List.of("Cartón"))));
        mvc.perform(get("/api/centros/mapa/7"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(7))
            .andExpect(jsonPath("$.ubicable").value(true));
        verify(servicio).obtenerCentroMapa(7L);
        verifyNoMoreInteractions(servicio);
    }
    @Test void detalleInexistenteDevuelve404() throws Exception {
        when(servicio.obtenerCentroMapa(99L)).thenReturn(java.util.Optional.empty());
        mvc.perform(get("/api/centros/mapa/99")).andExpect(status().isNotFound());
    }
    @Test void detalleSinCoordenadasExistePeroNoEsUbicable() throws Exception {
        when(servicio.obtenerCentroMapa(9L)).thenReturn(java.util.Optional.of(
            new CentroMapaDTO(9L, "Pendiente", "Zona 15", null, -90.4, null, List.of())));
        mvc.perform(get("/api/centros/mapa/9"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ubicable").value(false))
            .andExpect(jsonPath("$.latitud").isEmpty())
            .andExpect(jsonPath("$.longitud").isEmpty());
    }
}
