package gt.edu.uvg.ecouvg.dto;

import java.util.List;

/** Solo información educativa de la guía, sin entidades JPA en la respuesta. */
public record MaterialArcade(String slug, String nombre, List<String> ejemplos, String preparacion) {
    public MaterialArcade { ejemplos = List.copyOf(ejemplos); }
}
