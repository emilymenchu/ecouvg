package gt.edu.uvg.ecouvg.controlador;

import java.util.List;
import java.util.Optional; 

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam; 

import gt.edu.uvg.ecouvg.modelo.Residuo;
import gt.edu.uvg.ecouvg.servicio.ServicioReciclaje; 

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

    @GetMapping("/residuos/buscar")
    public String consultarResiduo(@RequestParam String nombre, Model model){
        Optional<Residuo> residuo = servicio.buscarResiduo(nombre);

        if (residuo.isEmpty()){
            return "residuo-no-encontrado"; 
        }

        model.addAttribute("residuo", residuo.get()); 
        return "consulta-residuo"; 
    }
   
}

