package gt.edu.uvg.ecouvg.controlador;

import gt.edu.uvg.ecouvg.modelo.Usuario;
import gt.edu.uvg.ecouvg.servicio.ServicioUsuario;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ControladorLogin {

    private final ServicioUsuario servicioUsuario;

    public ControladorLogin(ServicioUsuario servicioUsuario) {
        this.servicioUsuario = servicioUsuario;
    }

    @GetMapping("/login")
    public String mostrarLogin() {
        return "login";
    }

    @GetMapping("/registro")
    public String mostrarRegistro() {
        return "registro";
    }

    @PostMapping("/registro")
    public String registrarUsuario(
            @RequestParam String nombre,
            @RequestParam String carnet,
            @RequestParam String correo,
            @RequestParam String contrasena,
            @RequestParam String confirmarContrasena,
            Model model) {

        if (!contrasena.equals(confirmarContrasena)) {
            model.addAttribute("error", "Las contraseñas no coinciden.");
            return "registro";
        }

        if (servicioUsuario.carnetExiste(carnet)) {
            model.addAttribute("error", "Ya existe una cuenta con ese carné.");
            return "registro";
        }

        if (servicioUsuario.correoExiste(correo)) {
            model.addAttribute("error", "Ya existe una cuenta con ese correo.");
            return "registro";
        }

        servicioUsuario.registrarUsuario(nombre, carnet, correo, contrasena);

        return "redirect:/login?registro=true";
    }

    @GetMapping("/inicio")
    public String mostrarInicio(Authentication authentication, Model model) {

        String carnet = authentication.getName();

        Usuario usuario = servicioUsuario.buscarPorCarnet(carnet);

        model.addAttribute("usuario", usuario);

        return "inicio";
    }
}