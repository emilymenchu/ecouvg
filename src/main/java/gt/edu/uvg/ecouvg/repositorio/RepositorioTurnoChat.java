package gt.edu.uvg.ecouvg.repositorio;

import gt.edu.uvg.ecouvg.modelo.TurnoChat;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepositorioTurnoChat extends JpaRepository<TurnoChat, Long> {
    List<TurnoChat> findByConversacionIdOrderByIdAsc(UUID conversacionId);
    List<TurnoChat> findTop10ByConversacionIdOrderByIdDesc(UUID conversacionId);
    Optional<TurnoChat> findByConversacionIdAndSolicitudId(UUID conversacionId, UUID solicitudId);
}
