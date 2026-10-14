package gt.edu.uvg.ecouvg.modelo;
import gt.edu.uvg.ecouvg.repositorio.RepositorioEvidencia;
import java.util.List;
public class ValidacionEvidencia extends ValidacionReto {
    private final RepositorioEvidencia repositorioEvidencia;

    public ValidacionEvidencia(RepositorioEvidencia repositorioEvidencia){
        this.repositorioEvidencia = repositorioEvidencia; 
    }
    @Override 
    public boolean validar(ProgresoReto progresoReto){
        if (progresoReto == null) {
            throw new IllegalArgumentException ("Debe existir un progreso");

        }
        Long idProgreso = progresoReto.getId(); 
        if (idProgreso == null){
            throw new IllegalArgumentException("El progreso debe registrarse"); 

        }
        List<Evidencia> evidencias = repositorioEvidencia.findByProgresoRetoId(idProgreso); 
        for (Evidencia evidencia :evidencias){
            if (evidencia.getEstado() == EstadoEvidencia.APROBADA){
                return true; 
            }
        }
        return false; 
    }
}
