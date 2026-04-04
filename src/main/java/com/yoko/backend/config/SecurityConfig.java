package com.yoko.backend.config;

import java.util.Arrays;
import java.util.List;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthFilter;

  public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
    this.jwtAuthFilter = jwtAuthFilter;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http)
    throws Exception {
    http
      // 1. Un solo método para CORS apuntando a nuestra fuente
      .cors(cors -> cors.configurationSource(corsConfigurationSource()))
      // 2. Un solo método para desactivar CSRF
      .csrf(AbstractHttpConfigurer::disable)
      .authorizeHttpRequests(auth ->
        auth
          // Recursos estáticos y archivos comunes
          .requestMatchers(PathRequest.toStaticResources().atCommonLocations())
          .permitAll()
          .requestMatchers(
            "/",
            "/index.html",
            "/static/**",
            "/*.png",
            "/*.jpg",
            "/*.ico",
            "/favicon.ico"
          )
          .permitAll()
          // Endpoints públicos (Login, Registro, Swagger)
          .requestMatchers(
            "/api/auth/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/error"
          )
          .permitAll()
          // Endpoints de Administración
          .requestMatchers("/api/data-entry/**")
          .hasAuthority("ADMIN")
          // Todo lo demás requiere token
          .anyRequest()
          .authenticated()
      )
      .sessionManagement(session ->
        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      )
      .addFilterBefore(
        jwtAuthFilter,
        UsernamePasswordAuthenticationFilter.class
      );

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    // 🚨 CAMBIO 1: Usar setAllowedOriginPatterns para que el asterisco funcione
    configuration.setAllowedOriginPatterns(
      List.of(
        "http://localhost:5173", // PC local
        "https://yoko-frontend-*.vercel.app", // Cubre todas las previews
        "https://yoko-frontend-theta.vercel.app" // Tu dominio principal
      )
    );

    configuration.setAllowedMethods(
      Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS")
    );

    // 🚨 CAMBIO 2: Agregar el header especial de LocalTunnel o usar "*"
    configuration.setAllowedHeaders(
      Arrays.asList(
        "Authorization",
        "Content-Type",
        "Cache-Control",
        "Bypass-Tunnel-Reminder"
      )
    );

    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source =
      new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
