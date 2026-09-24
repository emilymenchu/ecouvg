package gt.edu.uvg.ecouvg.servicio;

import org.springframework.stereotype.Service;

import gt.edu.uvg.ecouvg.modelo.Usuario;
import gt.edu.uvg.ecouvg.repositorio.RepositorioUsuario;

@Service
public class ServicioUsuario {

    private final RepositorioUsuario repositorioUsuario;

    public ServicioUsuario(RepositorioUsuario repositorioUsuario) {
        this.repositorioUsuario = repositorioUsuario;
    }

    public Usuario buscarPorCarnet(String carnet) {
        return repositorioUsuario.findByCarnet(carnet).orElse(null);
    }
}
