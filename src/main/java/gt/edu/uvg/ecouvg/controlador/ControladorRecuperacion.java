package gt.edu.uvg.ecouvg.controlador;

import gt.edu.uvg.ecouvg.modelo.Usuario;
import gt.edu.uvg.ecouvg.servicio.ServicioRecuperacionContrasena;
import gt.edu.uvg.ecouvg.servicio.ServicioUsuario;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class ControladorRecuperacion {

    private final ServicioRecuperacionContrasena servicioRecuperacion;
    private final ServicioUsuario servicioUsuario;

    public ControladorRecuperacion(
            ServicioRecuperacionContrasena servicioRecuperacion,
            ServicioUsuario servicioUsuario) {

        this.servicioRecuperacion = servicioRecuperacion;
        this.servicioUsuario = servicioUsuario;
    }

    @GetMapping("/recuperar")
    public String mostrarRecuperacion() {
        return "recuperar";
    }

    @PostMapping("/recuperar")
    public String solicitarCodigo(
            @RequestParam String correo,
            Model model) {

        boolean enviado =
                servicioRecuperacion.solicitarCodigo(correo);

        if (!enviado) {
            model.addAttribute(
                    "error",
                    "No existe una cuenta con ese correo."
            );

            return "recuperar";
        }

        model.addAttribute("correo", correo);

        return "verificar-codigo";
    }

    @PostMapping("/verificar-codigo")
    public String verificarCodigo(
            @RequestParam String correo,
            @RequestParam String codigo,
            HttpSession session,
            Model model) {

        Usuario usuario =
                servicioRecuperacion.validarCodigo(
                        correo,
                        codigo
                );

        if (usuario == null) {
            model.addAttribute(
                    "error",
                    "El código es incorrecto o expiró."
            );

            model.addAttribute("correo", correo);

            return "verificar-codigo";
        }

        session.setAttribute(
                "usuarioRecuperacion",
                usuario.getId()
        );

        return "nueva-contrasena";
    }

    @PostMapping("/nueva-contrasena")
    public String cambiarContrasena(
            @RequestParam String contrasena,
            @RequestParam String confirmarContrasena,
            HttpSession session,
            Model model) {

        if (!contrasena.equals(confirmarContrasena)) {
            model.addAttribute(
                    "error",
                    "Las contraseñas no coinciden."
            );

            return "nueva-contrasena";
        }

        Long idUsuario =
                (Long) session.getAttribute(
                        "usuarioRecuperacion"
                );

        if (idUsuario == null) {
            return "redirect:/recuperar";
        }

        Usuario usuario = servicioUsuario.buscarPorId(idUsuario);

        servicioUsuario.cambiarContrasena(usuario, contrasena);

        servicioRecuperacion.marcarComoUsado(usuario);

        session.removeAttribute("usuarioRecuperacion");

        return "redirect:/login?recuperada=true";
    }
}