package com.yoko.backend.config;

import com.yoko.backend.entities.User;
import com.yoko.backend.repositories.UserRepository;
import com.yoko.backend.services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final UserRepository userRepository;

  public JwtAuthenticationFilter(
    JwtService jwtService,
    UserRepository userRepository
  ) {
    this.jwtService = jwtService;
    this.userRepository = userRepository;
  }

  @Override
  protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain
  ) throws ServletException, IOException {
    final String authHeader = request.getHeader("Authorization");
    final String jwt;
    final String userEmail;

    // 1. Si no hay token, lo dejamos pasar al siguiente filtro (y SecurityConfig lo rebotará con el 403)
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      log.debug("Received header {}", authHeader);
      filterChain.doFilter(request, response);
      return;
    }

    // 2. Extraemos el token (quitando los 7 caracteres de "Bearer ")
    jwt = authHeader.substring(7);
    userEmail = jwtService.extractUsername(jwt);
    log.info("Logged in successfully user: {}", userEmail);

    // 3. Si hay un correo en el token y el usuario aún no está autenticado en este hilo
    if (
      userEmail != null &&
      SecurityContextHolder.getContext().getAuthentication() == null
    ) {
      // Buscamos al usuario en la BD de
      User userDetails = userRepository
        .findByEmail(userEmail)
        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

      // Si el token es válido, creamos el "Pase VIP" y lo guardamos en el contexto
      if (jwtService.isTokenValid(jwt, userDetails.getUsername())) {
        log.debug("Token valido");
        UsernamePasswordAuthenticationToken authToken =
          new UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.getAuthorities()
          );
        authToken.setDetails(
          new WebAuthenticationDetailsSource().buildDetails(request)
        );
        SecurityContextHolder.getContext().setAuthentication(authToken);
      }
    }
    // Continuamos con la petición (ahora sí, dejándolo pasar al controlador)
    filterChain.doFilter(request, response);
  }
}
