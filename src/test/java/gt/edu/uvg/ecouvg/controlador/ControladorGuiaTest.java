package gt.edu.uvg.ecouvg.controlador;

import gt.edu.uvg.ecouvg.servicio.ServicioGuia;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.hasSize;

/** Contrato MVC; no sustituye renderizado Thymeleaf ni pruebas de sesión. */
class ControladorGuiaTest {
    private MockMvc mvc;
    @BeforeEach void preparar() {
        mvc = MockMvcBuilders.standaloneSetup(new ControladorGuia(new ServicioGuia(gt.edu.uvg.ecouvg.GuiaPrueba.repositorio())))
            .setSingleView(org.mockito.Mockito.mock(org.springframework.web.servlet.View.class)).build();
    }
    @Test void muestraCatalogoCompleto() throws Exception {
        mvc.perform(get("/guia")).andExpect(status().isOk()).andExpect(view().name("guia"))
            .andExpect(model().attribute("categorias", hasSize(8)));
    }
    @Test void buscaYConservaConsulta() throws Exception {
        mvc.perform(get("/guia").param("q", "pilas")).andExpect(status().isOk())
            .andExpect(model().attribute("categorias", hasSize(1)))
            .andExpect(model().attribute("q", "pilas"));
    }
    @Test void detalleConservaConsultaDeRegreso() throws Exception {
        mvc.perform(get("/guia/vidrio").param("q", "botella"))
            .andExpect(status().isOk()).andExpect(view().name("guia-detalle"))
            .andExpect(model().attributeExists("categoria"))
            .andExpect(model().attribute("q", "botella"));
    }
    @Test void devuelve404ParaCategoriaDesconocida() throws Exception {
        mvc.perform(get("/guia/inventada")).andExpect(status().isNotFound());
    }
    @Test void limitaConsultasEnAmbasRutas() throws Exception {
        mvc.perform(get("/guia").param("q", "x".repeat(121))).andExpect(status().isBadRequest());
        mvc.perform(get("/guia/vidrio").param("q", "x".repeat(121))).andExpect(status().isBadRequest());
    }
}
