package gt.edu.uvg.ecouvg.modelo;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id; 
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity 
@Table(name = "evidencia")

public class Evidencia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Usuario usuario;
    @ManyToOne
    private ProgresoReto progresoReto;
    private String rutaFotografia ;
    @Enumerated(EnumType.STRING)
    private EstadoEvidencia estado;
    private LocalDateTime fechaEnvio; 

    protected Evidencia(){
        
    }


    public Evidencia (Usuario usuario, ProgresoReto progresoReto, String rutaFotografia){
        this.usuario = usuario; 
        this.progresoReto = progresoReto; 
        this.rutaFotografia = rutaFotografia; 
        this.estado = EstadoEvidencia.PENDIENTE;
        this.fechaEnvio = LocalDateTime.now(); 
    }

    public Long getId(){
        return id; 
    }

    public Usuario getUsuario(){
        return usuario;
    }

    public ProgresoReto getProgresoReto(){
        return progresoReto; 
    }

    public EstadoEvidencia getEstado() {
        return estado; 
    }

    public LocalDateTime getFechaEnvio(){
        return fechaEnvio; 
    }
    public String getRutaFotografia(){
        return rutaFotografia;
    }

    public boolean estaPendiente() {
        return estado == EstadoEvidencia.PENDIENTE;
    }

    public void aprobar() {
        if (!estaPendiente()){
            throw new IllegalStateException("Solo las evidencias pedientes pueden aprobarse :)"); 
        }

        this.estado = EstadoEvidencia.APROBADA;
    }

    public void rechazar() {
        if (!estaPendiente()){
            throw new IllegalStateException("Solo las evidencias pedientes pueden rechazarse :)"); 
        }

        this.estado = EstadoEvidencia.RECHAZADA;
    }

}
