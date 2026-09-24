package gt.edu.uvg.ecouvg.servicio;

import gt.edu.uvg.ecouvg.modelo.Reto;
import gt.edu.uvg.ecouvg.modelo.ProgresoReto;
import gt.edu.uvg.ecouvg.modelo.Usuario;
import gt.edu.uvg.ecouvg.repositorio.RepositorioReto;
import gt.edu.uvg.ecouvg.repositorio.RepositorioProgresoReto;
import gt.edu.uvg.ecouvg.repositorio.RepositorioUsuario;
import org.springframework.stereotype.Service;

import java.util.List;
import java.time.LocalDate; 
import java.util.Optional; 

@Service
public class ServicioReto { 
    private RepositorioReto repositorioReto; 
    private final RepositorioProgresoReto repositorioProgresoReto; 
    private final RepositorioUsuario repositorioUsuario; 
    
    public ServicioReto(RepositorioReto repositorioReto, RepositorioProgresoReto repositorioProgresoReto, RepositorioUsuario repositorioUsuario){
        this.repositorioReto = repositorioReto; 
        this.repositorioProgresoReto = repositorioProgresoReto; 
        this.repositorioUsuario = repositorioUsuario;
    }

    public List<Reto> obtenerRetosSemanales( LocalDate fecha){
        return repositorioReto.findByFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(fecha, fecha);
    }

    public ProgresoReto obtenerProgreso(Long idUsuario, Long idReto){
        return repositorioProgresoReto.findByUsuarioIdAndRetoId(idUsuario, idReto).orElse(null);
    }

    public ProgresoReto actualizarProgreso(Long idUsuario, Long idReto, int progreso){
        if (progreso <0 || progreso >100){
            throw new IllegalArgumentException("El progreso debe ser entre 0 y 100");  

        }

        Optional<Usuario> usuarioBuscar = repositorioUsuario.findById(idUsuario);
        Usuario usuario = usuarioBuscar.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        Optional<Reto> retoBuscar = repositorioReto.findById(idReto);
        Reto reto = retoBuscar.orElseThrow(() -> new IllegalArgumentException("Reto no encontrado"));
        
        Optional<ProgresoReto> progresoBuscar = repositorioProgresoReto.findByUsuarioIdAndRetoId(idUsuario, idReto); 
        ProgresoReto progresoReto; 

        if (progresoBuscar.isPresent()) {
            progresoReto = progresoBuscar.get(); 
        } else {
            progresoReto = new ProgresoReto(usuario, reto); 
        }
        boolean yaCompletado = progresoReto.estaCompletado();
        progresoReto.actualizarProgreso(progreso); 
        
        if (progreso >= 100 && !yaCompletado){
            completarReto(progresoReto);
            return progresoReto; 
        }
        return repositorioProgresoReto.save(progresoReto);
    }


    public void completarReto(ProgresoReto progresoReto){
        if (progresoReto.estaCompletado()){
            return; 
        }
        progresoReto.completar();
        Usuario usuario = progresoReto.getUsuario(); 
        Reto reto = progresoReto.getReto(); 
        progresoReto.completar(); 

        usuario.sumarPuntos(reto.getPuntosRecompensa());
        repositorioUsuario.save(usuario);
        repositorioProgresoReto.save(progresoReto); 
    }
}
