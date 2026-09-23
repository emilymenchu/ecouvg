package gt.edu.uvg.ecouvg.modelo;

import jakarta.persistence.*;

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

    private int puntos;
    private int puntosTotales;
    private int diasRacha;
    private int nivel;
    private int horasBeca;

    public Usuario() {
    }

    public Usuario(String nombre, String carnet, String contrasena) {
        this.nombre = nombre;
        this.carnet = carnet;
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
}
