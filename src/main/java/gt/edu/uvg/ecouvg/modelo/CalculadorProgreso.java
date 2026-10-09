package gt.edu.uvg.ecouvg.modelo;

public abstract class CalculadorProgreso {

    public abstract int calcularProgreso( int actual, int objetivo); 


    protected int limiteProgreso(int progreso){
        return Math.max(0, Math.min(100, progreso));
    }
       
    
}
