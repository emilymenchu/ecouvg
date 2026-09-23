package gt.edu.uvg.ecouvg.servicio;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Locale;

/** Búsqueda literal: % y _ no son comodines; no se concatena SQL. */
public final class TextoBusqueda {
    private TextoBusqueda() {}
    public static String normalizar(String texto) {
        if (texto == null) return "";
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
            .replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT)
            .replaceAll("\\s+", " ").trim();
    }
    public static boolean coincide(String texto, String consulta) {
        String normalizado = normalizar(texto);
        return Arrays.stream(normalizar(consulta).split(" "))
            .allMatch(normalizado::contains);
    }
}
