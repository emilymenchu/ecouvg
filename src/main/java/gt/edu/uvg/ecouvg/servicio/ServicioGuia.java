package gt.edu.uvg.ecouvg.servicio;

import gt.edu.uvg.ecouvg.modelo.GuiaContenedor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ServicioGuia {
    private static final String EPA = "https://espanol.epa.gov/espanol/como-reciclar-los-materiales-reciclables-comunes";
    // Catálogo educativo versionado: no escribe ni depende de tablas de Supabase.
    private final List<GuiaContenedor> categorias = List.of(
        new GuiaContenedor("organicos", "Orgánicos", "verde", "♧",
            "Restos vegetales para compostaje, cuando exista un programa que los reciba.",
            List.of("Cáscaras de frutas", "Restos de verduras", "Hojas secas", "Posos de café"),
            "Sepáralos de los envases. Para compostaje doméstico evita carnes, lácteos y aceites.",
            "https://www.epa.gov/recycle/composting-home"),
        new GuiaContenedor("carton", "Cartón", "cafe", "▱",
            "Cajas y empaques de cartón.", List.of("Cajas de cereal", "Cajas de zapatos"),
            "Retira restos de comida y aplana las cajas.", EPA),
        new GuiaContenedor("plasticos", "Plásticos", "azul", "♳",
            "Envases aceptados por el programa local.", List.of("Botellas de agua", "Envases plásticos"),
            "Vacía y limpia. El símbolo del plástico no garantiza su aceptación.", EPA),
        new GuiaContenedor("metales", "Metales", "gris", "◎",
            "Envases metálicos recuperables.", List.of("Latas de bebidas", "Latas de conservas"),
            "Vacía y retira restos de comida. Confirma qué metales reciben.", EPA),
        new GuiaContenedor("papel", "Papel", "amarillo", "▤",
            "Productos de papel para recuperación.", List.of("Periódicos", "Revistas", "Hojas de papel"),
            "Consérvalos limpios y secos. Consulta antes de llevar papel laminado.", EPA),
        new GuiaContenedor("multicapa", "Envases multicapa", "naranja", "▧",
            "Envases de bebidas que combinan distintos materiales.", List.of("Cartones de leche", "Cartones de jugo"),
            "Consulta al centro si acepta estos envases y cómo entregarlos. No asumas que van con el cartón.",
            "https://www.epa.gov/recycle/frequent-questions-recycling"),
        new GuiaContenedor("vidrio", "Vidrio", "celeste", "♙",
            "Envases de vidrio de alimentos y bebidas.", List.of("Botellas de vidrio", "Frascos de alimentos"),
            "Retira tapas metálicas. No deposites vidrio roto en el contenedor común.", EPA),
        new GuiaContenedor("especiales", "Manejo especial", "negro", "!",
            "Residuos que requieren una recepción específica.", List.of("Pilas y baterías", "Aparatos electrónicos"),
            "No los mezcles con reciclables comunes. Consulta un punto especializado antes de llevarlos.", EPA)
    );

    public List<GuiaContenedor> buscar(String q) {
        return categorias.stream().filter(c -> TextoBusqueda.coincide(
            c.nombre() + " " + c.descripcion() + " " + String.join(" ", c.ejemplos()), q)).toList();
    }
    public Optional<GuiaContenedor> obtener(String slug) {
        return categorias.stream().filter(c -> c.slug().equals(slug)).findFirst();
    }
}
