package gt.edu.uvg.ecouvg.servicio;

import gt.edu.uvg.ecouvg.modelo.*;
import gt.edu.uvg.ecouvg.repositorio.*;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ServicioSocial {
    private final RepositorioUsuario usuarios;
    private final RepositorioSeguimiento seguimientos;
    public record Resumen(long seguidores, long seguidos, long amigos, boolean siguiendo, boolean teSigue) {
        public boolean isAmistad() { return siguiendo && teSigue; }
    }
    public ServicioSocial(RepositorioUsuario usuarios, RepositorioSeguimiento seguimientos) {
        this.usuarios = usuarios;
        this.seguimientos = seguimientos;
    }
    @Transactional(readOnly = true)
    public Resumen resumen(Long perfilId, Long actualId) {
        return new Resumen(seguimientos.countBySeguidoId(perfilId), seguimientos.countBySeguidorId(perfilId),
            seguimientos.contarAmigos(perfilId), seguimientos.existsBySeguidorIdAndSeguidoId(actualId, perfilId),
            seguimientos.existsBySeguidorIdAndSeguidoId(perfilId, actualId));
    }
    @Transactional(readOnly = true)
    public Page<Usuario> buscar(String consulta, int pagina) {
        String texto = consulta == null ? "" : consulta.strip();
        if (texto.length() > 100) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La búsqueda admite hasta 100 caracteres.");
        Pageable limite = PageRequest.of(Math.max(0, pagina), 20, Sort.by("nombre", "id"));
        return texto.isEmpty() ? Page.empty(limite) : usuarios.findByNombreContainingIgnoreCaseOrCarnetContainingIgnoreCase(texto, texto, limite);
    }
    @Transactional(readOnly = true)
    public Page<Usuario> listar(Long perfilId, String lista, int pagina) {
        if (!usuarios.existsById(perfilId)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        Pageable limite = PageRequest.of(Math.max(0, pagina), 20);
        return switch (lista) {
            case "seguidores" -> seguimientos.seguidores(perfilId, limite);
            case "seguidos" -> seguimientos.seguidos(perfilId, limite);
            case "amigos" -> seguimientos.amigos(perfilId, limite);
            default -> throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        };
    }
    @Transactional
    public void seguir(Long actualId, Long perfilId) {
        ParUsuarios par = bloquearPar(actualId, perfilId);
        if (!seguimientos.existsBySeguidorIdAndSeguidoId(actualId, perfilId)) {
            seguimientos.save(new Seguimiento(par.actual(), par.destino()));
        }
    }
    @Transactional
    public void dejarDeSeguir(Long actualId, Long perfilId) {
        bloquearPar(actualId, perfilId);
        seguimientos.deleteBySeguidorIdAndSeguidoId(actualId, perfilId);
    }
    private record ParUsuarios(Usuario actual, Usuario destino) {}
    private ParUsuarios bloquearPar(Long actualId, Long perfilId) {
        if (actualId.equals(perfilId)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes seguirte a ti mismo.");
        // Un orden fijo evita interbloqueos cuando dos personas se siguen al mismo tiempo.
        Usuario primero = usuarios.bloquearParaSeguimiento(Math.min(actualId, perfilId))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Usuario segundo = usuarios.bloquearParaSeguimiento(Math.max(actualId, perfilId))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return actualId.equals(primero.getId()) ? new ParUsuarios(primero, segundo) : new ParUsuarios(segundo, primero);
    }

}
