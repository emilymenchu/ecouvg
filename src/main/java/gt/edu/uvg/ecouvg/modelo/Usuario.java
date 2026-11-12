package gt.edu.uvg.ecouvg.modelo;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;

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
public class Usuario implements Comparable<Usuario> {

    private static final int[] UMBRALES_NIVEL = {0, 100, 250, 500, 1000, 10000};

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
    private int semanasRacha;
    @Column(name = "last_actividad_valida")
    private LocalDate lastActividadValida; 
    private int nivel = 1;
    private int horasBeca;
    private String fotoPerfil;

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(String fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }


    public Usuario() {
    }

    public Usuario(String nombre, String carnet, String correo, String contrasena) {
        this.nombre = nombre;
        this.carnet = carnet;
        this.correo = correo;
        this.contrasena = contrasena;
        this.nivel = 1;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
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

    public int getSemanasRacha() {
        return semanasRacha;
    }

    public int getNivel() {
        return Math.clamp(nivel, 1, UMBRALES_NIVEL.length);
    }

    public boolean isNivelMaximo() {
        return getNivel() == UMBRALES_NIVEL.length;
    }

    public int getMetaNivel() {
        return UMBRALES_NIVEL[isNivelMaximo() ? getNivel() - 1 : getNivel()];
    }

    public int getPorcentajeNivel() {
        if (isNivelMaximo()) {
            return 100;
        }
        int inicio = UMBRALES_NIVEL[getNivel() - 1];
        return (int) Math.clamp((puntosTotales - (long) inicio) * 100 / (getMetaNivel() - inicio), 0, 100);
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

    @Transient
    public int getRachaVigente() {
        LocalDate hoy = LocalDate.now(ZoneId.of("America/Guatemala"));
        return obtenerRacha(hoy);
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
        return calculador.nombreNivel(getNivel()); 
    }

    public void registroActividadValida(LocalDate fechaActividad){
        if (fechaActividad == null){
            throw new IllegalArgumentException("La actividad debe contar con fecha -_-"); 

        }
        LocalDate semanaActual = fechaActividad.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY) );

        if (lastActividadValida == null){
            semanasRacha = 1; 
        }else {
            LocalDate semanaAnterior = lastActividadValida.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            if (semanaActual.equals(semanaAnterior)){
                    return; 
                } else if (semanaActual.equals(semanaAnterior.plusWeeks(1))) {
                    semanasRacha++;

                } else if (semanaActual.isAfter(semanaAnterior)) {
                    semanasRacha = 1;

                } else {
                    return;
                }
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
        LocalDate semanaActual = hoy.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate ultimaSemana = lastActividadValida.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        if (ultimaSemana.equals(semanaActual)|| ultimaSemana.equals(semanaActual.minusWeeks(1))) {
        return semanasRacha;
        }
        return 0; 
    }

    @Override
    public int compareTo(Usuario otro) {
        return Integer.compare(otro.puntosTotales, this.puntosTotales);
    }

    public void calcularNivel() {
        nivel = 1;
        for (int i = 1; i < UMBRALES_NIVEL.length; i++) {
            if (puntosTotales >= UMBRALES_NIVEL[i]) {
                nivel = i + 1;
            }
        }
    }



}
