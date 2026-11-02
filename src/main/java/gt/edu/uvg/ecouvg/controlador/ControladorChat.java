package gt.edu.uvg.ecouvg.controlador;

import gt.edu.uvg.ecouvg.dto.SolicitudChat;
import gt.edu.uvg.ecouvg.dto.RespuestaChat;
import gt.edu.uvg.ecouvg.servicio.ServicioChat;
import gt.edu.uvg.ecouvg.servicio.ServicioUsuario;
import gt.edu.uvg.ecouvg.modelo.*;
import java.util.*;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class ControladorChat {
    private final ServicioChat servicioChat;
    private final ServicioUsuario servicioUsuario;

    public ControladorChat(ServicioChat servicioChat, ServicioUsuario servicioUsuario) {
        this.servicioChat = servicioChat;
        this.servicioUsuario = servicioUsuario;
    }

    @GetMapping("/chat")
    public String mostrarChat(Authentication authentication, Model model,
                             @RequestParam(required = false) UUID conversacion,
                             @RequestParam(defaultValue = "false") boolean nuevo) {
        if (authentication == null) return "redirect:/login";
        Usuario usuario = servicioUsuario.buscarPorCarnet(authentication.getName());
        if (usuario == null) return "redirect:/login";
        List<ConversacionChat> historial = servicioChat.listar(usuario.getId());
        UUID seleccionada = conversacion;
        if (seleccionada == null && !nuevo && !historial.isEmpty()) seleccionada = historial.getFirst().getId();
        model.addAttribute("usuario", usuario);
        model.addAttribute("conversaciones", historial);
        model.addAttribute("conversacionId", seleccionada);
        model.addAttribute("turnos", seleccionada == null ? List.of() : servicioChat.leer(seleccionada, usuario.getId()));
        return "chat";
    }

    @PostMapping("/chat/conversaciones")
    @ResponseBody
    public Map<String, UUID> crear(Authentication authentication) {
        return Map.of("id", servicioChat.crear(usuarioActual(authentication)).getId());
    }

    @PostMapping("/chat/preguntar")
    @ResponseBody
    public RespuestaChat preguntar(Authentication authentication, @RequestBody SolicitudChat solicitud) {
        Usuario usuario = usuarioActual(authentication);
        TurnoChat turno = servicioChat.preguntar(usuario.getId(), solicitud.getConversacionId(), solicitud.getSolicitudId(), solicitud.getPregunta());
        return new RespuestaChat(turno.getRespuesta());
    }

    private Usuario usuarioActual(Authentication authentication) {
        if (authentication == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        Usuario usuario = servicioUsuario.buscarPorCarnet(authentication.getName());
        if (usuario == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return usuario;
    }
}
