package gt.edu.uvg.ecouvg.modelo;

public class CalculadorAccionUnica extends CalculadorProgreso {

    @Override
    public int calcularProgreso(int actual, int objetivo){
        if(objetivo <= 0){
            throw new IllegalArgumentException("El objetivo no puede ser negativo o 0");
        }
        
        if(actual >= 1){
            return 100; 
        
        }
        return 0; 
        
    }
}
