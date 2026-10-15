package civicconnect.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * NFR003 / ASR-01 / RISK-003 / RISK-004.
 * Login is session based (AuthService). Every API route except health and login needs a signed-in user.
 * Role rules are checked here per route, and again per record in the services (FilterStrategy, guard).
 */
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                // Not signed in -> 401. Signed in but wrong role -> 403 (Spring default).
                .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/health").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        // Frontend shell packaged into the jar for staging (same origin as API).
                        .requestMatchers("/", "/index.html", "/assets/**", "/favicon.ico").permitAll()
                        // FR012: summary counts are for Management only
                        .requestMatchers(HttpMethod.GET, "/api/v1/requests/summary").hasRole("Management")
                        // FR001: only requesters submit requests
                        .requestMatchers(HttpMethod.POST, "/api/v1/requests").hasRole("Requester")
                        // FR005 list and single request: any signed-in role; the FilterStrategy limits the rows
                        .requestMatchers(HttpMethod.GET, "/api/v1/requests", "/api/v1/requests/*").authenticated()
                        .requestMatchers("/api/v1/**").authenticated()
                        // SPA client routes (React Router) when packaged into the jar
                        .anyRequest().permitAll()
                )
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable);
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(
            @Value("${CORS_ALLOWED_ORIGINS:http://localhost:5173,http://127.0.0.1:5173}") String allowedOrigins
    ) {
        CorsConfiguration config = new CorsConfiguration();
        // Staging sets CORS_ALLOWED_ORIGINS in its environment; nothing is hard-coded per environment.
        config.setAllowedOrigins(Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Content-Type", "Authorization", "X-Requested-With"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
