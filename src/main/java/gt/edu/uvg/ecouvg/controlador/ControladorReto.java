package gt.edu.uvg.ecouvg.controlador;
import java.time.LocalDate; 
import java.util.List; 

import gt.edu.uvg.ecouvg.modelo.Reto;
import gt.edu.uvg.ecouvg.servicio.ServicioReto;
import gt.edu.uvg.ecouvg.modelo.ProgresoReto; 
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller 
public class ControladorReto {
    private final ServicioReto servicioReto; 

    public ControladorReto(ServicioReto servicioReto){
        this.servicioReto = servicioReto; 
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
    public String mostrarRetos(Model model) {
        List<Reto> retos = consultarRetosSemanales(); 

        model.addAttribute("retos", retos); 

        return "retos"; 
    }
    
}
