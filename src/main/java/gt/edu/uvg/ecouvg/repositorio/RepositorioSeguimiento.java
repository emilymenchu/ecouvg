package gt.edu.uvg.ecouvg.repositorio;

import gt.edu.uvg.ecouvg.modelo.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface RepositorioSeguimiento extends JpaRepository<Seguimiento, Long> {
    boolean existsBySeguidorIdAndSeguidoId(Long seguidorId, Long seguidoId);
    long countBySeguidoId(Long id);
    long countBySeguidorId(Long id);
    void deleteBySeguidorIdAndSeguidoId(Long seguidorId, Long seguidoId);

    @Query("select s.seguidor from Seguimiento s where s.seguido.id = :id order by lower(s.seguidor.nombre), s.seguidor.id")
    Page<Usuario> seguidores(@Param("id") Long id, Pageable pagina);
    @Query("select s.seguido from Seguimiento s where s.seguidor.id = :id order by lower(s.seguido.nombre), s.seguido.id")
    Page<Usuario> seguidos(@Param("id") Long id, Pageable pagina);
    @Query("select s.seguido from Seguimiento s where s.seguidor.id = :id and exists " +
           "(select r.id from Seguimiento r where r.seguidor.id = s.seguido.id and r.seguido.id = :id) order by lower(s.seguido.nombre), s.seguido.id")
    Page<Usuario> amigos(@Param("id") Long id, Pageable pagina);
    @Query("select count(s) from Seguimiento s where s.seguidor.id = :id and exists " +
           "(select r.id from Seguimiento r where r.seguidor.id = s.seguido.id and r.seguido.id = :id)")
    long contarAmigos(@Param("id") Long id);
}
