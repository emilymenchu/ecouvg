package gt.edu.uvg.ecouvg.servicio;

import gt.edu.uvg.ecouvg.dto.MaterialArcade;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ServicioArcade {
    private final ServicioGuia guia;
    // Identificadores del contrato del juego; los textos y ejemplos vienen de BD.
    private static final List<String> MATERIALES = List.of("plasticos", "metales", "papel", "carton");

    public ServicioArcade(ServicioGuia guia) { this.guia = guia; }

    public List<MaterialArcade> obtenerMateriales() {
        var categorias = guia.buscar("");
        return MATERIALES.stream().map(slug -> {
            var categoria = categorias.stream().filter(c -> c.slug().equals(slug))
                .findFirst().orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE, "Faltan categorías de la guía para iniciar Eco Arcade"));
            if (categoria.ejemplos().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Faltan ejemplos en una categoría de la guía");
            }
            return new MaterialArcade(categoria.slug(), categoria.nombre(),
                categoria.ejemplos(), categoria.preparacion());
        }).toList();
    }
}
