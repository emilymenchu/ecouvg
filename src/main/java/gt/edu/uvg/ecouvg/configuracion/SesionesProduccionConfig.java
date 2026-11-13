package gt.edu.uvg.ecouvg.configuracion;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

/** Sesiones compartidas; el esquema se prepara manualmente antes de publicar. */
@Configuration(proxyBeanMethods = false)
@Profile("prod")
@EnableJdbcHttpSession(tableName = "ecouvg_private.spring_session", maxInactiveIntervalInSeconds = 1800)
public class SesionesProduccionConfig {
    @Bean
    CookieSerializer cookieSerializer() {
        DefaultCookieSerializer cookie = new DefaultCookieSerializer();
        cookie.setCookieName("JSESSIONID");
        cookie.setCookiePath("/");
        cookie.setUseSecureCookie(true);
        cookie.setUseHttpOnlyCookie(true);
        cookie.setSameSite("Lax");
        return cookie;
    }
}
