package gt.edu.uvg.ecouvg.controlador;
import java.time.LocalDate;
import java.util.List; 

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import gt.edu.uvg.ecouvg.modelo.Usuario;
import java.util.Map;
import java.util.HashMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.PathVariable;
import gt.edu.uvg.ecouvg.modelo.ProgresoReto;
import gt.edu.uvg.ecouvg.modelo.Reto;
import gt.edu.uvg.ecouvg.servicio.ServicioReto;
import gt.edu.uvg.ecouvg.servicio.ServicioUsuario;

@Controller 
public class ControladorReto {
    private final ServicioReto servicioReto; 
    private final ServicioUsuario servicioUsuario; 

    public ControladorReto(ServicioReto servicioReto, ServicioUsuario servicioUsuario){
        this.servicioReto = servicioReto; 
        this.servicioUsuario = servicioUsuario; 
    }

    public List<Reto> consultarRetosSemanales() {
        return servicioReto.obtenerRetosSemanales(LocalDate.now()); 

    }

    public ProgresoReto consultarProgreso(Long idUsuario, Long idReto){
        return servicioReto.obtenerProgreso(idUsuario, idReto);
    }
    
    public ProgresoReto actualizarProgreso(Long idUsuario, Long idReto, int progreso){
        return servicioReto.actualizarProgreso(idUsuario, idReto, progreso);

    }

    @GetMapping("/retos") 
    public String mostrarRetos(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated() ||"anonymousUser".equals(authentication.getPrincipal())){
            return "redirect:/login"; 
        }
        String carnet = authentication.getName();
        Usuario usuario = servicioUsuario.buscarPorCarnet(carnet); 
        if (usuario== null){
            return "redirect:/login"; 
        }
        List<Reto> retos = consultarRetosSemanales(); 
        Map<Long, ProgresoReto> progresos = new HashMap<>(); 
        for (Reto reto :retos){
            ProgresoReto progreso = servicioReto.obtenerProgreso(usuario.getId(), reto.getId());
            
            if (progreso !=null){
                progresos.put(reto.getId(), progreso) ;
            }
        }

        
        model.addAttribute("retos", retos); 
        model.addAttribute("progresos", progresos); 

        return "retos"; 
    }

    @PostMapping("/retos/iniciar/{idReto}")
    public String inicioReto(@PathVariable Long idReto, Authentication authentication, RedirectAttributes atributos){
        if (authentication == null || !authentication.isAuthenticated()|| "anonymousUser".equals(authentication.getPrincipal())){
            return "redirect/:login"; 
        }
        try{
            String carnet = authentication.getName();
            Usuario usuario = servicioUsuario.buscarPorCarnet(carnet); 
            if (usuario ==null){
                throw new IllegalArgumentException("usuario no encontrado");
            }
            ProgresoReto progreso = servicioReto.inicioReto(usuario.getId(), idReto);
            return  "redirect:/evidencias/enviar/" + progreso.getId();
        }catch (IllegalArgumentException | IllegalStateException e) {
            atributos.addFlashAttribute("error", e.getMessage());
            return "redirect:/retos";
        }
    }
    
}
