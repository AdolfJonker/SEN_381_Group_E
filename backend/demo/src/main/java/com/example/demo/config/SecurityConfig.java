package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
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
 * Replaces the earlier permitAll configuration.
 * Every /api route now needs an authenticated user, and role rules are
 * enforced here (coarse, per route) and again in the service layer (per record).
 */
@Configuration
public class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				// Stateless API: credentials travel in the Authorization header, not a cookie,
				// so there is no session for a CSRF attack to ride on.
				.csrf(AbstractHttpConfigurer::disable)
				.cors(Customizer.withDefaults())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				// Return a plain 401 (no WWW-Authenticate header) so the browser
				// does not open its own login pop-up over the React login screen.
				.httpBasic(basic -> basic.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
				.exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						// FR012: summary counts are Management only
						.requestMatchers(HttpMethod.GET, "/api/summary").hasRole("Management")
						// FR001: only requesters submit
						.requestMatchers(HttpMethod.POST, "/api/requests").hasRole("Requester")
						// FR008-FR011: only staff act on a request
						.requestMatchers(HttpMethod.POST,
								"/api/requests/*/assign",
								"/api/requests/*/status",
								"/api/requests/*/comments").hasRole("Staff")
						// Everything else under /api needs a logged-in user
						.requestMatchers("/api/**").authenticated()
						.anyRequest().denyAll()
				);
		return http.build();
	}

	/**
	 * Delegating encoder: new passwords are stored as {bcrypt}..., and the prefix
	 * tells Spring which algorithm to use when checking a stored hash.
	 */
	@Bean
	PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource(
			@Value("${civicconnect.cors.allowed-origins:http://localhost:*,http://127.0.0.1:*}") String allowedOrigins
	) {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOriginPatterns(Arrays.stream(allowedOrigins.split(","))
				.map(String::trim)
				.filter(origin -> !origin.isEmpty())
				.toList());
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
		config.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}
}
