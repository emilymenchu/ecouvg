package gt.edu.uvg.ecouvg.repositorio;
import gt.edu.uvg.ecouvg.modelo.ProgresoReto;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional; 



public interface RepositorioProgresoReto extends JpaRepository<ProgresoReto, Long>{
    
    Optional<ProgresoReto> findByUsuarioIdAndRetoId(Long idUsuario, Long idReto);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ProgresoReto p WHERE p.id = :id")
    Optional<ProgresoReto> buscarParaCompletar(@Param("id") Long id);

}
    
