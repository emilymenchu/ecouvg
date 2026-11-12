package gt.edu.uvg.ecouvg.repositorio;
import java.util.List;
import java.util.Optional;
 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import gt.edu.uvg.ecouvg.modelo.EstadoEvidencia; 
import gt.edu.uvg.ecouvg.modelo.Evidencia;


public interface  RepositorioEvidencia extends JpaRepository<Evidencia, Long>{

    List<Evidencia> findByEstado(EstadoEvidencia estado); 
    List<Evidencia> findByProgresoRetoId(Long idProgresoReto);
    @Query("SELECT e FROM Evidencia e WHERE e.id = :id")
    Optional<Evidencia> buscarParaRevision(@Param("id") Long id);
}


