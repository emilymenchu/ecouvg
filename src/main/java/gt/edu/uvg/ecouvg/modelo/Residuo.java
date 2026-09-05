package gt.edu.uvg.ecouvg.modelo;

import jakarta.persistence.*;

@Entity
@Table(name = "residuos")
public class Residuo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String tipo;
    private String descripcion;
    private String instrucciones;
    private boolean reciclable;

    public Residuo() {
    }

    public Residuo(String nombre, String tipo, String descripcion, String instrucciones, boolean reciclable) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.instrucciones = instrucciones;
        this.reciclable = reciclable;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getInstrucciones() {
        return instrucciones;
    }

    public boolean isReciclable() {
        return reciclable;
    }
}
