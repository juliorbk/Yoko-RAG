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

  // Un mapa en memoria para guardar los "baldes" (buckets) por cada IP
  private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

  private Bucket createNewBucket() {
    // Configuración del límite: 15 peticiones máximo por minuto.
    // Si se gastan las 15, el usuario debe esperar a que se recargue.
    Bandwidth limit = Bandwidth.builder()
      .capacity(15)
      .refillGreedy(15, Duration.ofMinutes(1))
      .build();

    return Bucket.builder().addLimit(limit).build();
  }

  @Override
  protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain
  ) throws ServletException, IOException {
    // Solo aplicamos el escudo a las rutas de la API, dejamos libre el login o carga de datos si quieres
    if (request.getRequestURI().startsWith("/api/sessions")) {
      // Obtenemos la IP del usuario
      // Nota: Si Render usa un proxy, podrías necesitar leer el header "X-Forwarded-For"
      String ip = request.getHeader("X-Forwarded-For");
      if (ip == null || ip.isEmpty()) {
        ip = request.getRemoteAddr();
      }

      // Buscamos el balde de esta IP, si no existe, lo creamos
      Bucket bucket = cache.computeIfAbsent(ip, k -> createNewBucket());

      // Intentamos consumir 1 token por esta petición
      if (bucket.tryConsume(1)) {
        // Hay tokens disponibles, dejamos pasar la petición
        filterChain.doFilter(request, response);
      } else {
        // No hay tokens, bloqueamos y devolvemos Error 429 (Too Many Requests)
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");
        response
          .getWriter()
          .write(
            "{\"error\": \"Has enviado demasiados mensajes a Yoko. Por favor, respira un minuto y vuelve a intentarlo.\"}"
          );
        return; // Cortamos la ejecución aquí
      }
    } else {
      // Si no es una ruta de /api/sessions, pasa directo
      filterChain.doFilter(request, response);
    }
  }
}
