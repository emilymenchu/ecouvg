package gt.edu.uvg.ecouvg.configuracion;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class RelojArcadeConfig {
    @Bean public Clock relojArcade() { return Clock.systemUTC(); }
}
