package gt.edu.uvg.ecouvg.modelo;

public class CalculadorAccionUnica extends CalculadorProgreso {

    @Override
    public int calcularProgreso(int actual, int objetivo){
        if(objetivo >= 1){
            return 100; 
        
        }
        return 0; 
        
    }
}
