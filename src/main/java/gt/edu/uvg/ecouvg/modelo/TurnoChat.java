package gt.edu.uvg.ecouvg.modelo;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Una pregunta y su respuesta se guardan juntas para evitar intercambios incompletos. */
@Entity
@Table(name = "turnos_chat", uniqueConstraints = @UniqueConstraint(name = "uk_chat_solicitud", columnNames = {"conversacion_id", "solicitud_id"}))
public class TurnoChat {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversacion_id", nullable = false)
    private ConversacionChat conversacion;
    @Column(name = "solicitud_id", nullable = false)
    private UUID solicitudId;
    @Column(nullable = false, length = 1000)
    private String pregunta;
    @Column(nullable = false, columnDefinition = "text")
    private String respuesta;
    @Column(nullable = false)
    private Instant creado;

    protected TurnoChat() {}
    public TurnoChat(ConversacionChat conversacion, UUID solicitudId, String pregunta, String respuesta) {
        this.conversacion = conversacion;
        this.solicitudId = solicitudId;
        this.pregunta = pregunta;
        this.respuesta = respuesta;
        this.creado = Instant.now();
    }
    public Long getId() { return id; }
    public String getPregunta() { return pregunta; }
    public String getRespuesta() { return respuesta; }
    public Instant getCreado() { return creado; }
}
