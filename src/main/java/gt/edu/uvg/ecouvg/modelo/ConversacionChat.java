package gt.edu.uvg.ecouvg.modelo;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "conversaciones_chat", indexes = @Index(name = "idx_chat_usuario_fecha", columnList = "usuario_id,actualizada"))
public class ConversacionChat {
    @Id
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    @Column(nullable = false, length = 100)
    private String titulo;
    @Column(nullable = false)
    private Instant actualizada;

    protected ConversacionChat() {}
    public ConversacionChat(Usuario usuario) {
        this.id = UUID.randomUUID();
        this.usuario = usuario;
        this.titulo = "Nueva conversación";
        this.actualizada = Instant.now();
    }
    public UUID getId() { return id; }
    public String getTitulo() { return titulo; }
    public Instant getActualizada() { return actualizada; }
    public void registrarTurno(String primeraPregunta) {
        if (titulo.equals("Nueva conversación")) {
            titulo = primeraPregunta.replaceAll("\\s+", " ");
            if (titulo.codePointCount(0, titulo.length()) > 80) titulo = titulo.substring(0, titulo.offsetByCodePoints(0, 80)) + "…";
        }
        actualizada = Instant.now();
    }
}
