package gt.edu.uvg.ecouvg.controlador;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.security.core.Authentication;
import gt.edu.uvg.ecouvg.servicio.ServicioUsuario;
import gt.edu.uvg.ecouvg.modelo.Usuario;


    @Controller
public class ControladorRecompensa {
    private final ServicioUsuario servicioUsuario; 

    public ControladorRecompensa(ServicioUsuario servicioUsuario){
        this.servicioUsuario = servicioUsuario; 
    }

    @GetMapping("/recompensa")
    public String mostrarRecompensas(Authentication authentication,Model model ) {
        if (authentication == null) {
            return "redirect:/login";
        }

        Usuario usuario = servicioUsuario.buscarPorCarnet(authentication.getName());

        if (usuario == null) {
            return "redirect:/login";
        }

        model.addAttribute("usuario", usuario);

        return "recompensa";
    }

}
