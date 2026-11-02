package gt.edu.uvg.ecouvg.servicio;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class ServicioGeminiTest {
    private ServicioGemini gemini;
    private MockRestServiceServer servidor;
    @BeforeEach void preparar() {
        RestClient.Builder builder = RestClient.builder();
        servidor = MockRestServiceServer.bindTo(builder).build();
        gemini = new ServicioGemini();
        ReflectionTestUtils.setField(gemini, "restClient", builder.build());
        ReflectionTestUtils.setField(gemini, "apiKey", "clave-prueba");
        ReflectionTestUtils.setField(gemini, "modelo", "modelo-prueba");
    }
    @Test void enviaElHistorialComoContentsYRecuperaElTexto() {
        servidor.expect(requestTo("https://generativelanguage.googleapis.com/v1beta/models/modelo-prueba:generateContent"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(jsonPath("$.contents[0].role").value("user"))
            .andExpect(jsonPath("$.contents[1].role").value("model"))
            .andExpect(jsonPath("$.contents[2].parts[0].text").value("¿Y las tapas?"))
            .andRespond(withSuccess("{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"Separa las tapas.\"}]}}]}", MediaType.APPLICATION_JSON));
        assertEquals("Separa las tapas.", gemini.generarRespuesta(List.of(
            contenido("user", "vidrio"), contenido("model", "Lava el envase."), contenido("user", "¿Y las tapas?"))));
        servidor.verify();
    }
    @Test void errorRemotoSePropagaParaNoGuardarUnFalsoMensajeDeEco() {
        servidor.expect(anything()).andRespond(withServerError());
        assertEquals(502, assertThrows(ResponseStatusException.class,
            () -> gemini.generarRespuesta(List.of(contenido("user", "hola")))).getStatusCode().value());
        servidor.verify();
    }
    private Map<String, Object> contenido(String rol, String texto) {
        return Map.of("role", rol, "parts", List.of(Map.of("text", texto)));
    }
}
