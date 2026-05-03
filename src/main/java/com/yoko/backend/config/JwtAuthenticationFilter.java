package com.yoko.backend.config;

import com.yoko.backend.entities.User;
import com.yoko.backend.repositories.SuperAdminCredentialsRepository;
import com.yoko.backend.repositories.UserRepository;
import com.yoko.backend.services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final UserRepository userRepository;
  private final SuperAdminCredentialsRepository superAdminRepository;

  public JwtAuthenticationFilter(
    JwtService jwtService,
    UserRepository userRepository,
    SuperAdminCredentialsRepository superAdminRepository
  ) {
    this.jwtService = jwtService;
    this.userRepository = userRepository;
    this.superAdminRepository = superAdminRepository;
  }

  @Override
  protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain
  ) throws ServletException, IOException {
    final String authHeader = request.getHeader("Authorization");
    final String jwt;
    final String identifier; // Cambiado: Ahora sabemos que puede ser Email o Username

    // 1. Si no hay token, lo dejamos pasar
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      log.debug("Received header {}", authHeader);
      filterChain.doFilter(request, response);
      return;
    }

    // 2. Extraemos el token y el identificador (subject del JWT)
    jwt = authHeader.substring(7);
    identifier = jwtService.extractUsername(jwt); // extractUsername saca el subject del token

    // 3. Verificamos si aún no está autenticado
    if (
      identifier != null &&
      SecurityContextHolder.getContext().getAuthentication() == null
    ) {
      UserDetails userDetails = null;

      // LÓGICA NUEVA: Identificador dual
      // Intentamos buscarlo primero como usuario normal (por EMAIL)
      var regularUser = userRepository.findByEmail(identifier);

      if (regularUser.isPresent()) {
        userDetails = regularUser.get();
      } else {
        // Si no está, significa que el identificador es un USERNAME de Super Admin
        userDetails = superAdminRepository
          .findByUsername(identifier)
          .orElseThrow(() ->
            new RuntimeException("Usuario/Email no encontrado en el sistema")
          );
      }

      // 4. Si el token es válido, creamos la autenticación
      if (jwtService.isTokenValid(jwt, userDetails.getUsername())) {
        log.debug("Token válido para: {}", userDetails.getUsername());

        // Verificar si el usuario está activo
        if (!userDetails.isEnabled()) {
          log.warn("Intento de acceso con usuario desactivado: {}", userDetails.getUsername());
          response.setStatus(HttpStatus.FORBIDDEN.value());
          filterChain.doFilter(request, response);
          return;
        }

        // Verificar si la organización está activa
        if (userDetails instanceof User user) {
          if (
            user.getOrganization() == null || !user.getOrganization().isActive()
          ) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            filterChain.doFilter(request, response);
            return;
          }
        }

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

    filterChain.doFilter(request, response);
  }
}
