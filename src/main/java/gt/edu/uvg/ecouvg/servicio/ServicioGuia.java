package gt.edu.uvg.ecouvg.servicio;

import gt.edu.uvg.ecouvg.modelo.CategoriaGuia;
import gt.edu.uvg.ecouvg.modelo.GuiaContenedor;
import gt.edu.uvg.ecouvg.repositorio.RepositorioGuia;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ServicioGuia {
    private final RepositorioGuia repositorio;

    public ServicioGuia(RepositorioGuia repositorio) {
        this.repositorio = repositorio;
    }

    public List<GuiaContenedor> buscar(String q) {
        // Catálogo pequeño: carga categorías y ejemplos en una consulta.
        // Conserva búsqueda literal sin tildes y por todas las palabras.
        return repositorio.findAllByOrderByOrdenAscSlugAsc().stream()
            .map(this::convertir)
            .filter(c -> TextoBusqueda.coincide(c.nombre() + " " + c.descripcion()
                + " " + String.join(" ", c.ejemplos()), q))
            .toList();
    }

    public Optional<GuiaContenedor> obtener(String slug) {
        return repositorio.findBySlug(slug).map(this::convertir);
    }

    private GuiaContenedor convertir(CategoriaGuia c) {
        return new GuiaContenedor(c.getSlug(), c.getNombre(), c.getColor(),
            c.getSimbolo(), c.getDescripcion(), c.getEjemplos(),
            c.getPreparacion(), c.getFuenteUrl());
    }
}
