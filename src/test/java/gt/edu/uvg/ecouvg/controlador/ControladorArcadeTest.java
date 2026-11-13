package gt.edu.uvg.ecouvg.controlador;

import gt.edu.uvg.ecouvg.dto.MaterialArcade;
import gt.edu.uvg.ecouvg.servicio.ServicioArcade;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.View;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Contrato MVC aislado; la seguridad sigue definida en SecurityConfig. */
class ControladorArcadeTest {
    @Test void combinaElDisenoSubmarinoConLosControlesDeLaMision() throws Exception {
        var plantillas = new ClassLoaderTemplateResolver();
        plantillas.setPrefix("templates/");
        plantillas.setSuffix(".html");
        plantillas.setCharacterEncoding("UTF-8");
        var motor = new SpringTemplateEngine();
        motor.setTemplateResolver(plantillas);
        var vistas = new ThymeleafViewResolver();
        vistas.setTemplateEngine(motor);
        vistas.setCharacterEncoding("UTF-8");
        var mvc = MockMvcBuilders.standaloneSetup(new ControladorArcade(mock(ServicioArcade.class)))
            .setViewResolvers(vistas).build();
        var resultado = mvc.perform(get("/arcade").flashAttr("_csrf", new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", "token-prueba")))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("class=\"tema-arcade\"")))
            .andExpect(content().string(containsString("class=\"pagina pagina-arcade\"")))
            .andExpect(content().string(containsString("aria-current=\"page\"")))
            .andExpect(content().string(containsString("/css/inicio.css")))
            .andExpect(content().string(containsString("/css/arcade.css?v=2")))
            .andExpect(content().string(containsString("/js/arcade/app.mjs?v=2")))
            .andExpect(content().string(containsString("data-mision-url=\"/api/arcade/mision\"")))
            .andExpect(content().string(containsString("id=\"aciertos-mision\"")))
            .andExpect(content().string(containsString("id=\"estado-mision\"")))
            .andExpect(content().string(containsString("id=\"reintentar-mision\"")))
            .andExpect(content().string(containsString("name=\"_csrf\" content=\"token-prueba\"")))
            .andExpect(content().string(containsString("name=\"_csrf_header\" content=\"X-CSRF-TOKEN\"")))
            .andReturn();
        String vistaPrevia = System.getProperty("arcade.preview");
        if (vistaPrevia != null) java.nio.file.Files.writeString(java.nio.file.Path.of(vistaPrevia), resultado.getResponse().getContentAsString());
    }

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
