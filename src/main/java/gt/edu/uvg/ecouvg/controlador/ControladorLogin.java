package gt.edu.uvg.ecouvg.controlador;

import gt.edu.uvg.ecouvg.modelo.Usuario;
import gt.edu.uvg.ecouvg.servicio.ServicioUsuario;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

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

    @GetMapping("/inicio")
    public String mostrarInicio(Authentication authentication, Model model) {

        String carnet = authentication.getName();

        Usuario usuario = servicioUsuario.buscarPorCarnet(carnet);

        model.addAttribute("usuario", usuario);

        return "inicio";
    }
}