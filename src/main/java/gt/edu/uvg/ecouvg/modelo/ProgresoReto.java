package gt.edu.uvg.ecouvg.modelo;

import jakarta.persistence.*;
@Entity 
@Table(name = "progreso_reto")

public class ProgresoReto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne  
    private Reto reto; 
    private int progreso; 
    private int cantidadValidada; 
    private boolean completado; 
    @ManyToOne 
    private Usuario usuario; 
    @Enumerated(EnumType.STRING)
    private EstadoProgreso estado; 

    public ProgresoReto(){

    }

    public ProgresoReto( Usuario usuario, Reto reto){
        this.usuario = usuario; 
        this.reto = reto; 
        this.estado = EstadoProgreso.EN_PROGRESO; 
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

    public int getCantidadValidada(){
        return cantidadValidada; 
    }

    public void registrarUnidadValidada() {

        if (estaCompletado()) {
            throw new IllegalStateException( "La misión ya está completada");
        }

        if (cantidadValidada >= reto.getObjetivo()) {
            throw new IllegalStateException("Ya se alcanzó el objetivo de la misión"  );
        }

        this.cantidadValidada++;
    }

    public EstadoProgreso getEstado(){
        return estado; 
    }
    
    public void actualizarProgreso(int progreso){
        if (progreso <0 || progreso > 100){
            throw new IllegalArgumentException("El progreso tiene que estar entre 0 y 100. "); 
        }
        this.progreso = progreso; 
        
    }
    
    public void pendienteValidacion() {
        if (completado){
            throw new IllegalStateException ("Misión completada");    
        }
        this.estado = EstadoProgreso.PENDIENTE_VALIDACION;
    }


    public void completar(){
        this.completado = true;
        this.estado = EstadoProgreso.COMPLETADO; 

    }


    public boolean estaCompletado(){
        return completado; 

    }
}
