package gt.edu.uvg.ecouvg.servicio;
import gt.edu.uvg.ecouvg.dto.*;
import gt.edu.uvg.ecouvg.modelo.*;
import gt.edu.uvg.ecouvg.repositorio.*;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ServicioMisionArcade {
    public static final int OBJETIVO=5;
    private static final ZoneId ZONA=ZoneId.of("America/Guatemala");
    private static final Map<String,Integer> MAXIMOS=Map.of("eco-run",20,"eco-snake",12,"eco-breakout",50);
    private final RepositorioUsuario usuarios;
    private final RepositorioMisionArcade misiones;
    private final Clock reloj;
    public ServicioMisionArcade(RepositorioUsuario usuarios,RepositorioMisionArcade misiones,Clock reloj) {
        this.usuarios=usuarios;this.misiones=misiones;this.reloj=reloj;
    }
    private LocalDate hoy() { return LocalDate.now(reloj.withZone(ZONA)); }
    private LocalDate semana(LocalDate fecha) { return fecha.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)); }
    private Usuario usuario(String carnet) {
        return usuarios.findByCarnet(carnet).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
    @Transactional(readOnly=true)
    public EstadoMisionArcade consultar(String carnet) {
        var u=usuario(carnet);var fecha=hoy();var semana=semana(fecha);
        return new EstadoMisionArcade(misiones.existsByUsuarioIdAndSemana(u.getId(),semana),OBJETIVO,u.obtenerRacha(fecha),semana);
    }
    @Transactional
    public EstadoMisionArcade completar(String carnet,SolicitudMisionArcade solicitud) {
        if(solicitud==null || solicitud.juego()==null || !MAXIMOS.containsKey(solicitud.juego())
            || solicitud.aciertos()==null || solicitud.aciertos()<OBJETIVO || solicitud.aciertos()>MAXIMOS.get(solicitud.juego())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Se requieren cinco aciertos de un juego válido");
        }
        // El carné procede de Authentication, nunca de la petición JSON.
        var u=usuario(carnet);
        u=usuarios.bloquearParaSeguimiento(u.getId()).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        var fecha=hoy();var semana=semana(fecha);
        if(!misiones.existsByUsuarioIdAndSemana(u.getId(),semana)) {
            misiones.save(new MisionArcade(u,semana,fecha,solicitud.juego()));
            u.registroActividadValida(fecha);
            usuarios.save(u);
        }
        return new EstadoMisionArcade(true,OBJETIVO,u.obtenerRacha(fecha),semana);
    }
}
