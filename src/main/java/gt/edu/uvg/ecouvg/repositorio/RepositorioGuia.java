package gt.edu.uvg.ecouvg.repositorio;

import gt.edu.uvg.ecouvg.modelo.CategoriaGuia;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RepositorioGuia extends JpaRepository<CategoriaGuia, String> {
    @EntityGraph(attributePaths = "ejemplos")
    List<CategoriaGuia> findAllByOrderByOrdenAscSlugAsc();

    @EntityGraph(attributePaths = "ejemplos")
    Optional<CategoriaGuia> findBySlug(String slug);
}
