package gt.edu.uvg.ecouvg.modelo;
import java.time.LocalDateTime;
 
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "revision_evidencia", uniqueConstraints = @UniqueConstraint(columnNames = {"evidencia_id", "revisor_id"}))

public class RevisionEvidencia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "evidencia_id", nullable = false)
    private Evidencia evidencia;
    @ManyToOne(optional = false)
    @JoinColumn(name = "revisor_id", nullable = false)
    private Usuario revisor;
    private boolean aprobada;
    private LocalDateTime fechaRevision; 

    public RevisionEvidencia(){

    }

    public RevisionEvidencia(Evidencia evidencia, Usuario revisor, boolean aprobada){
        
        if (evidencia == null || revisor == null) {
            throw new IllegalArgumentException("Evidencia y revisor son obligatorios :)"); 
        }
        Usuario autor = evidencia.getUsuario(); 
        
        if (autor == null || autor.getId() == null || revisor.getId() == null){
            throw new IllegalArgumentException("Los usuarios deben estar registrados "); 
        }
        if(autor.getId().equals(revisor.getId())){
            throw new IllegalArgumentException("No puedes verificar evidencia propia"); 
        
        }

        this.evidencia = evidencia; 
        this.revisor = revisor; 
        this.aprobada = aprobada; 
        this.fechaRevision = LocalDateTime.now(); 
    }

    public Long getId(){
        return id; 
    }

    public Evidencia getEvidencia(){
        return evidencia; 
    }

    public Usuario getRevisor(){
        return revisor; 
    }

    public boolean estaAprobada(){
        return aprobada; 
    }

    public LocalDateTime getFechaRevision(){
        return fechaRevision; 
    }

}
