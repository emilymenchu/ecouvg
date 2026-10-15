package gt.edu.uvg.ecouvg.servicio;

import gt.edu.uvg.ecouvg.dto.CentroMapaDTO;
import gt.edu.uvg.ecouvg.modelo.CentroReciclaje;
import gt.edu.uvg.ecouvg.modelo.Residuo;
import gt.edu.uvg.ecouvg.repositorio.RepositorioCentro;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ServicioCentroMapaTest {
    RepositorioCentro repositorio;
    ServicioCentro servicio;
    CentroReciclaje pendiente;

    @BeforeEach void preparar() {
        repositorio = mock(RepositorioCentro.class);
        servicio = new ServicioCentro(repositorio);
        Residuo carton = new Residuo("Cartón", "Papel", "", "", true);
        Residuo vidrio = new Residuo("Vidrio", "Vidrio", "", "", true);
        CentroReciclaje cayala = new CentroReciclaje("Cayalá", "Zona 16", 14.6, -90.4,
            "Por confirmar", "", List.of(vidrio, carton, carton));
        pendiente = new CentroReciclaje("Pendiente", "Zona 15", null, -90.4,
            "Por confirmar", "", List.of(vidrio));
        when(repositorio.findAllByOrderByNombreAsc()).thenReturn(List.of(cayala, pendiente));
    }
    @Test void aplicaLosMismosFiltrosDeLaLista() {
        List<CentroMapaDTO> resultado = servicio.obtenerCentrosMapa(" CAYALA ", "carton");
        assertEquals(1, resultado.size());
        assertEquals("Cayalá", resultado.getFirst().getNombre());
        assertEquals(servicio.buscarCentros(" CAYALA ", "carton").size(), resultado.size());
        assertTrue(resultado.getFirst().isUbicable());
    }
    @Test void conservaCentrosSinCoordenadasSinModificarLaEntidad() {
        List<CentroMapaDTO> resultado = servicio.obtenerCentrosMapa("", "");
        assertEquals(2, resultado.size());
        assertFalse(resultado.get(1).isUbicable());
        assertNull(resultado.get(1).getLongitud());
        assertEquals(Double.valueOf(-90.4), pendiente.getLongitud());
        verify(repositorio, times(1)).findAllByOrderByNombreAsc();
    }
    @Test void noInventaResultadosYCombinaFiltros() {
        assertTrue(servicio.obtenerCentrosMapa("inexistente", "").isEmpty());
        assertTrue(servicio.obtenerCentrosMapa("zona 15", "Cartón").isEmpty());
    }
    @Test void entregaMaterialesOrdenadosYUnicos() {
        assertEquals(List.of("Cartón", "Vidrio"), servicio.obtenerCentrosMapa("cayala", "")
            .getFirst().getMateriales());
    }
    @Test void consultaIndividualNoCargaTodoElDirectorio() {
        when(repositorio.findById(9L)).thenReturn(java.util.Optional.of(pendiente));
        CentroMapaDTO resultado = servicio.obtenerCentroMapa(9L).orElseThrow();
        assertEquals("Pendiente", resultado.getNombre());
        assertFalse(resultado.isUbicable());
        assertEquals(List.of("Vidrio"), resultado.getMateriales());
        verify(repositorio).findById(9L);
        verify(repositorio, never()).findAllByOrderByNombreAsc();
    }
    @Test void consultaIndividualConservaAusencia() {
        when(repositorio.findById(99L)).thenReturn(java.util.Optional.empty());
        assertTrue(servicio.obtenerCentroMapa(99L).isEmpty());
    }
}
