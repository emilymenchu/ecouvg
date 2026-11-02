package gt.edu.uvg.ecouvg.servicio;

import java.util.List;
import java.util.Map;
import java.time.Duration;
import java.net.http.HttpClient;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.client.RestClientException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

@Service
public class ServicioGemini {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.modelo}")
    private String modelo;

    private final RestClient restClient;

    public ServicioGemini() {
        var factory = new JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build());
        factory.setReadTimeout(Duration.ofSeconds(25));
        restClient = RestClient.builder().requestFactory(factory).build();
    }

    public String generarRespuesta(List<Map<String, Object>> contexto) {

        String instrucciones = """
                Eres Eco, la tortuga y asistente virtual de EcoUVG.
                Ayudas a estudiantes con preguntas sobre reciclaje.
                Responde en español de forma clara y amigable.
                Explica cómo clasificar correctamente los residuos.
                No inventes ubicaciones de centros de reciclaje.
                Si no sabes algo, indícalo.
                """;

        Map<String, Object> solicitud = Map.of(
                "systemInstruction", Map.of(
                        "parts", List.of(
                                Map.of("text", instrucciones)
                        )
                ),
                "contents", contexto
        );

        
        try {
            JsonNode respuesta = restClient.post()
                    .uri("https://generativelanguage.googleapis.com/v1beta/models/"
                            + modelo + ":generateContent")
                    .header("x-goog-api-key", apiKey)
                    .body(solicitud)
                    .retrieve()
                    .body(JsonNode.class);

            if (respuesta == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Eco no respondió");
            }

            JsonNode texto = respuesta.path("candidates")
                    .path(0)
                    .path("content")
                    .path("parts")
                    .path(0)
                    .path("text");

            if (!texto.isString()) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Eco no pudo generar una respuesta");
            }

            return texto.asString();

        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "No se pudo consultar a Eco");
        }

    }
}
