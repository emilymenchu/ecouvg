package gt.edu.uvg.ecouvg.modelo;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class RecuperacionContrasena {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private Usuario usuario;

    private String codigo;

    private LocalDateTime fechaExpiracion;

    private boolean usado;

    public RecuperacionContrasena() {
    }

    public RecuperacionContrasena(
            Usuario usuario,
            String codigo,
            LocalDateTime fechaExpiracion) {

        this.usuario = usuario;
        this.codigo = codigo;
        this.fechaExpiracion = fechaExpiracion;
        this.usado = false;
    }

    public boolean esValido(String codigoIngresado) {
        return !usado
                && codigo.equals(codigoIngresado)
                && LocalDateTime.now().isBefore(fechaExpiracion);
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getCodigo() {
        return codigo;
    }

    public LocalDateTime getFechaExpiracion() {
        return fechaExpiracion;
    }

    public boolean isUsado() {
        return usado;
    }

    public void setUsado(boolean usado) {
        this.usado = usado;
    }
}