package gt.edu.uvg.ecouvg.servicio;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.security.crypto.password.PasswordEncoder;

import gt.edu.uvg.ecouvg.modelo.Usuario;
import gt.edu.uvg.ecouvg.repositorio.RepositorioUsuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ServicioPerfil {
    private static final Logger log = LoggerFactory.getLogger(ServicioPerfil.class);
    private final RepositorioUsuario usuarios;
    private final ServicioStorage storage;

    private final PasswordEncoder passwordEncoder;

    public ServicioPerfil(RepositorioUsuario usuarios, ServicioStorage storage, PasswordEncoder passwordEncoder) {
        this.usuarios = usuarios;
        this.storage = storage;
        this.passwordEncoder = passwordEncoder;
    }

    public byte[] obtenerFoto(String carnet) {
        Usuario usuario = buscarUsuario(carnet);
        String ruta = usuario.getFotoPerfil();
        return ruta == null || ruta.isBlank() ? null : storage.obtenerImagen("perfiles", ruta);
    }

    @Transactional
    public void cambiarContrasena(String carnet, String actual, String nueva, String confirmacion) {
        Usuario usuario = buscarUsuario(carnet);
        if (actual == null || !passwordEncoder.matches(actual, usuario.getContrasena())) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta.");
        }
        if (nueva == null || nueva.isBlank() || nueva.length() < 8 || nueva.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("Usa al menos 8 caracteres y un máximo de 72 bytes para la nueva contraseña.");
        }
        if (!nueva.equals(confirmacion)) {
            throw new IllegalArgumentException("Las contraseñas nuevas no coinciden.");
        }
        usuario.setContrasena(passwordEncoder.encode(nueva));
        usuarios.saveAndFlush(usuario);
    }

    private Usuario buscarUsuario(String carnet) {
        return usuarios.findByCarnet(carnet)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
    }

    @Transactional(rollbackFor = IOException.class)
    public void actualizarFoto(String carnet, MultipartFile fotografia) throws IOException {
        if (fotografia == null || fotografia.isEmpty()) {
            throw new IllegalArgumentException("Selecciona una fotografía.");
        }
        // La cuenta se obtiene de la sesión, nunca de un ID enviado por el formulario.
        Usuario usuario = buscarUsuario(carnet);
        String anterior = usuario.getFotoPerfil();
        String nueva = storage.subirImagen(fotografia, "perfiles", "usuario_" + usuario.getId());

        // Storage y la base de datos no comparten una transacción.
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                if (anterior != null && !anterior.isBlank() && !anterior.equals(nueva)) {
                    eliminarSinInterrumpir(anterior);
                }
            }

            @Override
            public void afterCompletion(int estado) {
                if (estado == STATUS_ROLLED_BACK) {
                    eliminarSinInterrumpir(nueva);
                }
            }
        });
        usuario.setFotoPerfil(nueva);
        usuarios.saveAndFlush(usuario);
    }

    private void eliminarSinInterrumpir(String ruta) {
        try {
            storage.eliminarImagen("perfiles", ruta);
        } catch (RuntimeException e) {
            log.warn("No se pudo eliminar una foto de perfil reemplazada o descartada", e);
        }
    }
}
