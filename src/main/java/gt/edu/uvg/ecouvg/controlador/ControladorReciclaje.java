package gt.edu.uvg.ecouvg.controlador;

import gt.edu.uvg.ecouvg.modelo.Residuo;
import gt.edu.uvg.ecouvg.servicio.ServicioReciclaje;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class ControladorReciclaje {
    private final ServicioReciclaje servicio;

    public ControladorReciclaje(ServicioReciclaje servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/residuos")
    public String mostrarResiduos(Model model) {
        List<Residuo> residuos = servicio.obtenerResiduos();
        model.addAttribute("residuos", residuos);
        return "residuos";
    }
}
