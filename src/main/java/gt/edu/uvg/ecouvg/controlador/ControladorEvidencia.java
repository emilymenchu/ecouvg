package gt.edu.uvg.ecouvg.controlador;
import gt.edu.uvg.ecouvg.servicio.ServicioEvidencia;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.IOException;
import gt.edu.uvg.ecouvg.modelo.Usuario;
import gt.edu.uvg.ecouvg.servicio.ServicioUsuario;
import org.springframework.security.core.Authentication;

@Controller
@RequestMapping("/evidencias")
public class ControladorEvidencia {
    private final ServicioEvidencia servicioEvidencia;
    private final ServicioUsuario servicioUsuario;  

    public ControladorEvidencia(ServicioEvidencia servicioEvidencia, ServicioUsuario servicioUsuario){
        this.servicioEvidencia = servicioEvidencia; 
        this.servicioUsuario = servicioUsuario;
    }

    @GetMapping("/enviar/{idProgreso}")
    public String mostrarFormulario(@PathVariable long idProgreso, Authentication authentication, Model model){ 
         if (authentication == null ||!authentication.isAuthenticated() ||"anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/login";
        }

        String carnet = authentication.getName();
        Usuario usuario = servicioUsuario.buscarPorCarnet(carnet);

        if (usuario == null) {
            return "redirect:/login";
        }

        model.addAttribute("idProgreso", idProgreso);
        return "envio-evidencia";
        }

    @PostMapping("/enviar")
    public String enviarEvidencia(@RequestParam Long idProgreso, @RequestParam("fotografia") MultipartFile fotografia, Authentication authentication, RedirectAttributes atributos){
        if (authentication == null ||!authentication.isAuthenticated()) {
            atributos.addFlashAttribute("error", "Debes iniciar sesión");
        return "redirect:/login";
    } 
    try {
        String carnet = authentication.getName();
        Usuario usuario = servicioUsuario.buscarPorCarnet(carnet);

        if (usuario == null) {
            throw new IllegalArgumentException("Usuario no encontrado");
        }

        servicioEvidencia.registrarEvidencia(usuario.getId(),idProgreso,fotografia );
        atributos.addFlashAttribute("mensaje","Fotografía enviada. Pendiente de revisión.");

    } catch (IllegalArgumentException | IllegalStateException e) {
        atributos.addFlashAttribute("error", e.getMessage());

    } catch (IOException e) {
        atributos.addFlashAttribute("error","No se pudo procesar la fotografía" );
    }
    return "redirect:/evidencias/enviar/" +idProgreso; 
    }
}
