package gt.edu.uvg.ecouvg.modelo;

public class CalculadorCantidad extends CalculadorProgreso {

    @Override
    public int calcularProgreso(int actual, int objetivo){
       if (objetivo <= 0){
        throw new IllegalArgumentException("El objetivo debe ser mayor a 0");
       }
       int porcentajeAvance = (int)( ((double) actual/ objetivo) * 100);
       return Math.max(0, Math.min(100, porcentajeAvance));
    }
    
}
