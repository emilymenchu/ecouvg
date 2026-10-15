package gt.edu.uvg.ecouvg.servicio;

import gt.edu.uvg.ecouvg.modelo.RecuperacionContrasena;
import gt.edu.uvg.ecouvg.modelo.Usuario;
import gt.edu.uvg.ecouvg.repositorio.RepositorioRecuperacionContrasena;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.stereotype.Service;

@Service
public class ServicioRecuperacionContrasena {

    private final RepositorioRecuperacionContrasena repositorio;
    private final ServicioUsuario servicioUsuario;
    private final ServicioCorreo servicioCorreo;

    public ServicioRecuperacionContrasena(RepositorioRecuperacionContrasena repositorio, ServicioUsuario servicioUsuario, ServicioCorreo servicioCorreo) {
        this.repositorio = repositorio;
        this.servicioUsuario = servicioUsuario;
        this.servicioCorreo = servicioCorreo;
    }

    public boolean solicitarCodigo(String correo) {

        Usuario usuario = servicioUsuario.buscarPorCorreo(correo);

        if (usuario == null) {
            return false;
        }

        String codigo = String.format(
                "%06d",
                new Random().nextInt(1000000)
        );

        RecuperacionContrasena recuperacion =
                new RecuperacionContrasena(
                        usuario,
                        codigo,
                        LocalDateTime.now().plusMinutes(10)
                );

        repositorio.save(recuperacion);

        servicioCorreo.enviarCodigoRecuperacion(
                correo,
                codigo
        );

        return true;
    }

    public Usuario validarCodigo(
            String correo,
            String codigo) {

        Usuario usuario = servicioUsuario.buscarPorCorreo(correo);

        if (usuario == null) {
            return null;
        }

        RecuperacionContrasena recuperacion =
                repositorio
                    .findTopByUsuarioOrderByIdDesc(usuario)
                    .orElse(null);

        if (recuperacion == null) {
            return null;
        }

        if (!recuperacion.esValido(codigo)) {
            return null;
        }

        return usuario;
    }

    public void marcarComoUsado(Usuario usuario) {

        RecuperacionContrasena recuperacion =
                repositorio
                    .findTopByUsuarioOrderByIdDesc(usuario)
                    .orElse(null);

        if (recuperacion != null) {
            recuperacion.setUsado(true);
            repositorio.save(recuperacion);
        }
    }
}