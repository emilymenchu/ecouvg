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
import gt.edu.uvg.ecouvg.modelo.ValidacionReto;
import gt.edu.uvg.ecouvg.modelo.ValidacionEvidencia;
import gt.edu.uvg.ecouvg.repositorio.RepositorioEvidencia;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.time.LocalDate; 
import java.util.Optional; 

@Service
public class ServicioReto { 
    private RepositorioReto repositorioReto; 
    private final RepositorioProgresoReto repositorioProgresoReto; 
    private final RepositorioUsuario repositorioUsuario; 
    private final RepositorioEvidencia repositorioEvidencia; 
    
    public ServicioReto(RepositorioReto repositorioReto, RepositorioProgresoReto repositorioProgresoReto, RepositorioUsuario repositorioUsuario, RepositorioEvidencia repositorioEvidencia){
        this.repositorioReto = repositorioReto; 
        this.repositorioProgresoReto = repositorioProgresoReto; 
        this.repositorioUsuario = repositorioUsuario;
        this.repositorioEvidencia = repositorioEvidencia; 
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
            throw new IllegalArgumentException("El progreso debe ser positivo");  

        }

        Optional<Usuario> usuarioBuscar = repositorioUsuario.findById(idUsuario);
        Usuario usuario = usuarioBuscar.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        Optional<Reto> retoBuscar = repositorioReto.findById(idReto);
        Reto reto = retoBuscar.orElseThrow(() -> new IllegalArgumentException("Reto no encontrado"));
        if (!reto.estaDisponible(LocalDate.now())){
            throw new IllegalStateException("No se puede registrar progreso si el reto no está disponible"); 
        }
        int porcentaje = calcularProgreso(reto, actual); 
        
        Optional<ProgresoReto> progresoBuscar = repositorioProgresoReto.findByUsuarioIdAndRetoId(idUsuario, idReto); 
        ProgresoReto progresoReto; 

        if (progresoBuscar.isPresent()) {
            progresoReto = progresoBuscar.get(); 
        } else {
            progresoReto = new ProgresoReto(usuario, reto); 
        }
        
        if (progresoReto.estaCompletado()) {
            throw new IllegalStateException("No se puede modificar una misión completada");
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


    private void completarReto(ProgresoReto progresoReto){
        if (progresoReto.estaCompletado()){
            return; 
        }
        
        Reto reto = progresoReto.getReto(); 
        

        progresoReto.completar();
        Usuario usuario = progresoReto.getUsuario(); 
        usuario.sumarPuntos(reto.getPuntosRecompensa());
        repositorioUsuario.save(usuario);
        repositorioProgresoReto.save(progresoReto); 
    }
    
    private boolean tieneEvidenciaAprobada(ProgresoReto progresoReto) {

        ValidacionReto validador =
            new ValidacionEvidencia(repositorioEvidencia);

        return validador.validar(progresoReto);
    }


        
    @Transactional
    public void completarRetoConEvidencia(ProgresoReto progresoReto) {

        if (progresoReto == null) {
            throw new IllegalArgumentException("El progreso no puede ser nulo");
        }

        Long idProgreso = progresoReto.getId();

        if (idProgreso == null) {
            throw new IllegalArgumentException("El progreso debe estar registrado");
        }

        ProgresoReto progresoBloqueado = repositorioProgresoReto.buscarParaCompletar(idProgreso).orElseThrow(() -> new IllegalArgumentException("Progreso no encontrado" ));

        if (progresoBloqueado.estaCompletado()) {
            return;
        }

        if (progresoBloqueado.getProgreso() != 100) {
            throw new IllegalStateException("La misión todavía no alcanza el 100%");
        }

        if (!tieneEvidenciaAprobada(progresoBloqueado)) {
            throw new IllegalStateException("La evidencia todavía no está aprobada" );
        }

        completarReto(progresoBloqueado);
    }
        
    private void actualizarProgresoValidado(ProgresoReto progresoReto) {

        Reto reto = progresoReto.getReto();
        int cantidad = progresoReto.getCantidadValidada();
        int porcentaje = calcularProgreso(reto, cantidad);
        progresoReto.actualizarProgreso(porcentaje);
    }
        
    @Transactional
    public void registrarEvidenciaAprobada(ProgresoReto progresoReto) {

        if (progresoReto == null || progresoReto.getId() == null) {
            throw new IllegalArgumentException("El progreso debe estar registrado");
        }

        ProgresoReto progresoBloqueado =repositorioProgresoReto.buscarParaCompletar(progresoReto.getId() ).orElseThrow(() -> new IllegalArgumentException("Progreso no encontrado"));

        if (progresoBloqueado.estaCompletado()) {
            throw new IllegalStateException("La misión ya está completada" );
        }

        Reto reto = progresoBloqueado.getReto();

        if (reto.getTipoProgreso() != TipoProgreso.CANTIDAD) {
            throw new IllegalStateException( "Este registro requiere una misión de cantidad" );
        }

        progresoBloqueado.registrarUnidadValidada();

        actualizarProgresoValidado(progresoBloqueado);

        if (progresoBloqueado.getProgreso() == 100) {
            completarReto(progresoBloqueado);
        }

        repositorioProgresoReto.save(progresoBloqueado);
    }



}
