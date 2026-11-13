package gt.edu.uvg.ecouvg.repositorio;
import gt.edu.uvg.ecouvg.modelo.MisionArcade;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
public interface RepositorioMisionArcade extends JpaRepository<MisionArcade,Long> {
    boolean existsByUsuarioIdAndSemana(Long usuarioId,LocalDate semana);
}
