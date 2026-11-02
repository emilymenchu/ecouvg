package gt.edu.uvg.ecouvg.modelo;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "seguimientos", uniqueConstraints = @UniqueConstraint(name = "uk_seguimiento", columnNames = {"seguidor_id", "seguido_id"}),
    indexes = @Index(name = "idx_seguimiento_seguido", columnList = "seguido_id"))
public class Seguimiento {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seguidor_id", nullable = false)
    private Usuario seguidor;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seguido_id", nullable = false)
    private Usuario seguido;
    @Column(nullable = false)
    private Instant creado;

    protected Seguimiento() {}
    public Seguimiento(Usuario seguidor, Usuario seguido) {
        if (seguidor.getId().equals(seguido.getId())) throw new IllegalArgumentException("No puedes seguirte a ti mismo.");
        this.seguidor = seguidor;
        this.seguido = seguido;
        this.creado = Instant.now();
    }
}
