package gt.edu.uvg.ecouvg.servicio;

import gt.edu.uvg.ecouvg.modelo.CentroReciclaje;
import gt.edu.uvg.ecouvg.modelo.Residuo;
import gt.edu.uvg.ecouvg.repositorio.RepositorioCentro;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ServicioCentro {
    private final RepositorioCentro repositorio;
    public ServicioCentro(RepositorioCentro repositorio) { this.repositorio = repositorio; }

    public List<CentroReciclaje> obtenerCentros() {
        return repositorio.findAllByOrderByNombreAsc();
    }
    public Optional<CentroReciclaje> obtenerCentro(Long id) {
        return repositorio.findById(id);
    }
    // Directorio pequeño: una consulta con EntityGraph carga centros y materiales.
    // El filtrado literal evita diferencias entre dialectos y funciona sin extensiones SQL.
    public List<CentroReciclaje> buscarCentros(String consulta, String material) {
        String filtro = TextoBusqueda.normalizar(material);
        return obtenerCentros().stream().filter(c -> {
            String residuos = c.getResiduosAceptados().stream()
                .map(r -> r.getNombre() + " " + r.getTipo()).collect(Collectors.joining(" "));
            boolean acepta = filtro.isEmpty() || c.getResiduosAceptados().stream()
                .anyMatch(r -> TextoBusqueda.normalizar(r.getNombre()).equals(filtro));
            return acepta && TextoBusqueda.coincide(c.getNombre() + " " + c.getUbicacion()
                + " " + residuos, consulta);
        }).toList();
    }
    public List<String> obtenerMateriales() {
        return obtenerCentros().stream().flatMap(c -> c.getResiduosAceptados().stream())
            .map(Residuo::getNombre).distinct().sorted().toList();
    }
}
