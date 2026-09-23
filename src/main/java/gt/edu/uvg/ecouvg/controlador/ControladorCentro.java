package gt.edu.uvg.ecouvg.controlador;

import gt.edu.uvg.ecouvg.servicio.ServicioCentro;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class ControladorCentro {
    private final ServicioCentro servicio;
    public ControladorCentro(ServicioCentro servicio) { this.servicio = servicio; }

    @GetMapping({"/centros", "/mapa"})
    public String consultarCentros(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "") String material, Model model) {
        if (q.length() > 120 || material.length() > 120) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La búsqueda admite hasta 120 caracteres.");
        }
        model.addAttribute("centros", servicio.buscarCentros(q, material));
        model.addAttribute("materiales", servicio.obtenerMateriales());
        model.addAttribute("q", q);
        model.addAttribute("material", material);
        return "centros";
    }

    @GetMapping("/centros/{id}")
    public String consultarCentro(@PathVariable Long id, Model model) {
        model.addAttribute("centro", servicio.obtenerCentro(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Centro no encontrado")));
        return "centro-detalle";
    }
}
