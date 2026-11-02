package gt.edu.uvg.ecouvg.servicio;
import java.time.LocalDate; 

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gt.edu.uvg.ecouvg.modelo.Evidencia;
import gt.edu.uvg.ecouvg.repositorio.RepositorioEvidencia; 


@Service
public class ServicioExpirarEvidencia {
    private final RepositorioEvidencia repositorioEvidencia; 
    public ServicioExpirarEvidencia (RepositorioEvidencia repositorioEvidencia){
        this.repositorioEvidencia = repositorioEvidencia; 
    }

    @Transactional 
    public String expirarEvidencia(Long id, LocalDate hoy){
        Evidencia evidencia = repositorioEvidencia.buscarParaRevision(id).orElseThrow(() -> new IllegalArgumentException("Evidencia no encontrada :( "));

        if (!evidencia.estaPendiente()|| !evidencia.estaVencida(hoy)) {
            return null;
        }

        evidencia.expirar();
        repositorioEvidencia.save(evidencia);
        return evidencia.getRutaFotografia();
    }
    
}
