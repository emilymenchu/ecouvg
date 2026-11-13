package gt.edu.uvg.ecouvg.modelo;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name="mision_arcade", uniqueConstraints=@UniqueConstraint(name="uk_mision_arcade_semana",columnNames={"usuario_id","semana"}))
public class MisionArcade {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="usuario_id",nullable=false) private Usuario usuario;
    @Column(nullable=false) private LocalDate semana;
    @Column(nullable=false) private LocalDate fecha;
    @Column(nullable=false,length=20) private String juego;
    protected MisionArcade() { }
    public MisionArcade(Usuario usuario,LocalDate semana,LocalDate fecha,String juego) {
        this.usuario=usuario;this.semana=semana;this.fecha=fecha;this.juego=juego;
    }
}
