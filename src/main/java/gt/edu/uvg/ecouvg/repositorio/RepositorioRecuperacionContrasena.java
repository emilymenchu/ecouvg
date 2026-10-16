package gt.edu.uvg.ecouvg.repositorio;

import gt.edu.uvg.ecouvg.modelo.RecuperacionContrasena;
import gt.edu.uvg.ecouvg.modelo.Usuario;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RepositorioRecuperacionContrasena
        extends JpaRepository<RecuperacionContrasena, Long> {

    Optional<RecuperacionContrasena>
        findTopByUsuarioOrderByIdDesc(Usuario usuario);
}