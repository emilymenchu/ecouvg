package gt.edu.uvg.ecouvg.servicio;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gt.edu.uvg.ecouvg.modelo.Evidencia;
import gt.edu.uvg.ecouvg.modelo.ProgresoReto;
import gt.edu.uvg.ecouvg.modelo.RevisionEvidencia;
import gt.edu.uvg.ecouvg.modelo.Usuario;
import gt.edu.uvg.ecouvg.repositorio.RepositorioEvidencia;
import gt.edu.uvg.ecouvg.repositorio.RepositorioRevisionEvidencia;
import gt.edu.uvg.ecouvg.repositorio.RepositorioUsuario;

@Service 
public class ServicioRevisionEvidencia {

    private final RepositorioEvidencia repositorioEvidencia; 
    private final RepositorioRevisionEvidencia repositorioRevisionEvidencia; 
    private final RepositorioUsuario repositorioUsuario; 
    private final ServicioReto servicioReto;
    

    public ServicioRevisionEvidencia(RepositorioEvidencia repositorioEvidencia, RepositorioRevisionEvidencia repositorioRevisionEvidencia, RepositorioUsuario repositorioUsuario, ServicioReto servicioReto){

    this.repositorioEvidencia = repositorioEvidencia; 
    this.repositorioRevisionEvidencia = repositorioRevisionEvidencia; 
    this.repositorioUsuario = repositorioUsuario; 
    this.servicioReto = servicioReto; 
    }

    @Transactional
    public RevisionEvidencia registrarRevision(Long idEvidencia, Long idRevisor, boolean aprobada){

        Evidencia evidencia = repositorioEvidencia.buscarParaRevision(idEvidencia).orElseThrow(()-> new IllegalArgumentException("Evidencia no encontrada"));
        Usuario revisor = repositorioUsuario.findById(idRevisor).orElseThrow(() -> new IllegalArgumentException( "Usuario no encontrado"));
        if (!evidencia.estaPendiente()){
        throw new IllegalStateException("Evidencia ya revisada y cerrada ");
        }
        Usuario autor = evidencia.getUsuario(); 
        if (autor.getId().equals(revisor.getId())){
            throw new IllegalArgumentException("NO puedes revisar evidencia propia"); 
        }
        ProgresoReto progresoReto = evidencia.getProgresoReto();
        if (progresoReto == null) {
            throw new IllegalStateException("La evidencia no muestra un progrso" );
        }

        boolean yaVoto = repositorioRevisionEvidencia.existsByEvidenciaIdAndRevisorId(idEvidencia, idRevisor); 
        if (yaVoto){
            throw new IllegalStateException("Ya revisaste esta evidencia - _ -");  
        }
        long totalRevisiones = repositorioRevisionEvidencia.countByEvidenciaIdAndAprobada(idEvidencia, true)+ repositorioRevisionEvidencia.countByEvidenciaIdAndAprobada(idEvidencia, false);
        if (totalRevisiones >= 3){
            throw new IllegalStateException ("La evidencia solo acepta 3 revisiones") ;
        }


        RevisionEvidencia revision = new RevisionEvidencia(evidencia, revisor, aprobada); 
        RevisionEvidencia revisionGuardada = repositorioRevisionEvidencia.save(revision); 
        
        long aprobaciones = repositorioRevisionEvidencia.countByEvidenciaIdAndAprobada(idEvidencia, true); 
        long rechazos = repositorioRevisionEvidencia.countByEvidenciaIdAndAprobada(idEvidencia, false); 
        
        if (aprobaciones >= 2){
            evidencia.aprobar();
            repositorioEvidencia.saveAndFlush(evidencia);
            
            servicioReto.registrarEvidenciaAprobada(progresoReto);
            
        } else if (rechazos >= 2){
            evidencia.rechazar(); 
            repositorioEvidencia.save(evidencia); 
            
        }

        
        return revisionGuardada; 
   
    }


    //
    

    
    
    
}
