package gt.edu.uvg.ecouvg.servicio;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ServicioGuiaTest {
    private final ServicioGuia servicio = new ServicioGuia(gt.edu.uvg.ecouvg.GuiaPrueba.repositorio());
    @Test void muestraOchoCategoriasSinFiltro() {
        assertEquals(8, servicio.buscar("").size());
        assertEquals(8, servicio.buscar(null).size());
    }
    @Test void ignoraTildesMayusculasYEspacios() {
        var resultado = servicio.buscar("  CARTON  ");
        assertTrue(resultado.stream().anyMatch(c -> c.slug().equals("carton")));
        assertEquals("plasticos", servicio.buscar("PLASTICOS").getFirst().slug());
    }
    @Test void buscaEjemplosYCombinaPalabras() {
        assertEquals("vidrio", servicio.buscar("botellas vidrio").getFirst().slug());
        assertEquals("especiales", servicio.buscar("pilas").getFirst().slug());
        assertEquals("organicos", servicio.buscar("cafe").getFirst().slug());
    }
    @Test void noInterpretaComodinesNiHtml() {
        assertTrue(servicio.buscar("%").isEmpty());
        assertTrue(servicio.buscar("<script>").isEmpty());
        assertTrue(servicio.buscar("zzzzinexistente").isEmpty());
    }
    @Test void detalleConservaCategoriaYRechazaDesconocidos() {
        assertEquals("Vidrio", servicio.obtener("vidrio").orElseThrow().nombre());
        assertTrue(servicio.obtener("inventado").isEmpty());
    }
    @Test void ejemplosSonInmutables() {
        var categoria = servicio.obtener("papel").orElseThrow();
        assertThrows(UnsupportedOperationException.class, () -> categoria.ejemplos().add("otro"));
    }
    @Test void baseVaciaNoUsaCatalogoDeRespaldo() {
        var repo = org.mockito.Mockito.mock(gt.edu.uvg.ecouvg.repositorio.RepositorioGuia.class);
        org.mockito.Mockito.when(repo.findAllByOrderByOrdenAscSlugAsc()).thenReturn(java.util.List.of());
        assertTrue(new ServicioGuia(repo).buscar("").isEmpty());
        org.mockito.Mockito.verify(repo).findAllByOrderByOrdenAscSlugAsc();
    }
}
