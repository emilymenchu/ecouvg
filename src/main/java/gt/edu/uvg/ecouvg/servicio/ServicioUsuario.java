package gt.edu.uvg.ecouvg.servicio;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import gt.edu.uvg.ecouvg.modelo.Usuario;
import gt.edu.uvg.ecouvg.repositorio.RepositorioUsuario;

@Service
public class ServicioUsuario {

    private final RepositorioUsuario repositorioUsuario;
    private final PasswordEncoder passwordEncoder;

    public ServicioUsuario(RepositorioUsuario repositorioUsuario, PasswordEncoder passwordEncoder) {
        this.repositorioUsuario = repositorioUsuario;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario buscarPorCarnet(String carnet) {
        return repositorioUsuario.findByCarnet(carnet).orElse(null);
    }

    public boolean carnetExiste(String carnet) {
        return repositorioUsuario.existsByCarnet(carnet);
    }

    public Usuario registrarUsuario(
        String nombre,
        String carnet,
        String correo,
        String contrasena) {

        String contrasenaEncriptada = passwordEncoder.encode(contrasena);

        Usuario usuario = new Usuario(
                nombre,
                carnet,
                correo,
                contrasenaEncriptada
        );

        return repositorioUsuario.save(usuario);
    }

    public boolean correoExiste(String correo) {
        return repositorioUsuario.existsByCorreo(correo);
    }
}