package gt.edu.uvg.ecouvg.servicio;

import gt.edu.uvg.ecouvg.modelo.*;
import gt.edu.uvg.ecouvg.repositorio.*;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ServicioChat {
    private final RepositorioConversacionChat conversaciones;
    private final RepositorioTurnoChat turnos;
    private final ServicioGemini gemini;

    public ServicioChat(RepositorioConversacionChat conversaciones, RepositorioTurnoChat turnos, ServicioGemini gemini) {
        this.conversaciones = conversaciones;
        this.turnos = turnos;
        this.gemini = gemini;
    }
    @Transactional
    public ConversacionChat crear(Usuario usuario) {
        return conversaciones.save(new ConversacionChat(usuario));
    }
    @Transactional(readOnly = true)
    public List<ConversacionChat> listar(Long usuarioId) {
        return conversaciones.findByUsuarioIdOrderByActualizadaDesc(usuarioId);
    }
    @Transactional(readOnly = true)
    public List<TurnoChat> leer(UUID id, Long usuarioId) {
        conversaciones.findByIdAndUsuarioId(id, usuarioId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return turnos.findByConversacionIdOrderByIdAsc(id);
    }
    @Transactional
    public TurnoChat preguntar(Long usuarioId, UUID conversacionId, UUID solicitudId, String pregunta) {
        if (conversacionId == null || solicitudId == null || pregunta == null || pregunta.isBlank() || pregunta.length() > 1000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solicitud de chat inválida");
        }
        String texto = pregunta.strip();
        // Serializa los envíos de una misma conversación, incluso desde varias pestañas.
        ConversacionChat conversacion = conversaciones.bloquearPropia(conversacionId, usuarioId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Optional<TurnoChat> existente = turnos.findByConversacionIdAndSolicitudId(conversacionId, solicitudId);
        if (existente.isPresent()) {
            if (!existente.get().getPregunta().equals(texto)) throw new ResponseStatusException(HttpStatus.CONFLICT);
            return existente.get();
        }
        List<Map<String, Object>> contexto = new ArrayList<>();
        List<TurnoChat> recientes = new ArrayList<>(turnos.findTop10ByConversacionIdOrderByIdDesc(conversacionId));
        Collections.reverse(recientes);
        for (TurnoChat turno : recientes) {
            contexto.add(contenido("user", turno.getPregunta()));
            contexto.add(contenido("model", turno.getRespuesta()));
        }
        contexto.add(contenido("user", texto));
        String respuesta = gemini.generarRespuesta(contexto);
        if (respuesta == null || respuesta.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_GATEWAY);
        TurnoChat turno = turnos.save(new TurnoChat(conversacion, solicitudId, texto, respuesta));
        conversacion.registrarTurno(texto);
        return turno;
    }
    private Map<String, Object> contenido(String rol, String texto) {
        return Map.of("role", rol, "parts", List.of(Map.of("text", texto)));
    }
}
