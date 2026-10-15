package gt.edu.uvg.ecouvg.modelo;

public class ValidacionAutomatica extends ValidacionReto {

    @Override 
    public boolean validar(ProgresoReto progresoReto){
        if (progresoReto == null){
            throw new IllegalArgumentException("Debe existir un progreso");
        }
        return progresoReto.getProgreso() == 100; 
    }
    
}
