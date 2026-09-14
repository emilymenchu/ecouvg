package gt.edu.uvg.ecouvg.modelo;

import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
@Table(name = "reto")

public class Reto {
    //Atributos
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 
    private String titulo; 
    private String descripcion; 
    private int puntosRecompensa; 
    private String dificultad; 
    private LocalDate fechaInicio; 
    private LocalDate fechaFin; 
    
    //Métodos
    public Reto(){

    }

    public Reto(String titulo, String descripcion, int puntosRecompensa, String dificultad, LocalDate fechaInicio, LocalDate fechaFin){
        this.titulo = titulo;
        this.descripcion = descripcion; 
        this.puntosRecompensa = puntosRecompensa; 
        this.dificultad = dificultad; 
        this.fechaInicio = fechaInicio; 
        this.fechaFin = fechaFin; 

    }

    public String getTitulo(){
        return titulo; 
    }

    public String getDescripcion(){
        return descripcion; 
    }

    public int getPuntosRecompensa(){
        return puntosRecompensa; 
    }

    public String getDificultad() {
        return dificultad; 
    }


    public boolean estaDisponible(LocalDate fecha) {
         if (fecha == null || fechaInicio == null || fechaFin == null){
            return false; 
         }
         
         if (fecha.isBefore(fechaInicio)){
            return false; 
         }

         if (fecha.isAfter(fechaFin)){
            return false; 
         }
         return true; 
    }
}
