package gt.edu.uvg.ecouvg.servicio;

import java.util.List;
import java.util.Map;

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

    private final RestClient restClient = RestClient.create();

    public String generarRespuesta(String pregunta) {

        String instrucciones = """
                Eres EcoBot, el asistente virtual de EcoUVG.
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
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", pregunta)
                                )
                        )
                )
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
                return "No recibí una respuesta de Gemini.";
            }

            JsonNode texto = respuesta.path("candidates")
                    .path(0)
                    .path("content")
                    .path("parts")
                    .path(0)
                    .path("text");

            if (!texto.isString()) {
                return "No pude generar una respuesta.";
            }

            return texto.asString();

        } catch (Exception e) {
            System.out.println("Error en Gemini: " + e.getMessage());
            return "Lo siento, ocurrió un error. Intenta nuevamente.";
        }

    }
}
