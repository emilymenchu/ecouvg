package gt.edu.uvg.ecouvg.servicio;

import gt.edu.uvg.ecouvg.modelo.Reto;
import gt.edu.uvg.ecouvg.modelo.ProgresoReto;
import gt.edu.uvg.ecouvg.modelo.Usuario;
import gt.edu.uvg.ecouvg.modelo.CalculadorProgreso; 
import gt.edu.uvg.ecouvg.modelo.CalculadorCantidad; 
import gt.edu.uvg.ecouvg.modelo.CalculadorAccionUnica; 
import gt.edu.uvg.ecouvg.modelo.CalculadorDuracion; 
import gt.edu.uvg.ecouvg.modelo.TipoProgreso;
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


    private CalculadorProgreso seleccionarCalculador (Reto reto) {
        TipoProgreso tipo = reto.getTipoProgreso(); 
        if (tipo==null){
            throw new IllegalArgumentException("El reto no cuenta con un tipo definido de progreso"); 
        }
        switch(tipo){
            case CANTIDAD:
                return new CalculadorCantidad();

            case ACCION_UNICA:
                return new CalculadorAccionUnica();

            case DURACION: 
                return new CalculadorDuracion();
                    
            default: 
            throw new IllegalArgumentException(tipo + "no es válido");     

        } 
    }

    public int calcularProgreso (Reto reto, int actual) {
        CalculadorProgreso calculador = seleccionarCalculador(reto); 
        int objetivo = reto.getObjetivo();
        int porcentaje = calculador.calcularProgreso(actual, objetivo);
        return porcentaje; 
    }

    public List<Reto> obtenerRetosSemanales( LocalDate fecha){
        return repositorioReto.findByFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(fecha, fecha);
    }

    public ProgresoReto obtenerProgreso(Long idUsuario, Long idReto){
        return repositorioProgresoReto.findByUsuarioIdAndRetoId(idUsuario, idReto).orElse(null);
    }

    public ProgresoReto actualizarProgreso(Long idUsuario, Long idReto, int actual){
        if (actual <0) {
            throw new IllegalArgumentException("El progreso debe ser entre 0 y 100");  

        }

        Optional<Usuario> usuarioBuscar = repositorioUsuario.findById(idUsuario);
        Usuario usuario = usuarioBuscar.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        Optional<Reto> retoBuscar = repositorioReto.findById(idReto);
        Reto reto = retoBuscar.orElseThrow(() -> new IllegalArgumentException("Reto no encontrado"));
        int porcentaje = calcularProgreso(reto, actual); 
        
        Optional<ProgresoReto> progresoBuscar = repositorioProgresoReto.findByUsuarioIdAndRetoId(idUsuario, idReto); 
        ProgresoReto progresoReto; 

        if (progresoBuscar.isPresent()) {
            progresoReto = progresoBuscar.get(); 
        } else {
            progresoReto = new ProgresoReto(usuario, reto); 
        }
        
        int progresoAnterior = progresoReto.getProgreso(); 

        if(porcentaje < progresoAnterior){
            porcentaje = progresoAnterior; 
        }

        progresoReto.actualizarProgreso(porcentaje); 
        if (porcentaje == 100 && !progresoReto.estaCompletado()) {
            progresoReto.pendienteValidacion();
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
