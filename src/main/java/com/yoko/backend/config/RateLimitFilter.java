package com.yoko.backend.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

  // Clave compuesta: "IP:ruta" → su bucket
  private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

  private enum RateLimitedRoute {
    LOGIN("/api/auth/login", 5, 1), // 5 intentos por minuto
    REGISTER("/api/auth/register", 3, 5), // 3 registros por cada 5 minutos
    SESSIONS("/api/sessions", 15, 30); // 15 solicitudes por cada 30 minutos para endpoints de sesiones

    final String path;
    final int capacity;
    final int minutes;

    RateLimitedRoute(String path, int capacity, int minutes) {
      this.path = path;
      this.capacity = capacity;
      this.minutes = minutes;
    }
  }


  private Bucket createBucket(RateLimitedRoute route) {
    Bandwidth limit = Bandwidth.builder()
      .capacity(route.capacity)
      .refillGreedy(route.capacity, Duration.ofMinutes(route.minutes))
      .build();
    return Bucket.builder().addLimit(limit).build();
  }


  private String resolveIp(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isBlank()) {
      return forwarded.split(",")[0].trim(); // solo el primer IP, sanitizado
    }
    return request.getRemoteAddr();
  }


  @Override
  public void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain
  ) throws ServletException, IOException {
    String uri = request.getRequestURI();

    RateLimitedRoute matchedRoute = null;
    for (RateLimitedRoute route : RateLimitedRoute.values()) {
      if (uri.startsWith(route.path)) {
        matchedRoute = route;
        break;
      }
    }

    if (matchedRoute == null) {
      filterChain.doFilter(request, response);
      return;
    }

    String ip = resolveIp(request);
    String bucketKey = ip + ":" + matchedRoute.name();
    RateLimitedRoute finalRoute = matchedRoute;
    Bucket bucket = buckets.computeIfAbsent(bucketKey, k ->
      createBucket(finalRoute)
    );

    if (bucket.tryConsume(1)) {
      filterChain.doFilter(request, response);
    } else {
      response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
      response.setContentType("application/json");
      response
        .getWriter()
        .write(
          "{\"error\": \"Demasiadas solicitudes. Espera un momento e intenta de nuevo.\"}"
        );
    }
  }
}
