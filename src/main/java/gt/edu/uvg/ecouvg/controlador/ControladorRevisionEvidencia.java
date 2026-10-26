package gt.edu.uvg.ecouvg.controlador;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import gt.edu.uvg.ecouvg.modelo.EstadoEvidencia;
import gt.edu.uvg.ecouvg.modelo.Evidencia;
import gt.edu.uvg.ecouvg.modelo.Usuario; 
import gt.edu.uvg.ecouvg.repositorio.RepositorioEvidencia;
import gt.edu.uvg.ecouvg.repositorio.RepositorioRevisionEvidencia;
import gt.edu.uvg.ecouvg.servicio.ServicioRevisionEvidencia;
import gt.edu.uvg.ecouvg.servicio.ServicioStorage;
import gt.edu.uvg.ecouvg.servicio.ServicioUsuario;


@Controller 
@RequestMapping("/revisiones")
public class ControladorRevisionEvidencia {
    private final ServicioRevisionEvidencia servicioRevisionEvidencia; 
    private final ServicioUsuario servicioUsuario; 
    private final ServicioStorage servicioStorage; 
    private final RepositorioEvidencia repositorioEvidencia; 
    private final RepositorioRevisionEvidencia repositorioRevisionEvidencia; 


    public ControladorRevisionEvidencia(ServicioRevisionEvidencia servicioRevisionEvidencia, ServicioUsuario servicioUsuario,ServicioStorage servicioStorage,  RepositorioEvidencia repositorioEvidencia,RepositorioRevisionEvidencia repositorioRevisionEvidencia){
        this.servicioRevisionEvidencia = servicioRevisionEvidencia; 
        this.servicioUsuario = servicioUsuario; 
        this.servicioStorage = servicioStorage; 
        this.repositorioEvidencia = repositorioEvidencia; 
        this.repositorioRevisionEvidencia = repositorioRevisionEvidencia; 
    }
    
    @GetMapping("/pendientes")
    public String mostrarEvidenciasPendientes(Authentication authentication, Model model){
        if (authentication == null || !authentication.isAuthenticated() ||"anonymousUser".equals(authentication.getPrincipal())){
            return "redirect:/login"; 
        }
        Usuario usuario = servicioUsuario.buscarPorCarnet(authentication.getName()); 

        if (usuario == null){
            return "redirect:/login";
        }

        List<Evidencia> pendientes = repositorioEvidencia.findByEstado(EstadoEvidencia.PENDIENTE); 
        List<Evidencia> disponibles = new ArrayList<>(); 
        Map <Long, String> urlsImagenes = new HashMap<>(); 

        for (Evidencia evidencia :pendientes){
            if (evidencia.getUsuario().getId().equals(usuario.getId())){
                continue; 
            }

            boolean yaReviso = repositorioRevisionEvidencia.existsByEvidenciaIdAndRevisorId(evidencia.getId(), usuario.getId()); 
            if (yaReviso){ 
                continue; 

            }
            
            String url = servicioStorage.obtenerUrlFirmada("evidencias", evidencia.getRutaFotografia(), 500); 
            disponibles.add(evidencia);
            urlsImagenes.put(evidencia.getId(), url); 
        }


        model.addAttribute("evidencias", disponibles); 
        model.addAttribute("urlsImagenes", urlsImagenes);
        return "revisar-evidencias"; 

    }

    @PostMapping("/aprobar/{idEvidencia}")
    public String aprobarEvidencia(@PathVariable Long idEvidencia, Authentication authentication, RedirectAttributes atributos) {

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())){
            return "redirect:/login"; 
        }


        try {
            Usuario usuario = servicioUsuario.buscarPorCarnet(authentication.getName());
            if(usuario == null){
                throw new IllegalArgumentException("Usuario no encontrado :/ "); 
            }
            servicioRevisionEvidencia.registrarRevision(idEvidencia, usuario.getId(), true);
            atributos.addFlashAttribute("mensaje", "aprobación correctamente registrada : D "); 
            } catch (IllegalArgumentException | IllegalStateException e ){
                atributos.addFlashAttribute("error", e.getMessage());

        }
        return "redirect:/revisiones/pendientes"; 
        
    }

    @PostMapping("/rechazar/{idEvidencia}")
    public String rechazarEvidencia(@PathVariable Long idEvidencia, Authentication authentication, RedirectAttributes atributos){
        if (authentication == null || !authentication.isAuthenticated()|| "anonymousUser".equals(authentication.getPrincipal())){
            return "redirect:/login";

        }
        try{
            Usuario usuario = servicioUsuario.buscarPorCarnet(authentication.getName());
            if (usuario == null) {
                throw new IllegalArgumentException("Usuario no encontrado :/ "); 
            }

        servicioRevisionEvidencia.registrarRevision(idEvidencia, usuario.getId(), false);
        atributos.addFlashAttribute("mensaje", "rechazo correctamente registrada : D "); 


        }catch (IllegalArgumentException | IllegalStateException e ){
            atributos.addFlashAttribute("error", e.getMessage());

        }
        return "redirect:/revisiones/pendientes"; 
    }
    
}
