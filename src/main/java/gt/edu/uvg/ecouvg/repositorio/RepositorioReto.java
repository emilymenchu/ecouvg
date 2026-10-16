package gt.edu.uvg.ecouvg.repositorio;

import gt.edu.uvg.ecouvg.modelo.Reto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate; 
import java.util.List;



public interface RepositorioReto extends JpaRepository<Reto, Long> {
    List<Reto> findByFechaInicioLessThanEqualAndFechaFinGreaterThanEqual( LocalDate fechaInicio, LocalDate fechaFin);
}
    
