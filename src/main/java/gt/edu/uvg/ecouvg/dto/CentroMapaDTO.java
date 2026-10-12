package gt.edu.uvg.ecouvg.dto;

import java.util.List;

/** Datos públicos del centro que necesita Leaflet, sin exponer entidades JPA. */
public final class CentroMapaDTO {
    private final Long id;
    private final String nombre;
    private final String ubicacion;
    private final Double latitud;
    private final Double longitud;
    private final String horario;
    private final List<String> materiales;

    public CentroMapaDTO(Long id, String nombre, String ubicacion,
            Double latitud, Double longitud, String horario, List<String> materiales) {
        this.id = id;
        this.nombre = nombre;
        this.ubicacion = ubicacion;
        // Se valida el par completo; una coordenada aislada no permite ubicar un centro.
        boolean validas = coordenadasValidas(latitud, longitud);
        this.latitud = validas ? latitud : null;
        this.longitud = validas ? longitud : null;
        this.horario = horario;
        this.materiales = materiales == null ? List.of() : List.copyOf(materiales);
    }

    private static boolean coordenadasValidas(Double latitud, Double longitud) {
        return latitud != null && longitud != null
            && Double.isFinite(latitud) && Double.isFinite(longitud)
            && latitud >= -90 && latitud <= 90
            && longitud >= -180 && longitud <= 180;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getUbicacion() { return ubicacion; }
    public Double getLatitud() { return latitud; }
    public Double getLongitud() { return longitud; }
    public String getHorario() { return horario; }
    public List<String> getMateriales() { return materiales; }
    public boolean isUbicable() { return latitud != null && longitud != null; }
}
