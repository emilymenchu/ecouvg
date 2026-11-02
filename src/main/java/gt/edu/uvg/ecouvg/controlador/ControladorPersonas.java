package gt.edu.uvg.ecouvg.controlador;

import gt.edu.uvg.ecouvg.modelo.Usuario;
import gt.edu.uvg.ecouvg.servicio.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class ControladorPersonas {
    private final ServicioUsuario usuarios;
    private final ServicioSocial social;
    public ControladorPersonas(ServicioUsuario usuarios, ServicioSocial social) {
        this.usuarios = usuarios;
        this.social = social;
    }
    private Usuario actual(Authentication autenticacion) {
        if (autenticacion == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        Usuario actual = usuarios.buscarPorCarnet(autenticacion.getName());
        if (actual == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return actual;
    }
    @GetMapping("/personas")
    public String buscar(Authentication autenticacion, @RequestParam(defaultValue = "") String q,
                         @RequestParam(defaultValue = "0") int pagina, Model modelo) {
        modelo.addAttribute("usuarioActualId", actual(autenticacion).getId());
        modelo.addAttribute("resultados", social.buscar(q, pagina));
        modelo.addAttribute("q", q.strip());
        modelo.addAttribute("titulo", "Buscar personas");
        modelo.addAttribute("ruta", "/personas");
        return "personas";
    }
    @GetMapping("/perfil/{id}/{lista:seguidores|seguidos|amigos}")
    public String lista(@PathVariable Long id, @PathVariable String lista, Authentication autenticacion,
                        @RequestParam(defaultValue = "0") int pagina, Model modelo) {
        modelo.addAttribute("usuarioActualId", actual(autenticacion).getId());
        Usuario perfil = usuarios.buscarPorId(id);
        if (perfil == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        modelo.addAttribute("resultados", social.listar(id, lista, pagina));
        modelo.addAttribute("titulo", Character.toUpperCase(lista.charAt(0)) + lista.substring(1) + " de " + perfil.getNombre());
        modelo.addAttribute("ruta", "/perfil/" + id + "/" + lista);
        modelo.addAttribute("q", "");
        modelo.addAttribute("perfilOrigen", id);
        return "personas";
    }
    @PostMapping("/personas/{id}/seguir")
    public String seguir(@PathVariable Long id, Authentication autenticacion) {
        social.seguir(actual(autenticacion).getId(), id);
        return "redirect:/perfil/" + id;
    }
    @PostMapping("/personas/{id}/dejar-seguir")
    public String dejarDeSeguir(@PathVariable Long id, Authentication autenticacion) {
        social.dejarDeSeguir(actual(autenticacion).getId(), id);
        return "redirect:/perfil/" + id;
    }
}
