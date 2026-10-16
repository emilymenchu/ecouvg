package gt.edu.uvg.ecouvg.repositorio;
import gt.edu.uvg.ecouvg.modelo.Evidencia;
import jakarta.persistence.LockModeType;
import gt.edu.uvg.ecouvg.modelo.EstadoEvidencia; 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List; 


public interface  RepositorioEvidencia extends JpaRepository<Evidencia, Long>{

    List<Evidencia> findByEstado(EstadoEvidencia estado); 
    List<Evidencia> findByProgresoRetoId(Long idProgresoReto);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Evidencia e WHERE e.id = :id")
    Optional<Evidencia> buscarParaRevision(@Param("id") Long id);
}


