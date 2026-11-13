package gt.edu.uvg.ecouvg.controlador;
import gt.edu.uvg.ecouvg.dto.*;
import gt.edu.uvg.ecouvg.servicio.ServicioMisionArcade;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

@RestController
@RequestMapping("/api/arcade/mision")
public class ControladorMisionArcade {
    private final ServicioMisionArcade servicio;
    public ControladorMisionArcade(ServicioMisionArcade servicio) { this.servicio=servicio; }
    @GetMapping public ResponseEntity<EstadoMisionArcade> estado(Authentication auth) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(servicio.consultar(auth.getName()));
    }
    @PostMapping public EstadoMisionArcade completar(Authentication auth,@RequestBody SolicitudMisionArcade solicitud) {
        return servicio.completar(auth.getName(),solicitud);
    }
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public java.util.Map<String,String> conflicto() {
        return java.util.Map.of("mensaje","Tu actividad cambió al mismo tiempo. Reintenta guardar la misión.");
    }
}
