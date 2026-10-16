package gt.edu.uvg.ecouvg.controlador;
import gt.edu.uvg.ecouvg.servicio.ServicioRevisionEvidencia;
import gt.edu.uvg.ecouvg.servicio.ServicioUsuario;
import gt.edu.uvg.ecouvg.servicio.ServicioStorage;
import gt.edu.uvg.ecouvg.modelo.Usuario;
import gt.edu.uvg.ecouvg.modelo.Evidencia;
import gt.edu.uvg.ecouvg.modelo.EstadoEvidencia;
import gt.edu.uvg.ecouvg.repositorio.RepositorioEvidencia;
import gt.edu.uvg.ecouvg.repositorio.RepositorioRevisionEvidencia;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import java.util.List;
import java.util.ArrayList;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Map;
import java.util.HashMap;


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
    

    
}
