package gt.edu.uvg.ecouvg.repositorio;
import gt.edu.uvg.ecouvg.modelo.ProgresoReto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional; 



public interface RepositorioProgresoReto extends JpaRepository<ProgresoReto, Long>{
    
    Optional<ProgresoReto> findByUsuarioIdAndRetoId(Long idUsuario, Long idReto);

}
    
