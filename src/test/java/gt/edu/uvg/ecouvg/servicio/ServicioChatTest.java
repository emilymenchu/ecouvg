package gt.edu.uvg.ecouvg.servicio;

import gt.edu.uvg.ecouvg.modelo.*;
import gt.edu.uvg.ecouvg.repositorio.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.mockito.ArgumentCaptor;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DataJpaTest(showSql = false, properties = {
    "spring.datasource.url=jdbc:h2:mem:chat_test", "spring.datasource.username=sa", "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop", "spring.jpa.show-sql=false"
})
@Import(ServicioChat.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class ServicioChatTest {
    @Autowired ServicioChat chat;
    @Autowired RepositorioUsuario usuarios;
    @Autowired RepositorioConversacionChat conversaciones;
    @Autowired RepositorioTurnoChat turnos;
    @MockitoBean ServicioGemini gemini;
    private Usuario pilar;
    private Usuario emily;

    @BeforeEach void preparar() {
        turnos.deleteAll();
        conversaciones.deleteAll();
        usuarios.deleteAll();
        pilar = usuarios.save(new Usuario("Pilar", "123", "pilar@example.com", "clave"));
        emily = usuarios.save(new Usuario("Emily", "456", "emily@example.com", "clave"));
        when(gemini.generarRespuesta(anyList())).thenReturn("Separa las tapas.");
    }

    @Test void guardaYRecuperaMensajesDespuesDelCommit() {
        ConversacionChat c = chat.crear(pilar);
        chat.preguntar(pilar.getId(), c.getId(), UUID.randomUUID(), "¿Cómo reciclo vidrio?");
        List<TurnoChat> guardados = chat.leer(c.getId(), pilar.getId());
        assertEquals(1, guardados.size());
        assertEquals("¿Cómo reciclo vidrio?", guardados.getFirst().getPregunta());
        assertEquals("Separa las tapas.", guardados.getFirst().getRespuesta());
        assertEquals("¿Cómo reciclo vidrio?", chat.listar(pilar.getId()).getFirst().getTitulo());
    }

    @Test void otraCuentaNoPuedeLeerNiEscribirEnLaConversacion() {
        ConversacionChat c = chat.crear(pilar);
        assertTrue(chat.listar(emily.getId()).isEmpty());
        assertEquals(404, assertThrows(ResponseStatusException.class, () -> chat.leer(c.getId(), emily.getId())).getStatusCode().value());
        assertEquals(404, assertThrows(ResponseStatusException.class,
            () -> chat.preguntar(emily.getId(), c.getId(), UUID.randomUUID(), "hola")).getStatusCode().value());
        verifyNoInteractions(gemini);
    }

    @Test void reintentarLaMismaSolicitudNoDuplicaNiVuelveAConsultarLaIA() {
        ConversacionChat c = chat.crear(pilar);
        UUID solicitud = UUID.randomUUID();
        TurnoChat uno = chat.preguntar(pilar.getId(), c.getId(), solicitud, "vidrio");
        TurnoChat dos = chat.preguntar(pilar.getId(), c.getId(), solicitud, "vidrio");
        assertEquals(uno.getId(), dos.getId());
        assertEquals(1, turnos.count());
        verify(gemini, times(1)).generarRespuesta(anyList());
    }

    @Test void falloDeIARevierteElIntercambioYPermiteReintentar() {
        ConversacionChat c = chat.crear(pilar);
        UUID solicitud = UUID.randomUUID();
        when(gemini.generarRespuesta(anyList())).thenThrow(new ResponseStatusException(HttpStatus.BAD_GATEWAY));
        assertThrows(ResponseStatusException.class, () -> chat.preguntar(pilar.getId(), c.getId(), solicitud, "vidrio"));
        assertEquals(0, turnos.count());
        assertEquals("Nueva conversación", chat.listar(pilar.getId()).getFirst().getTitulo());
        doReturn("Respuesta recuperada").when(gemini).generarRespuesta(anyList());
        chat.preguntar(pilar.getId(), c.getId(), solicitud, "vidrio");
        assertEquals(1, chat.leer(c.getId(), pilar.getId()).size());
    }

    @Test void enviaContextoRecienteEnOrdenConRolesAlternados() {
        ConversacionChat c = chat.crear(pilar);
        for (int i = 0; i < 12; i++) chat.preguntar(pilar.getId(), c.getId(), UUID.randomUUID(), "pregunta " + i);
        @SuppressWarnings("unchecked") ArgumentCaptor<List<Map<String, Object>>> captor = ArgumentCaptor.forClass(List.class);
        verify(gemini, times(12)).generarRespuesta(captor.capture());
        List<Map<String, Object>> contexto = captor.getValue();
        assertEquals(21, contexto.size());
        assertEquals("user", contexto.getFirst().get("role"));
        assertEquals(List.of(Map.of("text", "pregunta 1")), contexto.getFirst().get("parts"));
        assertEquals("model", contexto.get(1).get("role"));
        assertEquals(List.of(Map.of("text", "pregunta 11")), contexto.getLast().get("parts"));
        assertEquals(12, chat.leer(c.getId(), pilar.getId()).size());
    }

    @Test void solicitudesInvalidasNoGuardanNiConsultanLaIA() {
        ConversacionChat c = chat.crear(pilar);
        for (String pregunta : new String[]{"", " ", "x".repeat(1001)}) {
            assertThrows(ResponseStatusException.class, () -> chat.preguntar(pilar.getId(), c.getId(), UUID.randomUUID(), pregunta));
        }
        assertEquals(0, turnos.count());
        verifyNoInteractions(gemini);
    }

    @Test void enviosConcurrentesDeLaMismaSolicitudCreanUnSoloTurno() throws Exception {
        ConversacionChat c = chat.crear(pilar);
        UUID solicitud = UUID.randomUUID();
        try (var ejecutor = Executors.newFixedThreadPool(2)) {
            Callable<Long> enviar = () -> chat.preguntar(pilar.getId(), c.getId(), solicitud, "vidrio").getId();
            var uno = ejecutor.submit(enviar);
            var dos = ejecutor.submit(enviar);
            assertEquals(uno.get(10, TimeUnit.SECONDS), dos.get(10, TimeUnit.SECONDS));
        }
        assertEquals(1, turnos.count());
        verify(gemini, times(1)).generarRespuesta(anyList());
    }
}
