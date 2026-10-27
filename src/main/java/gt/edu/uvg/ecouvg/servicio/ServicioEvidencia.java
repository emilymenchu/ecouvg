package gt.edu.uvg.ecouvg.servicio;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import gt.edu.uvg.ecouvg.modelo.EstadoEvidencia;
import gt.edu.uvg.ecouvg.modelo.Evidencia;
import gt.edu.uvg.ecouvg.modelo.ProgresoReto;
import gt.edu.uvg.ecouvg.modelo.Usuario;
import gt.edu.uvg.ecouvg.repositorio.RepositorioEvidencia;
import gt.edu.uvg.ecouvg.repositorio.RepositorioProgresoReto;
import gt.edu.uvg.ecouvg.repositorio.RepositorioUsuario;

@Service 
public class ServicioEvidencia {
    private final RepositorioEvidencia repositorioEvidencia; 
    private final RepositorioProgresoReto repositorioProgresoReto; 
    private final RepositorioUsuario repositorioUsuario;
    private final ServicioStorage servicioStorage;


    public ServicioEvidencia(RepositorioEvidencia repositorioEvidencia, RepositorioProgresoReto repositorioProgresoReto, RepositorioUsuario repositorioUsuario, ServicioStorage servicioStorage){
        this.repositorioEvidencia = repositorioEvidencia; 
        this.repositorioProgresoReto = repositorioProgresoReto; 
        this.repositorioUsuario = repositorioUsuario; 
        this.servicioStorage = servicioStorage; 
    }

        
    @Transactional
    public Evidencia registrarEvidencia(Long idUsuario,Long idProgreso,MultipartFile fotografia) throws IOException {
        
        Usuario usuario = repositorioUsuario.findById(idUsuario).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        ProgresoReto progresoReto =
            repositorioProgresoReto.findById(idProgreso).orElseThrow(() -> new IllegalArgumentException("Progreso no encontrado"));

        if (!usuario.getId().equals(progresoReto.getUsuario().getId())) {
            throw new IllegalArgumentException( "No puedes enviar evidencia para otra persona" );
        }

        if (progresoReto.estaCompletado()) {
            throw new IllegalStateException("La misión ya está completada" );
        }

        if(!progresoReto.retoEstaDisponible(LocalDate.now())){
            throw new IllegalStateException("La misión no está disponible" );
        }

        if (fotografia == null ||fotografia.isEmpty()) {
            throw new IllegalArgumentException("Debes adjuntar una fotografía" );
        }

        List<Evidencia> evidencias =repositorioEvidencia.findByProgresoRetoId(idProgreso);

        for (Evidencia evidencia : evidencias) {
            if (evidencia.getEstado() == EstadoEvidencia.PENDIENTE) {
                throw new IllegalStateException("Ya tienes una evidencia pendiente de revisión" );
            }
        }
        String rutaFotografia =  servicioStorage.subirImagen(fotografia, "evidencias", "usuario_" + usuario.getId()); 
        try{

        Evidencia evidencia = new Evidencia(usuario, progresoReto, rutaFotografia);
        return repositorioEvidencia.save(evidencia);
        }catch(RuntimeException e){
            try{
                servicioStorage.eliminarImagen("evidencias", rutaFotografia);

            }catch (RuntimeException errorEliminacion){
                e.addSuppressed(errorEliminacion); 
            }
            throw e; 
        }
    }

    
}
