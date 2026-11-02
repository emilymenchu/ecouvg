package gt.edu.uvg.ecouvg.servicio;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import gt.edu.uvg.ecouvg.modelo.EstadoEvidencia;
import gt.edu.uvg.ecouvg.modelo.Evidencia;
import gt.edu.uvg.ecouvg.repositorio.RepositorioEvidencia;

@Service 
public class ServicioLimpiezaEvidencia {
    private final RepositorioEvidencia repositorioEvidencia; 
    private final ServicioStorage servicioStorage; 
    private final ServicioExpirarEvidencia servicioExpirarEvidencia; 
    

    public ServicioLimpiezaEvidencia(RepositorioEvidencia repositorioEvidencia, ServicioStorage servicioStorage, ServicioExpirarEvidencia servicioExpirarEvidencia){
        this.repositorioEvidencia = repositorioEvidencia; 
        this.servicioStorage = servicioStorage; 
        this.servicioExpirarEvidencia = servicioExpirarEvidencia; 
    }   

   
    public List<Evidencia> buscarEvidenciasVencidas(){
        LocalDate hoy = LocalDate.now(ZoneId.of("America/Guatemala"));
        LocalDateTime inicioDelDia = hoy.atStartOfDay(); 
        return repositorioEvidencia.findByEstadoAndFechaEnvioBefore(EstadoEvidencia.PENDIENTE, inicioDelDia); 
        
    }

   public void limpiarEvidencia(List<Evidencia> vencidas){
        LocalDate hoy = LocalDate.now(ZoneId.of("America/Guatemala"));
        for (Evidencia evidencia : vencidas){
            String ruta = servicioExpirarEvidencia.expirarEvidencia(evidencia.getId(), hoy); 
            if (ruta!=null){
                servicioStorage.eliminarImagen("evidencias", ruta);
            }
        }
   }

    @Scheduled (cron = "0 0 0 * * *", zone = "America/Guatemala")
    public void limpiar(){
        List<Evidencia> vencidas = buscarEvidenciasVencidas(); 
        limpiarEvidencia(vencidas);
    }
}
