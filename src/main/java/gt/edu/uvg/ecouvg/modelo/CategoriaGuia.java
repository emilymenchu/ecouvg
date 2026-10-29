package gt.edu.uvg.ecouvg.modelo;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/** Categoría educativa de la guía; independiente de los contenedores físicos. */
@Entity
@Table(name = "guia_contenedores")
public class CategoriaGuia {
    @Id
    @Column(nullable = false, length = 80)
    private String slug;
    @Column(nullable = false, length = 120)
    private String nombre;
    @Column(nullable = false, length = 30)
    private String color;
    @Column(nullable = false, length = 16)
    private String simbolo;
    @Column(nullable = false, columnDefinition = "text")
    private String descripcion;
    @Column(nullable = false, columnDefinition = "text")
    private String preparacion;
    @Column(name = "fuente_url", nullable = false, length = 1000)
    private String fuenteUrl;
    @Column(nullable = false)
    private int orden;
    @ElementCollection
    @CollectionTable(name = "guia_contenedor_ejemplos",
        joinColumns = @JoinColumn(name = "contenedor_slug"))
    @OrderColumn(name = "posicion")
    @Column(name = "ejemplo", nullable = false, length = 250)
    private List<String> ejemplos = new ArrayList<>();

    protected CategoriaGuia() { }

    public CategoriaGuia(String slug, String nombre, String color, String simbolo,
            String descripcion, List<String> ejemplos, String preparacion,
            String fuenteUrl, int orden) {
        this.slug = slug;
        this.nombre = nombre;
        this.color = color;
        this.simbolo = simbolo;
        this.descripcion = descripcion;
        this.ejemplos = new ArrayList<>(ejemplos);
        this.preparacion = preparacion;
        this.fuenteUrl = fuenteUrl;
        this.orden = orden;
    }

    public String getSlug() { return slug; }
    public String getNombre() { return nombre; }
    public String getColor() { return color; }
    public String getSimbolo() { return simbolo; }
    public String getDescripcion() { return descripcion; }
    public String getPreparacion() { return preparacion; }
    public String getFuenteUrl() { return fuenteUrl; }
    public int getOrden() { return orden; }
    public List<String> getEjemplos() { return List.copyOf(ejemplos); }
}
