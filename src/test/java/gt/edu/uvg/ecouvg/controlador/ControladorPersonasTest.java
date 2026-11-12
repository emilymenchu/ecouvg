package gt.edu.uvg.ecouvg.controlador;

import gt.edu.uvg.ecouvg.modelo.Usuario;
import gt.edu.uvg.ecouvg.servicio.*;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ControladorPersonasTest {
    private ServicioSocial social;
    private ServicioUsuario usuarios;
    private MockMvc mvc;
    private Usuario emily;
    private final UsernamePasswordAuthenticationToken autenticacion = new UsernamePasswordAuthenticationToken("123", "clave");

    @BeforeEach void preparar() {
        social = mock(ServicioSocial.class);
        usuarios = mock(ServicioUsuario.class);
        Usuario pilar = new Usuario("Pilar", "123", "pilar@example.com", "clave");
        ReflectionTestUtils.setField(pilar, "id", 1L);
        emily = new Usuario("Emily", "456", "correo-privado@example.com", "contrasena-privada");
        ReflectionTestUtils.setField(emily, "id", 2L);
        emily.setFotoPerfil("usuario_2/foto.jpg");
        when(usuarios.buscarPorCarnet("123")).thenReturn(pilar);
        when(usuarios.buscarPorId(2L)).thenReturn(emily);
        ClassLoaderTemplateResolver plantillas = new ClassLoaderTemplateResolver();
        plantillas.setPrefix("templates/"); plantillas.setSuffix(".html"); plantillas.setCharacterEncoding("UTF-8");
        SpringTemplateEngine motor = new SpringTemplateEngine(); motor.setTemplateResolver(plantillas);
        ThymeleafViewResolver vistas = new ThymeleafViewResolver(); vistas.setTemplateEngine(motor); vistas.setCharacterEncoding("UTF-8");
        mvc = MockMvcBuilders.standaloneSetup(new ControladorPersonas(usuarios, social)).setViewResolvers(vistas).build();
    }

    @Test void muestraBusquedaPaginadaConPerfilesYFotosSinDatosPrivados() throws Exception {
        when(social.buscar("Emily", 0)).thenReturn(new PageImpl<>(List.of(emily), PageRequest.of(0, 20), 21));
        mvc.perform(get("/personas").param("q", "Emily").principal(autenticacion))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("/perfil/2/foto")))
            .andExpect(content().string(containsString("/perfil/2")))
            .andExpect(content().string(containsString("pagina=1")))
            .andExpect(content().string(containsString("Emily")))
            .andExpect(content().string(not(containsString("correo-privado@example.com"))))
            .andExpect(content().string(not(containsString("contrasena-privada"))));
    }
    @Test void listaDeAmigosConservaLaRutaDePaginacion() throws Exception {
        when(social.listar(2L, "amigos", 0)).thenReturn(new PageImpl<>(List.of(emily), PageRequest.of(0, 20), 21));
        mvc.perform(get("/perfil/2/amigos").principal(autenticacion))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Amigos de Emily")))
            .andExpect(content().string(containsString("/perfil/2/amigos?q=&amp;pagina=1")));
    }
    @Test void resultadosVaciosTienenMensaje() throws Exception {
        when(social.buscar("nadie", 0)).thenReturn(Page.empty());
        mvc.perform(get("/personas").param("q", "nadie").principal(autenticacion))
            .andExpect(status().isOk()).andExpect(content().string(
                org.hamcrest.Matchers.matchesPattern("(?s).*No\\s+se\\s+encontraron\\s+personas\\..*")));
    }
    @Test void seguirUsaLaCuentaDeLaSesionAunqueSeEnvieOtroId() throws Exception {
        mvc.perform(post("/personas/2/seguir").param("usuarioId", "999").principal(autenticacion))
            .andExpect(redirectedUrl("/perfil/2"));
        verify(social).seguir(1L, 2L);
    }
    @Test void dejarDeSeguirSoloModificaLaRelacionDeLaSesion() throws Exception {
        mvc.perform(post("/personas/2/dejar-seguir").principal(autenticacion))
            .andExpect(redirectedUrl("/perfil/2"));
        verify(social).dejarDeSeguir(1L, 2L);
    }
    @Test void noPermiteAccionesNiBusquedaSinSesion() throws Exception {
        mvc.perform(post("/personas/2/seguir")).andExpect(status().isUnauthorized());
        mvc.perform(post("/personas/2/dejar-seguir")).andExpect(status().isUnauthorized());
        mvc.perform(get("/personas")).andExpect(status().isUnauthorized());
        verifyNoInteractions(social);
    }
    @Test void seguirNoSePuedeEjecutarConUnEnlaceGet() throws Exception {
        mvc.perform(get("/personas/2/seguir").principal(autenticacion)).andExpect(status().isMethodNotAllowed());
        verifyNoInteractions(social);
    }
}
