package gt.edu.uvg.ecouvg.controlador;

import gt.edu.uvg.ecouvg.modelo.Ranking;
import gt.edu.uvg.ecouvg.modelo.Usuario;
import gt.edu.uvg.ecouvg.servicio.ServicioReto;
import gt.edu.uvg.ecouvg.servicio.ServicioUsuario;
import gt.edu.uvg.ecouvg.servicio.ServicioPerfil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.mock.web.MockMultipartFile;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ControladorPerfilTest {
    private ServicioUsuario usuarios;
    private ServicioReto retos;
    private ServicioPerfil perfil;
    private MockMvc mvc;
    private Usuario usuario;

    @BeforeEach void preparar() {
        usuarios = mock(ServicioUsuario.class);
        retos = mock(ServicioReto.class);
        perfil = mock(ServicioPerfil.class);
        usuario = new Usuario("Pilar", "123", "pilar@example.com", "clave");
        ReflectionTestUtils.setField(usuario, "id", 1L);
        usuario.sumarPuntos(750);
        when(usuarios.buscarPorCarnet("123")).thenReturn(usuario);
        when(retos.contarMisionesCompletadas(1L)).thenReturn(28L);
        Ranking ranking = new Ranking(4);
        ranking.agregarUsuario(usuario);
        when(usuarios.obtenerRanking(4)).thenReturn(ranking);

        ClassLoaderTemplateResolver plantillas = new ClassLoaderTemplateResolver();
        plantillas.setPrefix("templates/");
        plantillas.setSuffix(".html");
        plantillas.setCharacterEncoding("UTF-8");
        SpringTemplateEngine motor = new SpringTemplateEngine();
        motor.setTemplateResolver(plantillas);
        ThymeleafViewResolver vistas = new ThymeleafViewResolver();
        vistas.setTemplateEngine(motor);
        vistas.setCharacterEncoding("UTF-8");
        mvc = MockMvcBuilders.standaloneSetup(new ControladorPerfil(usuarios, retos, perfil))
            .setViewResolvers(vistas).build();
    }

    @Test void renderizaPerfilConDatosRealesYMenus() throws Exception {
        var resultado = mvc.perform(get("/perfil").principal(autenticacion()))
            .andExpect(status().isOk())
            .andExpect(model().attribute("misionesCompletadas", 28L))
            .andExpect(content().string(containsString(usuario.getNombreNivel())))
            .andExpect(content().string(containsString("value=\"50\"")))
            .andExpect(content().string(containsString("mi-posicion")))
            .andExpect(content().string(containsString("aria-current=\"page\"")))
            .andExpect(content().string(containsString("/js/navegacion.js")))
            .andReturn();
        String vistaPrevia = System.getProperty("perfil.preview");
        if (vistaPrevia != null) {
            java.nio.file.Files.writeString(java.nio.file.Path.of(vistaPrevia), resultado.getResponse().getContentAsString());
        }
    }

    @Test void renderizaNivelVacioSinInventarParticipantes() throws Exception {
        when(usuarios.obtenerRanking(2)).thenReturn(new Ranking(2));
        mvc.perform(get("/perfil").param("nivel", "2").principal(autenticacion()))
            .andExpect(status().isOk())
            .andExpect(model().attribute("nivelSeleccionado", 2))
            .andExpect(content().string(containsString("Aún no hay participantes")))
            .andExpect(content().string(not(containsString("class=\"mi-posicion\""))));
    }

    @Test void nivelInvalidoConservaElNivelDelUsuario() throws Exception {
        mvc.perform(get("/perfil").param("nivel", "7").principal(autenticacion()))
            .andExpect(status().isOk()).andExpect(model().attribute("nivelSeleccionado", 4));
    }

    @Test void nivelMaximoMuestraMensajeSinSiguienteMeta() throws Exception {
        usuario.sumarPuntos(9250);
        Ranking ranking = new Ranking(6);
        ranking.agregarUsuario(usuario);
        when(usuarios.obtenerRanking(6)).thenReturn(ranking);
        mvc.perform(get("/perfil").principal(autenticacion()))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("¡Alcanzaste el nivel máximo!")))
            .andExpect(content().string(containsString("value=\"100\"")));
    }

    @Test void sinSesionRedirigeAlLogin() throws Exception {
        mvc.perform(get("/perfil")).andExpect(redirectedUrl("/login"));
        verifyNoInteractions(usuarios, retos);
    }

    @Test void renderizaFotoGuardadaYFormularioMultipart() throws Exception {
        usuario.setFotoPerfil("usuario_1/foto.jpg");
        mvc.perform(get("/perfil").principal(autenticacion()))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("src=\"/perfil/foto\"")))
            .andExpect(content().string(containsString("enctype=\"multipart/form-data\"")));
    }

    @Test void guardaSoloElPerfilDeLaSesion() throws Exception {
        MockMultipartFile foto = new MockMultipartFile("fotografia", "foto.jpg", "image/jpeg", new byte[]{1});
        mvc.perform(multipart("/perfil/foto").file(foto)
                .param("id", "999").param("puntos", "99999").principal(autenticacion()))
            .andExpect(redirectedUrl("/perfil"))
            .andExpect(flash().attribute("mensaje", "Tu foto de perfil se actualizó correctamente."));
        verify(perfil).actualizarFoto("123", foto);
    }

    @Test void errorDeFotoMuestraValidacion() throws Exception {
        doThrow(new IllegalArgumentException("Selecciona una fotografía.")).when(perfil).actualizarFoto("123", null);
        mvc.perform(multipart("/perfil/foto").principal(autenticacion()))
            .andExpect(redirectedUrl("/perfil"))
            .andExpect(flash().attribute("error", "Selecciona una fotografía."));
    }

    @Test void errorDeStorageMuestraMensajeSinDetallesInternos() throws Exception {
        doThrow(new org.springframework.web.client.ResourceAccessException("clave interna"))
            .when(perfil).actualizarFoto("123", null);
        mvc.perform(multipart("/perfil/foto").principal(autenticacion()))
            .andExpect(redirectedUrl("/perfil"))
            .andExpect(flash().attribute("error", "No se pudo guardar la foto. Inténtalo de nuevo."));
    }

    @Test void noPermiteEditarSinSesion() throws Exception {
        mvc.perform(multipart("/perfil/foto"))
            .andExpect(redirectedUrl("/login"));
        verifyNoInteractions(perfil);
    }

    @Test void sirveLaFotoDeLaSesionSinCache() throws Exception {
        byte[] imagen = new byte[]{1, 2, 3};
        when(perfil.obtenerFoto("123")).thenReturn(imagen);
        mvc.perform(get("/perfil/foto").principal(autenticacion()))
            .andExpect(status().isOk()).andExpect(content().contentType("image/jpeg"))
            .andExpect(header().string("Cache-Control", "no-store"))
            .andExpect(content().bytes(imagen));
    }

    @Test void sinFotoDevuelve404YSinSesion401() throws Exception {
        mvc.perform(get("/perfil/foto").principal(autenticacion())).andExpect(status().isNotFound());
        mvc.perform(get("/perfil/foto")).andExpect(status().isUnauthorized());
    }

    @Test void falloAlLeerStorageNoDevuelveHtmlComoImagen() throws Exception {
        when(perfil.obtenerFoto("123")).thenThrow(new org.springframework.web.client.ResourceAccessException("fallo remoto"));
        mvc.perform(get("/perfil/foto").principal(autenticacion()))
            .andExpect(status().isBadGateway()).andExpect(content().string(""));
    }

    @Test void formularioSoloPermiteFotoYContrasena() throws Exception {
        mvc.perform(get("/perfil").principal(autenticacion()))
            .andExpect(content().string(containsString("aria-label=\"Cambiar foto de perfil\"")))
            .andExpect(content().string(containsString("action=\"/perfil/contrasena\"")))
            .andExpect(content().string(not(containsString("name=\"nombre\""))));
    }

    @Test void cambiaContrasenaDeLaSesion() throws Exception {
        mvc.perform(post("/perfil/contrasena").principal(autenticacion())
                .param("contrasenaActual", "actual123").param("nuevaContrasena", "NuevaClave123")
                .param("confirmarContrasena", "NuevaClave123"))
            .andExpect(redirectedUrl("/perfil"))
            .andExpect(flash().attribute("mensaje", "Tu contraseña se actualizó correctamente."));
        verify(perfil).cambiarContrasena("123", "actual123", "NuevaClave123", "NuevaClave123");
    }

    @Test void falloDeContrasenaNoConservaClavesEnFlash() throws Exception {
        doThrow(new IllegalArgumentException("La contraseña actual es incorrecta."))
            .when(perfil).cambiarContrasena("123", "incorrecta", "NuevaClave123", "NuevaClave123");
        mvc.perform(post("/perfil/contrasena").principal(autenticacion())
                .param("contrasenaActual", "incorrecta").param("nuevaContrasena", "NuevaClave123")
                .param("confirmarContrasena", "NuevaClave123"))
            .andExpect(redirectedUrl("/perfil"))
            .andExpect(flash().attribute("errorContrasena", "La contraseña actual es incorrecta."))
            .andExpect(flash().attributeCount(1));
    }

    @Test void noPermiteCambiarContrasenaSinSesion() throws Exception {
        mvc.perform(post("/perfil/contrasena").param("contrasenaActual", "actual123")
                .param("nuevaContrasena", "NuevaClave123").param("confirmarContrasena", "NuevaClave123"))
            .andExpect(redirectedUrl("/login"));
        verifyNoInteractions(perfil);
    }

    private UsernamePasswordAuthenticationToken autenticacion() {
        return new UsernamePasswordAuthenticationToken("123", "clave");
    }
}
