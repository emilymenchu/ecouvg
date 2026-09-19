package gt.edu.uvg.ecouvg.controlador;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ControladorLogin {
    @GetMapping("/login")
    public String mostrarLogin() {
        return "login";
    }

    @GetMapping("/inicio")
    public String mostrarInicio() {
        return "inicio";
    }
}