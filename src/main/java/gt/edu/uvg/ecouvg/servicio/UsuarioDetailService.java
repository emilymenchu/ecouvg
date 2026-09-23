package gt.edu.uvg.ecouvg.servicio;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import gt.edu.uvg.ecouvg.modelo.Usuario;
import gt.edu.uvg.ecouvg.repositorio.RepositorioUsuario;

@Service 
public class UsuarioDetailService implements UserDetailsService {
    private final RepositorioUsuario repositorioUsuario;

    public UsuarioDetailService(RepositorioUsuario repositorioUsuario) {
        this.repositorioUsuario = repositorioUsuario;
    }

    @Override
    public UserDetails loadUserByUsername(String carnet) throws UsernameNotFoundException {
        
        Usuario usuario = repositorioUsuario.findByCarnet(carnet).orElseThrow(() -> new UsernameNotFoundException("Credenciales incorrectas"));

        return User.builder()
                   .username(usuario.getCarnet())
                   .password(usuario.getContrasena())
                   .roles("USUARIO")
                   .build();
    }
}
