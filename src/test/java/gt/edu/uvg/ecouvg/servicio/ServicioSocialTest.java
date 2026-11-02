package gt.edu.uvg.ecouvg.servicio;

import gt.edu.uvg.ecouvg.modelo.*;
import gt.edu.uvg.ecouvg.repositorio.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(showSql = false, properties = {
    "spring.datasource.url=jdbc:h2:mem:social_test", "spring.datasource.username=sa", "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop", "spring.jpa.show-sql=false"
})
@Import(ServicioSocial.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class ServicioSocialTest {
    @Autowired ServicioSocial social;
    @Autowired RepositorioUsuario usuarios;
    @Autowired RepositorioSeguimiento seguimientos;
    private Usuario pilar;
    private Usuario emily;

    @BeforeEach void preparar() {
        seguimientos.deleteAll();
        usuarios.deleteAll();
        pilar = usuarios.save(new Usuario("Pilar Ordóñez", "12345", "pilar@example.com", "clave"));
        emily = usuarios.save(new Usuario("Emily Menchú", "45678", "emily@example.com", "clave"));
    }

    @Test void seguimientoSeGuardaYCuentaEnAmbosPerfiles() {
        social.seguir(pilar.getId(), emily.getId());
        assertEquals(1, social.resumen(pilar.getId(), pilar.getId()).seguidos());
        var resumen = social.resumen(emily.getId(), pilar.getId());
        assertEquals(1, resumen.seguidores());
        assertTrue(resumen.siguiendo());
        assertFalse(resumen.isAmistad());
        assertEquals(pilar.getId(), social.listar(emily.getId(), "seguidores", 0).getContent().getFirst().getId());
        assertEquals(emily.getId(), social.listar(pilar.getId(), "seguidos", 0).getContent().getFirst().getId());
    }

    @Test void seguimientoMutuoCreaAmistadYDejarDeSeguirLaDeshace() {
        social.seguir(pilar.getId(), emily.getId());
        social.seguir(emily.getId(), pilar.getId());
        assertTrue(social.resumen(emily.getId(), pilar.getId()).isAmistad());
        assertEquals(1, social.resumen(pilar.getId(), pilar.getId()).amigos());
        assertEquals(emily.getId(), social.listar(pilar.getId(), "amigos", 0).getContent().getFirst().getId());
        social.dejarDeSeguir(pilar.getId(), emily.getId());
        var resumen = social.resumen(emily.getId(), pilar.getId());
        assertFalse(resumen.isAmistad());
        assertEquals(0, resumen.amigos());
        assertTrue(resumen.teSigue());
        assertEquals(1, seguimientos.count());
    }

    @Test void accionesRepetidasNoDuplicanSeguimientos() {
        social.seguir(pilar.getId(), emily.getId());
        social.seguir(pilar.getId(), emily.getId());
        assertEquals(1, seguimientos.count());
        social.dejarDeSeguir(pilar.getId(), emily.getId());
        social.dejarDeSeguir(pilar.getId(), emily.getId());
        assertEquals(0, seguimientos.count());
    }

    @Test void noPermiteSeguirseNiSeguirUsuariosInexistentes() {
        assertEquals(400, assertThrows(ResponseStatusException.class, () -> social.seguir(pilar.getId(), pilar.getId())).getStatusCode().value());
        assertEquals(404, assertThrows(ResponseStatusException.class, () -> social.seguir(pilar.getId(), 999999L)).getStatusCode().value());
        assertEquals(0, seguimientos.count());
    }

    @Test void buscaPorNombreOCarneSinDistinguirMayusculasYEscapaComodines() {
        assertEquals(pilar.getId(), social.buscar("pILaR", 0).getContent().getFirst().getId());
        assertEquals(emily.getId(), social.buscar("567", 0).getContent().getFirst().getId());
        assertTrue(social.buscar("inexistente", 0).isEmpty());
        assertTrue(social.buscar("%", 0).isEmpty());
        assertTrue(social.buscar("_", 0).isEmpty());
        assertTrue(social.buscar("", 0).isEmpty());
        assertThrows(ResponseStatusException.class, () -> social.buscar("x".repeat(101), 0));
    }

    @Test void busquedaTienePaginacion() {
        for (int i = 0; i < 21; i++) usuarios.save(new Usuario("Persona " + i, "p" + i, "p" + i + "@example.com", "clave"));
        assertEquals(20, social.buscar("Persona", 0).getNumberOfElements());
        assertEquals(1, social.buscar("Persona", 1).getNumberOfElements());
    }

    @Test void seguirMutuamenteAlMismoTiempoNoCreaDuplicadosNiInterbloqueos() throws Exception {
        try (var ejecutor = Executors.newFixedThreadPool(2)) {
            var uno = ejecutor.submit(() -> social.seguir(pilar.getId(), emily.getId()));
            var dos = ejecutor.submit(() -> social.seguir(emily.getId(), pilar.getId()));
            uno.get(10, TimeUnit.SECONDS);
            dos.get(10, TimeUnit.SECONDS);
        }
        assertEquals(2, seguimientos.count());
        assertTrue(social.resumen(emily.getId(), pilar.getId()).isAmistad());
    }
}
