package gt.edu.uvg.ecouvg.repositorio;

import gt.edu.uvg.ecouvg.modelo.Usuario;

import java.util.Optional;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RepositorioUsuario extends JpaRepository<Usuario, Long>{
    Page<Usuario> findByNombreContainingIgnoreCaseOrCarnetContainingIgnoreCase(String nombre, String carnet, Pageable pagina);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from Usuario u where u.id = :id")
    Optional<Usuario> bloquearParaSeguimiento(@Param("id") Long id);

    Optional<Usuario> findByCarnet(String carnet);

    boolean existsByCarnet(String carnet);

    boolean existsByCorreo(String correo);

    Optional<Usuario> findByCorreo(String correo);
}