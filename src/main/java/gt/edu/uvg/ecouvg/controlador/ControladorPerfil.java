
package gt.edu.uvg.ecouvg.controlador;

import gt.edu.uvg.ecouvg.modelo.Usuario;
import gt.edu.uvg.ecouvg.modelo.Ranking;
import gt.edu.uvg.ecouvg.servicio.ServicioUsuario;
import gt.edu.uvg.ecouvg.servicio.ServicioReto;
import gt.edu.uvg.ecouvg.servicio.ServicioPerfil;
import gt.edu.uvg.ecouvg.servicio.ServicioSocial;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.http.CacheControl;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.dao.DataAccessException;
import org.springframework.web.client.RestClientException;

@Controller
public class ControladorPerfil {
    private static final Logger log = LoggerFactory.getLogger(ControladorPerfil.class);

    private final ServicioUsuario servicioUsuario;
    private final ServicioReto servicioReto;
    private final ServicioPerfil servicioPerfil;
    private final ServicioSocial servicioSocial;

    public ControladorPerfil(ServicioUsuario servicioUsuario, ServicioReto servicioReto, ServicioPerfil servicioPerfil, ServicioSocial servicioSocial) {
        this.servicioUsuario = servicioUsuario;
        this.servicioReto = servicioReto;
        this.servicioPerfil = servicioPerfil;
        this.servicioSocial = servicioSocial;
    }

    @GetMapping("/perfil")
    public String mostrarPerfil(Authentication authentication, @RequestParam(required = false) Integer nivel, Model model) {

        if (authentication == null) {
            return "redirect:/login";
        }
        Usuario usuario = servicioUsuario.buscarPorCarnet(authentication.getName());

        if (usuario == null) {
            return "redirect:/login";
        }

        return cargarPerfil(usuario, usuario, nivel, model);
    }

    @GetMapping("/perfil/{id}")
    public String verPerfil(@PathVariable Long id, Authentication authentication,
                            @RequestParam(required = false) Integer nivel, Model model) {
        if (authentication == null) return "redirect:/login";
        Usuario actual = servicioUsuario.buscarPorCarnet(authentication.getName());
        if (actual == null) return "redirect:/login";
        if (id.equals(actual.getId())) return "redirect:/perfil";
        Usuario perfil = servicioUsuario.buscarPorId(id);
        if (perfil == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return cargarPerfil(perfil, actual, nivel, model);
    }

    private String cargarPerfil(Usuario usuario, Usuario actual, Integer nivel, Model model) {
        model.addAttribute("perfilPropio", usuario.getId().equals(actual.getId()));
        model.addAttribute("usuarioActualId", actual.getId());
        model.addAttribute("social", servicioSocial.resumen(usuario.getId(), actual.getId()));
        int nivelSeleccionado = usuario.getNivel();

        if (nivel != null && nivel >= 1 && nivel <= 6) {
            nivelSeleccionado = nivel;
        }

        Ranking ranking = servicioUsuario.obtenerRanking(nivelSeleccionado);

        model.addAttribute("usuario", usuario);
        model.addAttribute("ranking", ranking);
        model.addAttribute("nivelSeleccionado", nivelSeleccionado);
        model.addAttribute("misionesCompletadas", servicioReto.contarMisionesCompletadas(usuario.getId()));

        return "perfil";
    }

    @GetMapping("/perfil/foto")
    public ResponseEntity<byte[]> obtenerFoto(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).cacheControl(CacheControl.noStore()).build();
        }
        try {
            byte[] imagen = servicioPerfil.obtenerFoto(authentication.getName());
            if (imagen == null || imagen.length == 0) {
                return ResponseEntity.notFound().cacheControl(CacheControl.noStore()).build();
            }
            return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .contentType(MediaType.IMAGE_JPEG).body(imagen);
        } catch (RestClientException | IllegalArgumentException e) {
            log.warn("No se pudo cargar la foto de perfil desde Storage", e);
            return ResponseEntity.status(502).cacheControl(CacheControl.noStore()).build();
        }
    }

    @GetMapping("/perfil/{id}/foto")
    public ResponseEntity<byte[]> obtenerFotoPersona(@PathVariable Long id, Authentication authentication) {
        if (authentication == null) return ResponseEntity.status(401).cacheControl(CacheControl.noStore()).build();
        try {
            byte[] imagen = servicioPerfil.obtenerFotoPorId(id);
            if (imagen == null || imagen.length == 0) return ResponseEntity.notFound().cacheControl(CacheControl.noStore()).build();
            return ResponseEntity.ok().cacheControl(CacheControl.noStore()).contentType(MediaType.IMAGE_JPEG).body(imagen);
        } catch (RestClientException | IllegalArgumentException e) {
            log.warn("No se pudo cargar la foto de perfil", e);
            return ResponseEntity.status(502).cacheControl(CacheControl.noStore()).build();
        }
    }

    @PostMapping("/perfil/foto")
    public String actualizarFoto(Authentication authentication,
                                 @RequestParam(required = false) MultipartFile fotografia,
                                 RedirectAttributes atributos) {
        if (authentication == null) {
            return "redirect:/login";
        }
        try {
            servicioPerfil.actualizarFoto(authentication.getName(), fotografia);
            atributos.addFlashAttribute("mensaje", "Tu foto de perfil se actualizó correctamente.");
        } catch (IllegalArgumentException e) {
            atributos.addFlashAttribute("error", e.getMessage());
        } catch (IOException | RestClientException | DataAccessException e) {
            log.warn("No se pudo guardar la foto de perfil", e);
            atributos.addFlashAttribute("error", "No se pudo guardar la foto. Inténtalo de nuevo.");
        }
        return "redirect:/perfil";
    }

    @PostMapping("/perfil/contrasena")
    public String cambiarContrasena(Authentication authentication,
                                    @RequestParam String contrasenaActual,
                                    @RequestParam String nuevaContrasena,
                                    @RequestParam String confirmarContrasena,
                                    RedirectAttributes atributos) {
        if (authentication == null) {
            return "redirect:/login";
        }
        try {
            servicioPerfil.cambiarContrasena(authentication.getName(), contrasenaActual, nuevaContrasena, confirmarContrasena);
            atributos.addFlashAttribute("mensaje", "Tu contraseña se actualizó correctamente.");
        } catch (IllegalArgumentException e) {
            atributos.addFlashAttribute("errorContrasena", e.getMessage());
        } catch (DataAccessException e) {
            atributos.addFlashAttribute("errorContrasena", "No se pudo cambiar la contraseña. Inténtalo de nuevo.");
        }
        return "redirect:/perfil";
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String fotografiaDemasiadoGrande(RedirectAttributes atributos) {
        atributos.addFlashAttribute("error", "La fotografía no debe superar los 5 MB.");
        return "redirect:/perfil";
    }
}
