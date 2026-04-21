package com.yoko.backend;

import static org.assertj.core.api.Assertions.assertThat;

import com.yoko.backend.config.RateLimitFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@DisplayName("Fix 2 — RateLimitFilter: límites por ruta y sanitización de IP")
class RateLimitFilterTest {

  private RateLimitFilter filter;

  @BeforeEach
  void setUp() {
    filter = new RateLimitFilter();
  }

  // Helper para disparar N peticiones desde una IP y ruta dadas
  private MockHttpServletResponse fireRequest(String uri, String ip)
    throws Exception {
    MockHttpServletRequest req = new MockHttpServletRequest("POST", uri);
    req.setRemoteAddr(ip);
    MockHttpServletResponse res = new MockHttpServletResponse();
    MockFilterChain chain = new MockFilterChain();
    filter.doFilterInternal(req, res, chain);
    return res;
  }

  private MockHttpServletResponse fireRequestWithHeader(
    String uri,
    String forwardedFor
  ) throws Exception {
    MockHttpServletRequest req = new MockHttpServletRequest("POST", uri);
    req.addHeader("X-Forwarded-For", forwardedFor);
    req.setRemoteAddr("10.0.0.1");
    MockHttpServletResponse res = new MockHttpServletResponse();
    MockFilterChain chain = new MockFilterChain();
    filter.doFilterInternal(req, res, chain);
    return res;
  }

  // ─── Rutas no limitadas ───────────────────────────────────────────────────

  @Nested
  @DisplayName("Rutas sin rate limit")
  class RutasSinLimite {

    @Test
    @DisplayName("debe dejar pasar /swagger-ui sin aplicar límite")
    void swaggerPasaLibre() throws Exception {
      MockHttpServletResponse res = fireRequest(
        "/swagger-ui/index.html",
        "1.1.1.1"
      );
      assertThat(res.getStatus()).isNotEqualTo(
        HttpStatus.TOO_MANY_REQUESTS.value()
      );
    }

    @Test
    @DisplayName("debe dejar pasar /v3/api-docs sin aplicar límite")
    void apiDocsPasaLibre() throws Exception {
      MockHttpServletResponse res = fireRequest("/v3/api-docs", "1.1.1.1");
      assertThat(res.getStatus()).isNotEqualTo(
        HttpStatus.TOO_MANY_REQUESTS.value()
      );
    }
  }

  // ─── /api/auth/login ──────────────────────────────────────────────────────

  @Nested
  @DisplayName("/api/auth/login — límite 5 por minuto")
  class LoginRateLimit {

    @Test
    @DisplayName("debe permitir hasta 5 intentos de login desde la misma IP")
    void debePermitirHasta5() throws Exception {
      String ip = "50.50.50.1";
      for (int i = 0; i < 5; i++) {
        MockHttpServletResponse res = fireRequest("/api/auth/login", ip);
        assertThat(res.getStatus())
          .as("Petición %d debe pasar", i + 1)
          .isNotEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
      }
    }

    @Test
    @DisplayName("debe bloquear el 6to intento de login con 429")
    void debeBloquerElSextoIntento() throws Exception {
      String ip = "50.50.50.2";
      for (int i = 0; i < 5; i++) fireRequest("/api/auth/login", ip);

      MockHttpServletResponse res = fireRequest("/api/auth/login", ip);
      assertThat(res.getStatus()).isEqualTo(
        HttpStatus.TOO_MANY_REQUESTS.value()
      );
    }

    @Test
    @DisplayName("la respuesta 429 debe tener Content-Type application/json")
    void respuesta429DebeSerJson() throws Exception {
      String ip = "50.50.50.3";
      for (int i = 0; i < 5; i++) fireRequest("/api/auth/login", ip);

      MockHttpServletResponse res = fireRequest("/api/auth/login", ip);
      assertThat(res.getContentType()).contains("application/json");
    }

    @Test
    @DisplayName("IPs distintas deben tener buckets independientes")
    void ipDistintasIndependientes() throws Exception {
      // IP A agota su límite
      for (int i = 0; i < 5; i++) fireRequest("/api/auth/login", "50.50.50.10");
      MockHttpServletResponse bloqueada = fireRequest(
        "/api/auth/login",
        "50.50.50.10"
      );
      assertThat(bloqueada.getStatus()).isEqualTo(
        HttpStatus.TOO_MANY_REQUESTS.value()
      );

      // IP B no debe estar afectada
      MockHttpServletResponse otraIp = fireRequest(
        "/api/auth/login",
        "50.50.50.11"
      );
      assertThat(otraIp.getStatus()).isNotEqualTo(
        HttpStatus.TOO_MANY_REQUESTS.value()
      );
    }
  }

  // ─── /api/auth/register ───────────────────────────────────────────────────

  @Nested
  @DisplayName("/api/auth/register — límite 3 por minuto")
  class RegisterRateLimit {

    @Test
    @DisplayName("debe permitir hasta 3 registros")
    void debePermitirHasta3() throws Exception {
      String ip = "60.60.60.1";
      for (int i = 0; i < 3; i++) {
        MockHttpServletResponse res = fireRequest("/api/auth/register", ip);
        assertThat(res.getStatus())
          .as("Registro %d debe pasar", i + 1)
          .isNotEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
      }
    }

    @Test
    @DisplayName("debe bloquear el 4to registro con 429")
    void debeBloquerElCuarto() throws Exception {
      String ip = "60.60.60.2";
      for (int i = 0; i < 3; i++) fireRequest("/api/auth/register", ip);

      MockHttpServletResponse res = fireRequest("/api/auth/register", ip);
      assertThat(res.getStatus()).isEqualTo(
        HttpStatus.TOO_MANY_REQUESTS.value()
      );
    }
  }

  // ─── Buckets independientes por ruta ─────────────────────────────────────

  @Nested
  @DisplayName("Buckets independientes por ruta")
  class BucketsIndependientes {

    @Test
    @DisplayName("agotar el límite de login NO debe afectar el de sessions")
    void loginNoCuentaParaSessions() throws Exception {
      String ip = "70.70.70.1";
      // Agotamos login
      for (int i = 0; i < 5; i++) fireRequest("/api/auth/login", ip);
      fireRequest("/api/auth/login", ip); // este ya da 429

      // Sessions debe seguir funcionando
      MockHttpServletResponse sessions = fireRequest(
        "/api/sessions/some-uuid/messages",
        ip
      );
      assertThat(sessions.getStatus()).isNotEqualTo(
        HttpStatus.TOO_MANY_REQUESTS.value()
      );
    }
  }

  // ─── Sanitización de X-Forwarded-For (Fix 7 combinado) ───────────────────

  @Nested
  @DisplayName("Sanitización de X-Forwarded-For")
  class XForwardedFor {

    @Test
    @DisplayName("debe usar solo el primer IP de una lista con comas")
    void debeUsarPrimerIp() throws Exception {
      // Enviamos "1.2.3.4, 5.6.7.8, 9.10.11.12" — debe tratarse como 1.2.3.4
      // Primero agotamos el límite desde "1.2.3.4"
      for (int i = 0; i < 5; i++) {
        fireRequestWithHeader("/api/auth/login", "1.2.3.4, 5.6.7.8");
      }
      MockHttpServletResponse bloqueada = fireRequestWithHeader(
        "/api/auth/login",
        "1.2.3.4, 5.6.7.8"
      );
      assertThat(bloqueada.getStatus()).isEqualTo(
        HttpStatus.TOO_MANY_REQUESTS.value()
      );
    }

    @Test
    @DisplayName("un IP diferente en el mismo header no debe estar bloqueado")
    void otroIpNoEstaAfectado() throws Exception {
      // Agotamos 1.2.3.4
      for (int i = 0; i < 5; i++) {
        fireRequestWithHeader("/api/auth/login", "1.2.3.4");
      }
      // 9.9.9.9 no debe estar bloqueado
      MockHttpServletResponse res = fireRequestWithHeader(
        "/api/auth/login",
        "9.9.9.9"
      );
      assertThat(res.getStatus()).isNotEqualTo(
        HttpStatus.TOO_MANY_REQUESTS.value()
      );
    }
  }
}
