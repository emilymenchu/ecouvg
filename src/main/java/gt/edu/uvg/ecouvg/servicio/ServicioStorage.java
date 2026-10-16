package gt.edu.uvg.ecouvg.servicio;


import tools.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ServicioStorage {

    private final RestClient cliente;
    private final ServicioImagen servicioImagen;
    private final String url;

    private static final Map<String, Integer> TAMANOS = Map.of(
            "evidencias", 1200, "perfiles", 400,
            "residuos", 800, "centros", 1000
    );

    private static final Set<String> PUBLICOS = Set.of("perfiles", "residuos", "centros");

    public ServicioStorage(@Value("${supabase.url}") String url,
                           @Value("${supabase.key}") String clave,
                           ServicioImagen servicioImagen) {
        this.url = url.replaceAll("/+$", "");
        this.servicioImagen = servicioImagen;
        this.cliente = RestClient.builder().baseUrl(this.url + "/storage/v1")
                .defaultHeader("apikey", clave).build();
    }

    private void validarBucket(String bucket) {
        if (!TAMANOS.containsKey(bucket)) throw new IllegalArgumentException("Bucket inválido");
    }

    private void validarRuta(String ruta) {
        if (ruta == null || !ruta.matches("[a-zA-Z0-9_-]+/[a-zA-Z0-9_-]+\\.jpg"))
            throw new IllegalArgumentException("Ruta inválida");
    }

    private String rutaObjeto(String bucket, String ruta) {
        validarBucket(bucket);
        validarRuta(ruta);
        return "/object/" + bucket + "/" + ruta;
    }

    public String subirImagen(MultipartFile archivo, String bucket, String carpeta) throws IOException {
        validarBucket(bucket);
        if (carpeta == null || !carpeta.matches("[a-zA-Z0-9_-]+"))
            throw new IllegalArgumentException("Carpeta inválida");

        byte[] imagen = servicioImagen.comprimir(archivo, TAMANOS.get(bucket));
        String ruta = carpeta + "/" + UUID.randomUUID() + ".jpg";

        cliente.post().uri(rutaObjeto(bucket, ruta))
                .contentType(MediaType.IMAGE_JPEG).body(imagen)
                .retrieve().toBodilessEntity();

        return ruta;
    }

    public byte[] obtenerImagen(String bucket, String ruta) {
        validarBucket(bucket);
        validarRuta(ruta);
        return cliente.get().uri("/object/authenticated/" + bucket + "/" + ruta)
                .retrieve().body(byte[].class);
    }

    public String actualizarImagen(MultipartFile archivo, String bucket, String carpeta) throws IOException {
        return subirImagen(archivo, bucket, carpeta);
    }

    public void eliminarImagen(String bucket, String ruta) {
        cliente.delete().uri(rutaObjeto(bucket, ruta)).retrieve().toBodilessEntity();
    }

    public String obtenerUrlPublica(String bucket, String ruta) {
        if (!PUBLICOS.contains(bucket)) throw new IllegalArgumentException("Bucket privado");
        validarRuta(ruta);
        return url + "/storage/v1/object/public/" + bucket + "/" + ruta;
    }

    public String obtenerUrlFirmada(String bucket, String ruta, int segundos) {
        validarBucket(bucket);
        validarRuta(ruta);
        if (segundos < 1 || segundos > 3600) throw new IllegalArgumentException("Duración inválida");

        JsonNode respuesta = cliente.post().uri("/object/sign/" + bucket + "/" + ruta)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("expiresIn", segundos))
                .retrieve().body(JsonNode.class);

        if (respuesta == null || !respuesta.hasNonNull("signedURL"))
            throw new IllegalStateException("No se pudo generar la URL");

        String enlace = respuesta.get("signedURL").asText();
        if (enlace.startsWith("https://") || enlace.startsWith("http://")) return enlace;
        if (enlace.startsWith("/storage/v1/")) return url + enlace;
        if (enlace.startsWith("/object/")) return url + "/storage/v1" + enlace;
        return url + "/storage/v1/" + enlace.replaceFirst("^/+", "");
    }
}