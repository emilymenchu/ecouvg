package gt.edu.uvg.ecouvg;

import gt.edu.uvg.ecouvg.modelo.Usuario;
import gt.edu.uvg.ecouvg.repositorio.RepositorioUsuario;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.session.jdbc.JdbcIndexedSessionRepository;
import org.springframework.session.Session;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.*;

/** HTTP real + dos repositorios independientes sobre H2; no usa servicios remotos. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "spring.datasource.url=jdbc:h2:mem:sesiones;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
    "spring.sql.init.mode=always",
    "spring.sql.init.schema-locations=file:docs/deploy/01-sesiones.sql"
})
@ActiveProfiles({"prod", "test"})
class SesionesProduccionTest {
    @LocalServerPort int puerto;
    @Autowired RepositorioUsuario usuarios;
    @Autowired PasswordEncoder encoder;
    @Autowired JdbcTemplate jdbc;
    @Autowired PlatformTransactionManager transacciones;
    private final HttpClient cliente = HttpClient.newHttpClient();


    @Test void cacheaSoloRecursosPublicosYConservaPrivadasLasPaginasDelUsuario() throws Exception {
        for (String recurso : new String[]{"/css/inicio.css", "/js/perfil.js", "/img/Eco%20nadando.png"}) {
            var publico = enviar("GET", recurso, null, null);
            assertEquals(200, publico.statusCode(), recurso);
            String cache = publico.headers().firstValue("Cache-Control").orElse("");
            assertTrue(cache.contains("max-age=3600"), recurso);
            assertTrue(cache.contains("public"), recurso);
            assertFalse(cache.contains("no-store"), recurso);
        }
        usuarios.save(new Usuario("Prueba caché", "cache-test", "cache@example.com", encoder.encode("clave-prueba")));
        var login = enviar("GET", "/login", null, null);
        assertEquals(200, login.statusCode());
        assertTrue(login.headers().firstValue("Cache-Control").orElse("").contains("no-store"));
        var csrf = Pattern.compile("name=\"_csrf\"[^>]*value=\"([^\"]+)\"").matcher(login.body());
        assertTrue(csrf.find());
        var autenticado = enviar("POST", "/login", cookie(login),
            "carnet=cache-test&contrasena=clave-prueba&_csrf=" + URLEncoder.encode(csrf.group(1), StandardCharsets.UTF_8));
        assertEquals(302, autenticado.statusCode());
        String cookie = cookie(autenticado);
        for (String ruta : new String[]{"/perfil", "/chat", "/perfil/foto"}) {
            var privada = enviar("GET", ruta, cookie, null);
            assertEquals(ruta.equals("/perfil/foto") ? 404 : 200, privada.statusCode(), ruta);
            String cache = privada.headers().firstValue("Cache-Control").orElse("");
            assertTrue(cache.contains("no-store"), ruta);
            assertFalse(cache.contains("public"), ruta);
        }
    }

    @Test void conservaAutenticacionEnJdbcYRevocaElAccesoAlBorrarLaSesionDesdeOtroRepositorio() throws Exception {
        var entradaAnonima = enviar("GET", "/", null, null);
        assertEquals(302, entradaAnonima.statusCode());
        assertTrue(entradaAnonima.headers().firstValue("Location").orElseThrow().endsWith("/login"));
        usuarios.save(new Usuario("Prueba", "sesion-test", "sesion@example.com", encoder.encode("clave-prueba")));
        var login = enviar("GET", "/login", null, null);
        assertEquals(200, login.statusCode());
        assertTrue(login.headers().firstValue("Cache-Control").orElse("").contains("no-store"));
        String cookie = cookie(login);
        assertTrue(login.headers().firstValue("Set-Cookie").orElseThrow().contains("Secure"));
        assertTrue(login.headers().firstValue("Set-Cookie").orElseThrow().contains("HttpOnly"));
        assertTrue(login.headers().firstValue("Set-Cookie").orElseThrow().contains("SameSite=Lax"));
        var csrf = Pattern.compile("name=\"_csrf\"[^>]*value=\"([^\"]+)\"").matcher(login.body());
        assertTrue(csrf.find(), "El formulario debe incluir el token CSRF");
        var autenticado = enviar("POST", "/login", cookie,
            "carnet=sesion-test&contrasena=clave-prueba&_csrf=" + URLEncoder.encode(csrf.group(1), StandardCharsets.UTF_8));
        assertEquals(302, autenticado.statusCode());
        assertTrue(autenticado.headers().firstValue("Location").orElseThrow().endsWith("/inicio"));
        cookie = cookie(autenticado);
        var entradaAutenticada = enviar("GET", "/", cookie, null);
        assertEquals(302, entradaAutenticada.statusCode());
        assertTrue(entradaAutenticada.headers().firstValue("Location").orElseThrow().endsWith("/inicio"));

        var paginaInexistente = cliente.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + puerto + "/pagina-inexistente"))
            .header("Cookie", cookie).header("Accept", "text/html").GET().build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(404, paginaInexistente.statusCode());
        assertTrue(paginaInexistente.body().contains("Esta página no existe"));
        assertTrue(paginaInexistente.body().contains("Volver a EcoUVG"));
        assertFalse(paginaInexistente.body().contains("Whitelabel"));
        assertEquals(200, enviar("GET", "/css/error.css", null, null).statusCode());

        var otroRepositorio = new JdbcIndexedSessionRepository(jdbc, new TransactionTemplate(transacciones));
        otroRepositorio.setTableName("ecouvg_private.spring_session");
        String id = new String(Base64.getDecoder().decode(cookie.substring("JSESSIONID=".length())), StandardCharsets.UTF_8);
        Session sesion = otroRepositorio.findById(id);
        assertNotNull(sesion);
        SecurityContext contexto = sesion.getAttribute("SPRING_SECURITY_CONTEXT");
        assertEquals("sesion-test", contexto.getAuthentication().getName());
        // Los recursos fallidos no deben redirigir al login ni reemplazar la sesión.
        assertEquals(200, enviar("GET", "/img/Eco%20nadando.png", cookie, null).statusCode());
        for (String ruta : new String[]{"/img/imagen-inexistente.png", "/favicon.ico", "/pagina-inexistente"}) {
            var error = enviar("GET", ruta, cookie, null);
            assertEquals(404, error.statusCode(), ruta);
            assertTrue(error.headers().allValues("Set-Cookie").stream()
                .noneMatch(valor -> valor.startsWith("JSESSIONID=")), "Un recurso faltante no debe reemplazar la cookie");
        }
        assertEquals(200, enviar("GET", "/evidencias/enviar/1", cookie, null).statusCode());

        var formulario = enviar("GET", "/evidencias/enviar/1", cookie, null);
        var token = Pattern.compile("name=\"_csrf\"[^>]*value=\"([^\"]+)\"").matcher(formulario.body());
        assertTrue(token.find());
        for (String ruta : new String[]{"/perfil/foto", "/evidencias/enviar"}) {
            String prefijo = "--limite\r\nContent-Disposition: form-data; name=\"fotografia\"; filename=\"grande.jpg\"\r\nContent-Type: image/jpeg\r\n\r\n";
            String cuerpo = prefijo + "a".repeat(4 * 1024 * 1024 + 1) + "\r\n--limite--\r\n";
            var solicitud = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + puerto + ruta))
                .header("Cookie", cookie).header("X-CSRF-TOKEN", token.group(1))
                .header("Content-Type", "multipart/form-data; boundary=limite")
                .POST(HttpRequest.BodyPublishers.ofString(cuerpo)).build();
            var rechazo = cliente.send(solicitud, HttpResponse.BodyHandlers.ofString());
            assertEquals(302, rechazo.statusCode(), "El exceso de tamaño debe volver al formulario: " + ruta);
            assertTrue(rechazo.headers().firstValue("Location").orElseThrow()
                .endsWith(ruta.equals("/perfil/foto") ? "/perfil" : "/retos"));
        }

        // Una sesión invalidada fuera de esta instancia ya no permite acceso.
        String primaryId = jdbc.queryForObject("SELECT primary_id FROM ecouvg_private.spring_session WHERE session_id = ?", String.class, id);
        otroRepositorio.deleteById(id);
        var revocado = enviar("GET", "/evidencias/enviar/1", cookie, null);
        assertEquals(302, revocado.statusCode());
        assertTrue(revocado.headers().firstValue("Location").orElseThrow().endsWith("/login"));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM ecouvg_private.spring_session_attributes WHERE session_primary_id = ?",
            Integer.class, primaryId));
    }

    private String cookie(HttpResponse<String> respuesta) {
        return respuesta.headers().allValues("Set-Cookie").stream()
            .filter(valor -> valor.startsWith("JSESSIONID=")).findFirst().orElseThrow().split(";", 2)[0];
    }

    private HttpResponse<String> enviar(String metodo, String ruta, String cookie, String cuerpo) throws Exception {
        var solicitud = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + puerto + ruta));
        if (cookie != null) solicitud.header("Cookie", cookie);
        if (cuerpo == null) solicitud.GET();
        else solicitud.header("Content-Type", "application/x-www-form-urlencoded")
            .method(metodo, HttpRequest.BodyPublishers.ofString(cuerpo));
        return cliente.send(solicitud.build(), HttpResponse.BodyHandlers.ofString());
    }
}
