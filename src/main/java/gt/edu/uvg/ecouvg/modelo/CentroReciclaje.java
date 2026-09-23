package gt.edu.uvg.ecouvg.modelo;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "centros_reciclaje", uniqueConstraints =
    @UniqueConstraint(name = "uk_centro_nombre_ubicacion", columnNames = {"nombre", "ubicacion"}))
public class CentroReciclaje {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private String nombre;
    @Column(nullable = false, length = 500) private String ubicacion;
    // Null representa una coordenada pendiente; nunca se sustituye por 0,0.
    private Double latitud;
    private Double longitud;
    @Column(length = 500) private String horario;
    @Column(length = 1500) private String descripcion;
    @Column(name = "fuente_url", length = 1000) private String fuenteUrl;
    @Column(name = "fecha_consulta") private LocalDate fechaConsulta;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "centro_residuo",
        joinColumns = @JoinColumn(name = "centro_id"),
        inverseJoinColumns = @JoinColumn(name = "residuo_id"),
        uniqueConstraints = @UniqueConstraint(columnNames = {"centro_id", "residuo_id"}))
    @OrderBy("nombre ASC")
    private List<Residuo> residuosAceptados = new ArrayList<>();

    public CentroReciclaje() {}

    public CentroReciclaje(String nombre, String ubicacion, Double latitud, Double longitud,
            String horario, String descripcion, List<Residuo> residuosAceptados) {
        this.nombre = nombre;
        this.ubicacion = ubicacion;
        this.latitud = latitud;
        this.longitud = longitud;
        this.horario = horario;
        this.descripcion = descripcion;
        this.residuosAceptados = new ArrayList<>(residuosAceptados);
    }
    public boolean aceptaResiduo(Residuo residuo) {
        if (residuo == null) return false;
        return residuosAceptados.stream().anyMatch(aceptado -> aceptado == residuo ||
            (residuo.getId() != null && residuo.getId().equals(aceptado.getId())));
    }
    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getUbicacion() { return ubicacion; }
    public Double getLatitud() { return latitud; }
    public Double getLongitud() { return longitud; }
    public String getHorario() { return horario; }
    public String getDescripcion() { return descripcion; }
    public List<Residuo> getResiduosAceptados() { return residuosAceptados; }
    public String getFuenteUrl() { return fuenteUrl; }
    public LocalDate getFechaConsulta() { return fechaConsulta; }
}