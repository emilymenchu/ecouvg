package gt.edu.uvg.ecouvg.repositorio;

import gt.edu.uvg.ecouvg.modelo.Residuo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RepositorioResiduo extends JpaRepository<Residuo, Long> {
    List<Residuo> findByTipo(String tipo);
    Optional<Residuo> findByNombre(String nombre);
}
