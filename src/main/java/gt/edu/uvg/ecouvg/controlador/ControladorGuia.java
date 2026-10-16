package gt.edu.uvg.ecouvg.controlador;

import gt.edu.uvg.ecouvg.servicio.ServicioGuia;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Controller
public class ControladorGuia {
    private final ServicioGuia servicio;
    public ControladorGuia(ServicioGuia servicio) { this.servicio = servicio; }

    @GetMapping("/guia")
    public String mostrar(@RequestParam(name = "q", defaultValue = "") String q, Model model) {
        validar(q);
        model.addAttribute("q", q);
        model.addAttribute("categorias", servicio.buscar(q));
        return "guia";
    }
    @GetMapping("/guia/{slug}")
    public String detalle(@PathVariable("slug") String slug,
            @RequestParam(name = "q", defaultValue = "") String q, Model model) {
        validar(q);
        model.addAttribute("categoria", servicio.obtener(slug).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría no encontrada")));
        model.addAttribute("q", q);
        return "guia-detalle";
    }
    private void validar(String q) {
        if (q.length() > 120) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            "La búsqueda admite hasta 120 caracteres.");
    }
}
