package gt.edu.uvg.ecouvg.modelo;

public class CalculadorDuracion extends CalculadorProgreso{

    @Override
    public int calcularProgreso(int actual, int objetivo){
        if(objetivo <= 0){
            throw new IllegalArgumentException ("EL objetivo tiene que ser mayor a 0"); 
        }

        int progreso = (int)(((double) actual / objetivo ) * 100);
        return Math.max(0, Math.min(100, progreso));
    }
    
}
