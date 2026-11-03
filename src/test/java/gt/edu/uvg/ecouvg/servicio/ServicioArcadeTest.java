package gt.edu.uvg.ecouvg.servicio;

import gt.edu.uvg.ecouvg.modelo.GuiaContenedor;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ServicioArcadeTest {
    private GuiaContenedor categoria(String slug, List<String> ejemplos) {
        return new GuiaContenedor(slug, "Nombre de BD " + slug, "verde", "!",
            "Descripción", ejemplos, "Preparación desde BD", "https://example.org");
    }
    @Test void consultaGuiaYConservaContratoSinDuplicarTextos() {
        var guia=mock(ServicioGuia.class);
        when(guia.buscar("")).thenReturn(List.of(
            categoria("carton",List.of("Caja")),categoria("papel",List.of("Hoja")),
            categoria("metales",List.of("Lata")),categoria("plasticos",List.of("Botella"))));
        var datos=new ServicioArcade(guia).obtenerMateriales();
        assertEquals(List.of("plasticos","metales","papel","carton"),datos.stream().map(m->m.slug()).toList());
        assertEquals("Nombre de BD plasticos",datos.getFirst().nombre());
        assertEquals(List.of("Botella"),datos.getFirst().ejemplos());
        verify(guia).buscar("");
    }
    @Test void fallaDeFormaExplicitaSiFaltaUnaCategoria() {
        var guia=mock(ServicioGuia.class);when(guia.buscar("")).thenReturn(List.of());
        var error=assertThrows(ResponseStatusException.class,()->new ServicioArcade(guia).obtenerMateriales());
        assertEquals(503,error.getStatusCode().value());
    }
    @Test void noIniciaConCategoriaSinEjemplos() {
        var guia=mock(ServicioGuia.class);
        when(guia.buscar("")).thenReturn(List.of(categoria("plasticos",List.of())));
        assertThrows(ResponseStatusException.class,()->new ServicioArcade(guia).obtenerMateriales());
    }
}
