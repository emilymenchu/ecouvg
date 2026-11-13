package gt.edu.uvg.ecouvg.configuracion;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(auth ->
                auth.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                .requestMatchers(
                        "/login",
                        "/registro",
                        "/recuperar",
                        "/verificar-codigo",
                        "/nueva-contrasena",
                        "/favicon.ico",
                        "/css/**",
                        "/js/**",
                        "/img/**"
                ).permitAll()
                .anyRequest().authenticated()
        )
        .formLogin(form ->
                form.loginPage("/login")
                    .loginProcessingUrl("/login")
                    .usernameParameter("carnet")
                    .passwordParameter("contrasena")
                    .defaultSuccessUrl("/inicio", true)
                    .failureUrl("/login?error=true")
                    .permitAll()
        )
        .logout(logout ->
                logout.logoutUrl("/logout")
                    .logoutSuccessUrl("/login?logout=true")
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID")
                    .permitAll()
        )
        .sessionManagement(session ->
                session.sessionFixation(fixation ->
                        fixation.migrateSession()
                )
        );

        return http.build();
    }
}
