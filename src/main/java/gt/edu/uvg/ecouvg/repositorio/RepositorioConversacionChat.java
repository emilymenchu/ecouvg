package gt.edu.uvg.ecouvg.repositorio;

import gt.edu.uvg.ecouvg.modelo.ConversacionChat;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface RepositorioConversacionChat extends JpaRepository<ConversacionChat, UUID> {
    List<ConversacionChat> findByUsuarioIdOrderByActualizadaDesc(Long usuarioId);
    Optional<ConversacionChat> findByIdAndUsuarioId(UUID id, Long usuarioId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from ConversacionChat c where c.id = :id and c.usuario.id = :usuarioId")
    Optional<ConversacionChat> bloquearPropia(@Param("id") UUID id, @Param("usuarioId") Long usuarioId);
}
