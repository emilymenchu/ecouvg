package gt.edu.uvg.ecouvg.dto;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CentroMapaDTOTest {
    private CentroMapaDTO crear(Double latitud, Double longitud) {
        return new CentroMapaDTO(1L, "Centro", "Zona 15", latitud, longitud,
            "Por confirmar", List.of("Cartón"));
    }

    @Test void aceptaCoordenadasDeGuatemala() {
        CentroMapaDTO dto = crear(14.5861349, -90.4879555);
        assertTrue(dto.isUbicable());
        assertEquals(Double.valueOf(14.5861349), dto.getLatitud());
        assertEquals(Double.valueOf(-90.4879555), dto.getLongitud());
    }
    @Test void aceptaLimitesGeograficosYElCeroReal() {
        assertTrue(crear(90.0, 180.0).isUbicable());
        assertTrue(crear(-90.0, -180.0).isUbicable());
        assertTrue(crear(0.0, 0.0).isUbicable());
    }
    @Test void anulaElParSiFaltaUnaCoordenada() {
        for (CentroMapaDTO dto : List.of(crear(null, -90.4), crear(14.6, null), crear(null, null))) {
            assertFalse(dto.isUbicable());
            assertNull(dto.getLatitud());
            assertNull(dto.getLongitud());
        }
    }
    @Test void rechazaFueraDeRangoYValoresNoFinitos() {
        for (double valor : new double[]{90.001, -90.001, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
            assertFalse(crear(valor, -90.4).isUbicable());
        }
        for (double valor : new double[]{180.001, -180.001, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
            CentroMapaDTO dto = crear(14.6, valor);
            assertFalse(dto.isUbicable());
            assertNull(dto.getLatitud());
            assertNull(dto.getLongitud());
        }
    }
    @Test void copiaLosMaterialesSinPermitirModificaciones() {
        List<String> materiales = new ArrayList<>(List.of("Cartón"));
        CentroMapaDTO dto = new CentroMapaDTO(1L, "Centro", "Zona 15", 14.6, -90.4, "", materiales);
        materiales.clear();
        assertEquals(List.of("Cartón"), dto.getMateriales());
        assertThrows(UnsupportedOperationException.class, () -> dto.getMateriales().add("Vidrio"));
    }
    @Test void admiteListaAusente() {
        assertTrue(new CentroMapaDTO(1L, "Centro", "Zona 15", null, null, null, null)
            .getMateriales().isEmpty());
    }
}
