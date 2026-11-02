package gt.edu.uvg.ecouvg.modelo;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UsuarioNivelTest {
    @Test void progresoRespetaLosUmbralesExistentes() {
        int[] puntos = {0, 99, 100, 249, 250, 499, 500, 999, 1000, 9999, 10000, 12000};
        int[] niveles = {1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6};
        int[] metas = {100, 100, 250, 250, 500, 500, 1000, 1000, 10000, 10000, 10000, 10000};
        for (int i = 0; i < puntos.length; i++) {
            Usuario usuario = new Usuario("Pilar", "123", "pilar@example.com", "clave");
            if (puntos[i] > 0) usuario.sumarPuntos(puntos[i]);
            assertEquals(niveles[i], usuario.getNivel());
            assertEquals(metas[i], usuario.getMetaNivel());
            assertTrue(usuario.getPorcentajeNivel() >= 0 && usuario.getPorcentajeNivel() <= 100);
            assertEquals(niveles[i] == 6, usuario.isNivelMaximo());
        }
    }

    @Test void barraMideElAvanceDentroDelNivelActual() {
        Usuario usuario = new Usuario("Pilar", "123", "pilar@example.com", "clave");
        usuario.sumarPuntos(750);
        assertEquals(4, usuario.getNivel());
        assertEquals("Granito verde", usuario.getNombreNivel());
        assertEquals(50, usuario.getPorcentajeNivel());
        usuario.sumarPuntos(9250);
        assertEquals(100, usuario.getPorcentajeNivel());
    }
}
