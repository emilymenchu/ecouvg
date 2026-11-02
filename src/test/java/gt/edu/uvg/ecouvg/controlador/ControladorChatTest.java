package gt.edu.uvg.ecouvg.controlador;

import gt.edu.uvg.ecouvg.modelo.Usuario;
import gt.edu.uvg.ecouvg.servicio.ServicioChat;
import gt.edu.uvg.ecouvg.modelo.ConversacionChat;
import gt.edu.uvg.ecouvg.modelo.TurnoChat;
import java.util.*;
import org.springframework.test.util.ReflectionTestUtils;
import gt.edu.uvg.ecouvg.servicio.ServicioUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ControladorChatTest {
    private ServicioUsuario usuarios;
    private MockMvc mvc;
    private Usuario usuario;
    private ServicioChat chats;

    @BeforeEach void preparar() {
        usuarios = mock(ServicioUsuario.class);
        chats = mock(ServicioChat.class);
        usuario = new Usuario("Pilar", "123", "pilar@example.com", "clave");
        ReflectionTestUtils.setField(usuario, "id", 1L);
        when(usuarios.buscarPorCarnet("123")).thenReturn(usuario);
        ClassLoaderTemplateResolver plantillas = new ClassLoaderTemplateResolver();
        plantillas.setPrefix("templates/");
        plantillas.setSuffix(".html");
        plantillas.setCharacterEncoding("UTF-8");
        SpringTemplateEngine motor = new SpringTemplateEngine();
        motor.setTemplateResolver(plantillas);
        ThymeleafViewResolver vistas = new ThymeleafViewResolver();
        vistas.setTemplateEngine(motor);
        vistas.setCharacterEncoding("UTF-8");
        mvc = MockMvcBuilders.standaloneSetup(new ControladorChat(chats, usuarios))
            .setViewResolvers(vistas).build();
    }

    @Test void renderizaEcoYSusAvataresConLaFotoDelUsuario() throws Exception {
        usuario.setFotoPerfil("usuario_1/foto.jpg");
        mvc.perform(get("/chat").principal(new UsernamePasswordAuthenticationToken("123", "clave")))
            .andExpect(status().isOk()).andExpect(view().name("chat"))
            .andExpect(model().attribute("usuario", usuario))
            .andExpect(content().string(containsString("<h1 id=\"titulo-chat\">Eco</h1>")))
            .andExpect(content().string(not(containsString("EcoBot"))))
            .andExpect(content().string(containsString("id=\"avatar-eco\"")))
            .andExpect(content().string(containsString("src=\"/perfil/foto\"")))
            .andExpect(content().string(containsString("data-url=\"/chat/preguntar\"")))
            .andExpect(content().string(containsString("aria-current=\"page\"")));
    }

    @Test void sinFotoUsaLaInicial() throws Exception {
        mvc.perform(get("/chat").principal(new UsernamePasswordAuthenticationToken("123", "clave")))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("class=\"inicial-usuario\" aria-hidden=\"true\">P</span>")))
            .andExpect(content().string(not(containsString("src=\"/perfil/foto\""))));
    }

    @Test void sinSesionRedirigeAlLogin() throws Exception {
        mvc.perform(get("/chat")).andExpect(redirectedUrl("/login"));
        verifyNoInteractions(usuarios);
    }

    @Test void usuarioInexistenteRedirigeAlLogin() throws Exception {
        mvc.perform(get("/chat").principal(new UsernamePasswordAuthenticationToken("otro", "clave")))
            .andExpect(redirectedUrl("/login"));
    }
    @Test void recuperaLaUltimaConversacionYRenderizaSusMensajes() throws Exception {
        ConversacionChat conversacion = new ConversacionChat(usuario);
        conversacion.registrarTurno("¿Cómo reciclo vidrio?");
        TurnoChat turno = new TurnoChat(conversacion, UUID.randomUUID(), "¿Cómo reciclo vidrio?", "**Separa** las tapas.");
        when(chats.listar(1L)).thenReturn(List.of(conversacion));
        when(chats.leer(conversacion.getId(), 1L)).thenReturn(List.of(turno));
        mvc.perform(get("/chat").principal(new UsernamePasswordAuthenticationToken("123", "clave")))
            .andExpect(status().isOk())
            .andExpect(model().attribute("conversacionId", conversacion.getId()))
            .andExpect(content().string(containsString("¿Cómo reciclo vidrio?")))
            .andExpect(content().string(containsString("**Separa** las tapas.")))
            .andExpect(content().string(containsString("data-respuesta-guardada")));
    }

    @Test void nuevoChatNoCargaUnaConversacionAnterior() throws Exception {
        when(chats.listar(1L)).thenReturn(List.of(new ConversacionChat(usuario)));
        mvc.perform(get("/chat").param("nuevo", "true").principal(new UsernamePasswordAuthenticationToken("123", "clave")))
            .andExpect(status().isOk()).andExpect(model().attribute("turnos", empty()));
        verify(chats, never()).leer(any(), any());
    }

    @Test void solicitudSinSesionNoPuedeGuardarMensajes() throws Exception {
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/chat/preguntar")
                .contentType("application/json").content("{\"pregunta\":\"hola\"}"))
            .andExpect(status().isUnauthorized());
        verifyNoInteractions(chats);
    }

}
