package gt.edu.uvg.ecouvg.modelo;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint; 

@Entity 
@Table(name = "usuarios", uniqueConstraints = {@UniqueConstraint(name = "uk_usuario_carnet", columnNames = "carnet")})
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @Column(nullable = false, unique = true, length = 20)
    private String carnet;

    @Column(nullable = false, length = 100)
    private String contrasena;

    @Column(nullable = false, unique = true)
    private String correo;

    private int puntos;
    private int puntosTotales;
    private int diasRacha;
    private LocalDate lastActividadValida; 
    private int nivel = 1;
    private int horasBeca;


    public Usuario() {
    }

    public Usuario(String nombre, String carnet, String correo, String contrasena) {
        this.nombre = nombre;
        this.carnet = carnet;
        this.correo = correo;
        this.contrasena = contrasena;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCarnet() {
        return carnet;
    }

    public String getContrasena() {
        return contrasena;
    }

    public int getPuntos() {
        return puntos;
    }

    public int getPuntosTotales() {
        return puntosTotales;
    }

    public int getDiasRacha() {
        return diasRacha;
    }

    public int getNivel() {
        return nivel;
    }

    public int getHorasBeca() {
        return horasBeca;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public void sumarPuntos (int cantidad){
        if (cantidad<= 0) {
            throw new IllegalArgumentException("La cantidad tiene que ser positiva"); 
        }
        this.puntos += cantidad;
        this.puntosTotales += cantidad; 
        CalculadorNivel calculador = new CalculadorNivel(); 
        this.nivel = calculador.calculadorNivel(this.puntosTotales); 
    }
    @Transient 
    public String getNombreNivel(){
        CalculadorNivel calculador = new CalculadorNivel(); 
        return calculador.nombreNivel(nivel); 
    }

    public void registroActividadValida(LocalDate fechaActividad){
        if (fechaActividad == null){
            throw new IllegalArgumentException("La actividad debe contar con fecha -_-"); 

        }
        if (lastActividadValida == null){
            diasRacha = 1; 
        }else if(fechaActividad.equals(lastActividadValida)){ 
            return; 

        }else if(fechaActividad.equals(lastActividadValida.plusDays(1))){
            diasRacha++;

        }else if (fechaActividad.isAfter(lastActividadValida)){
            diasRacha = 1; 
        }else {
            return; 
        }
        lastActividadValida = fechaActividad; 
    }

    public int obtenerRacha(LocalDate hoy){
        if(hoy == null){
            throw new IllegalArgumentException("La fecha actual es obligatoria ://// ");

        }
        if (lastActividadValida ==null){
            return 0; 
        }
        if (lastActividadValida.equals(hoy)   ||  lastActividadValida.equals(hoy.minusDays(1))){
            return diasRacha;
        }
        return 0; 
        
    }
}
