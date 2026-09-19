package gt.edu.uvg.ecouvg.repositorio;

import gt.edu.uvg.ecouvg.modelo.Usuario;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RepositorioUsuario extends JpaRepository<Usuario, Long>{
    Optional<Usuario> findByCarnet(String carnet);
}
