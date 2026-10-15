
package gt.edu.uvg.ecouvg.controlador;

import gt.edu.uvg.ecouvg.dto.SolicitudChat;
import gt.edu.uvg.ecouvg.dto.RespuestaChat;
import gt.edu.uvg.ecouvg.servicio.ServicioGemini;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ControladorChat {

    private final ServicioGemini servicioGemini;

    public ControladorChat(ServicioGemini servicioGemini) {
        this.servicioGemini = servicioGemini;
    }

    @GetMapping("/chat")
    public String mostrarChat() {
        return "chat";
    }

    @PostMapping("/chat/preguntar")
    @ResponseBody
    public RespuestaChat preguntar(@RequestBody SolicitudChat solicitud) {

        String pregunta = solicitud.getPregunta();

        if (pregunta == null || pregunta.isBlank()) {
            return new RespuestaChat("Por favor, escribe una pregunta.");
        }

        if (pregunta.length() > 1000) {
            return new RespuestaChat("La pregunta es demasiado larga.");
        }

        String respuesta = servicioGemini.generarRespuesta(pregunta);

        return new RespuestaChat(respuesta);
    }
}