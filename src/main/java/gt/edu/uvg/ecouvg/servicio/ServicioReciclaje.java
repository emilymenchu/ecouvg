package gt.edu.uvg.ecouvg.servicio;

import gt.edu.uvg.ecouvg.modelo.Residuo;
import gt.edu.uvg.ecouvg.repositorio.RepositorioResiduo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ServicioReciclaje {
    private final RepositorioResiduo repositorio;

    public ServicioReciclaje(RepositorioResiduo repositorio) {
        this.repositorio = repositorio;
    }

    public List<Residuo> obtenerResiduos() {
        return repositorio.findAll();
    }

    public Optional<Residuo> buscarResiduo(String nombre) {
        return repositorio.findByNombre(nombre);
    }

    public List<Residuo> buscarPorTipo(String tipo) {
        return repositorio.findByTipo(tipo);
    }
}
