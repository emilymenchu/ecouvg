package gt.edu.uvg.ecouvg.repositorio;
import gt.edu.uvg.ecouvg.modelo.RevisionEvidencia; 
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List; 



public interface  RepositorioRevisionEvidencia extends JpaRepository<RevisionEvidencia, Long>{

    List<RevisionEvidencia> findByEvidenciaId(Long idEvidencia);
    boolean existsByEvidenciaIdAndRevisorId(Long idEvidencia, Long idRevisor); 
    long countByEvidenciaIdAndAprobada(Long idEvidencia, boolean aprobada);
}

    
