package gt.edu.uvg.ecouvg.servicio;

import gt.edu.uvg.ecouvg.modelo.Reto;
import gt.edu.uvg.ecouvg.repositorio.RepositorioReto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.time.LocalDate; 

@Service
public class ServicioReto { 
    private RepositorioReto repositorioReto; 
    
    public ServicioReto(RepositorioReto repositorioReto){
        this.repositorioReto = repositorioReto; 
    }

    public List<Reto> obtenerRetosSemanales( LocalDate fecha){
        return repositorioReto.findByFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(fecha, fecha);
    }
}
