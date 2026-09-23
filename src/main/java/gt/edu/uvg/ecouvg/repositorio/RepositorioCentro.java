package gt.edu.uvg.ecouvg.repositorio;

import gt.edu.uvg.ecouvg.modelo.CentroReciclaje;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RepositorioCentro extends JpaRepository<CentroReciclaje, Long> {
    @EntityGraph(attributePaths = "residuosAceptados")
    List<CentroReciclaje> findAllByOrderByNombreAsc();

    @Override
    @EntityGraph(attributePaths = "residuosAceptados")
    Optional<CentroReciclaje> findById(Long id);
}
