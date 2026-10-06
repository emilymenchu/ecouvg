package gt.edu.uvg.ecouvg.modelo;

import jakarta.persistence.Entity; 
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
@Entity 
@Table(name = "progreso_reto")

public class ProgresoReto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne  
    private Reto reto; 
    private int progreso; 
    private boolean completado; 
    @ManyToOne 
    private Usuario usuario; 

    public ProgresoReto(){

    }

    public ProgresoReto( Usuario usuario, Reto reto){
        this.usuario = usuario; 
        this.reto = reto; 
    }
    public Long getId() {
        return id; 
    }
    
    public Usuario getUsuario(){
        return usuario; 
    }

    public Reto getReto(){
        return reto; 
    }

    public int getProgreso(){
        return progreso; 
    }
    
    public void actualizarProgreso(int progreso){
        if (progreso <0 || progreso > 100){
            throw new IllegalArgumentException("El progreso tiene que estar entre 0 y 100. "); 
        }
        this.progreso = progreso; 
        
    }


    public void completar(){
        this.completado = true;

    }

    public boolean estaCompletado(){
        return completado; 

    }
}
