package gt.edu.uvg.ecouvg.controlador;

import gt.edu.uvg.ecouvg.dto.MaterialArcade;
import gt.edu.uvg.ecouvg.servicio.ServicioArcade;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ControladorArcade {
    private final ServicioArcade servicio;
    public ControladorArcade(ServicioArcade servicio) { this.servicio = servicio; }

    @GetMapping("/arcade")
    public String mostrar() { return "arcade"; }

    @GetMapping("/api/arcade/materiales")
    @ResponseBody
    public List<MaterialArcade> materiales() { return servicio.obtenerMateriales(); }
}
