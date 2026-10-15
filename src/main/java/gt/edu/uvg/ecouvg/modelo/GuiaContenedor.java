package gt.edu.uvg.ecouvg.modelo;

import java.util.List;

/** Categoría educativa: no representa un contenedor físico ni una norma de colores. */
public record GuiaContenedor(String slug, String nombre, String color, String simbolo,
        String descripcion, List<String> ejemplos, String preparacion, String fuenteUrl) {
    public GuiaContenedor { ejemplos = List.copyOf(ejemplos); }
    public String getSlug() { return slug; }
    public String getNombre() { return nombre; }
    public String getColor() { return color; }
    public String getSimbolo() { return simbolo; }
    public String getDescripcion() { return descripcion; }
    public List<String> getEjemplos() { return ejemplos; }
    public String getPreparacion() { return preparacion; }
    public String getFuenteUrl() { return fuenteUrl; }
}
