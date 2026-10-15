package gt.edu.uvg.ecouvg.servicio;
import gt.edu.uvg.ecouvg.modelo.Evidencia;
import gt.edu.uvg.ecouvg.modelo.ProgresoReto;
import gt.edu.uvg.ecouvg.modelo.Usuario;
import gt.edu.uvg.ecouvg.modelo.EstadoEvidencia;

import gt.edu.uvg.ecouvg.repositorio.RepositorioEvidencia;
import gt.edu.uvg.ecouvg.repositorio.RepositorioProgresoReto;
import gt.edu.uvg.ecouvg.repositorio.RepositorioUsuario;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service 
public class ServicioEvidencia {
    private final RepositorioEvidencia repositorioEvidencia; 
    private final RepositorioProgresoReto repositorioProgresoReto; 
    private final RepositorioUsuario repositorioUsuario;
    
    public ServicioEvidencia(RepositorioEvidencia repositorioEvidencia, RepositorioProgresoReto repositorioProgresoReto, RepositorioUsuario repositorioUsuario){
        this.repositorioEvidencia = repositorioEvidencia; 
        this.repositorioProgresoReto = repositorioProgresoReto; 
        this.repositorioUsuario = repositorioUsuario; 
    }

        
    @Transactional
    public Evidencia registrarEvidencia(Long idUsuario,Long idProgreso,String rutaFotografia) {
        
        Usuario usuario = repositorioUsuario.findById(idUsuario).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        ProgresoReto progresoReto =
            repositorioProgresoReto.findById(idProgreso).orElseThrow(() -> new IllegalArgumentException("Progreso no encontrado"));

        if (!usuario.getId().equals(progresoReto.getUsuario().getId())) {
            throw new IllegalArgumentException( "No puedes enviar evidencia para otra persona" );
        }

        if (progresoReto.estaCompletado()) {
            throw new IllegalStateException("La misión ya está completada" );
        }

        if (rutaFotografia == null ||rutaFotografia.isBlank()) {
            throw new IllegalArgumentException("Debes adjuntar una fotografía" );
        }

        List<Evidencia> evidencias =repositorioEvidencia.findByProgresoRetoId(idProgreso);

        for (Evidencia evidencia : evidencias) {
            if (evidencia.getEstado() == EstadoEvidencia.PENDIENTE) {
                throw new IllegalStateException("Ya tienes una evidencia pendiente de revisión" );
            }
        }

        Evidencia evidencia = new Evidencia(
            usuario, progresoReto, rutaFotografia
        );

        return repositorioEvidencia.save(evidencia);
    }

    
}
