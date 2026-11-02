package gt.edu.uvg.ecouvg;

import gt.edu.uvg.ecouvg.modelo.CategoriaGuia;
import gt.edu.uvg.ecouvg.repositorio.RepositorioGuia;
import java.util.List;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.anyString;

/** Datos exclusivamente para pruebas; producción siempre consulta PostgreSQL. */
public final class GuiaPrueba {
    private GuiaPrueba() { }
    public static RepositorioGuia repositorio() {
        var categorias = List.of(
            new CategoriaGuia("organicos", "Orgánicos", "verde", "♧", "Restos vegetales para compostaje, cuando exista un programa que los reciba.", List.of("Cáscaras de frutas", "Restos de verduras", "Hojas secas", "Posos de café"), "Sepáralos de los envases. Para compostaje doméstico evita carnes, lácteos y aceites.", "https://www.epa.gov/recycle/composting-home", 0),
            new CategoriaGuia("carton", "Cartón", "cafe", "▱", "Cajas y empaques de cartón.", List.of("Cajas de cereal", "Cajas de zapatos"), "Retira restos de comida y aplana las cajas.", "https://espanol.epa.gov/espanol/como-reciclar-los-materiales-reciclables-comunes", 1),
            new CategoriaGuia("plasticos", "Plásticos", "azul", "♳", "Envases aceptados por el programa local.", List.of("Botellas de agua", "Envases plásticos"), "Vacía y limpia. El símbolo del plástico no garantiza su aceptación.", "https://espanol.epa.gov/espanol/como-reciclar-los-materiales-reciclables-comunes", 2),
            new CategoriaGuia("metales", "Metales", "gris", "◎", "Envases metálicos recuperables.", List.of("Latas de bebidas", "Latas de conservas"), "Vacía y retira restos de comida. Confirma qué metales reciben.", "https://espanol.epa.gov/espanol/como-reciclar-los-materiales-reciclables-comunes", 3),
            new CategoriaGuia("papel", "Papel", "amarillo", "▤", "Productos de papel para recuperación.", List.of("Periódicos", "Revistas", "Hojas de papel"), "Consérvalos limpios y secos. Consulta antes de llevar papel laminado.", "https://espanol.epa.gov/espanol/como-reciclar-los-materiales-reciclables-comunes", 4),
            new CategoriaGuia("multicapa", "Envases multicapa", "naranja", "▧", "Envases de bebidas que combinan distintos materiales.", List.of("Cartones de leche", "Cartones de jugo"), "Consulta al centro si acepta estos envases y cómo entregarlos. No asumas que van con el cartón.", "https://www.epa.gov/recycle/frequent-questions-recycling", 5),
            new CategoriaGuia("vidrio", "Vidrio", "celeste", "♙", "Envases de vidrio de alimentos y bebidas.", List.of("Botellas de vidrio", "Frascos de alimentos"), "Retira tapas metálicas. No deposites vidrio roto en el contenedor común.", "https://espanol.epa.gov/espanol/como-reciclar-los-materiales-reciclables-comunes", 6),
            new CategoriaGuia("especiales", "Manejo especial", "negro", "!", "Residuos que requieren una recepción específica.", List.of("Pilas y baterías", "Aparatos electrónicos"), "No los mezcles con reciclables comunes. Consulta un punto especializado antes de llevarlos.", "https://espanol.epa.gov/espanol/como-reciclar-los-materiales-reciclables-comunes", 7)
        );
        var repositorio = mock(RepositorioGuia.class);
        when(repositorio.findAllByOrderByOrdenAscSlugAsc()).thenReturn(categorias);
        when(repositorio.findBySlug(anyString())).thenAnswer(invocacion ->
            categorias.stream().filter(c -> c.getSlug().equals(invocacion.getArgument(0))).findFirst());
        return repositorio;
    }
}
