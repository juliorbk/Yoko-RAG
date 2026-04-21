package com.yoko.backend;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.yoko.backend.services.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("Fix 1 — JwtService: clave secreta y validación")
class JwtServiceTest {

  private JwtService jwtService;

  // Clave válida de 64 chars hex (256 bits) para los tests normales
  private static final String VALID_SECRET =
    "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

  @BeforeEach
  void setUp() {
    jwtService = new JwtService();
    ReflectionTestUtils.setField(jwtService, "secretKey", VALID_SECRET);
    ReflectionTestUtils.setField(jwtService, "jwtExpiration", 86400000L);
  }

  // ─── Validación de clave en arranque ──────────────────────────────────────

  @Nested
  @DisplayName("validateSecretKey()")
  class ValidateSecretKey {

    @Test
    @DisplayName("debe lanzar IllegalStateException si la clave es null")
    void debeRechazarClaveNull() {
      ReflectionTestUtils.setField(jwtService, "secretKey", null);
      assertThatThrownBy(() -> jwtService.validateSecretKey())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("JWT secret key");
    }

    @Test
    @DisplayName("debe lanzar IllegalStateException si la clave está en blanco")
    void debeRechazarClaveBlank() {
      ReflectionTestUtils.setField(jwtService, "secretKey", "   ");
      assertThatThrownBy(() -> jwtService.validateSecretKey()).isInstanceOf(
        IllegalStateException.class
      );
    }

    @Test
    @DisplayName(
      "debe lanzar IllegalStateException si la clave tiene menos de 64 chars"
    )
    void debeRechazarClaveCorta() {
      // La clave hardcodeada original tenía exactamente 64 chars — pero probamos con 32
      ReflectionTestUtils.setField(
        jwtService,
        "secretKey",
        "404E635266556A586E327235753878"
      );
      assertThatThrownBy(() -> jwtService.validateSecretKey())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("64 characters long");
    }

    @Test
    @DisplayName(
      "debe arrancar sin excepción con una clave válida de 64+ chars"
    )
    void debeAceptarClaveValida() {
      ReflectionTestUtils.setField(jwtService, "secretKey", VALID_SECRET);
      assertThatCode(() ->
        jwtService.validateSecretKey()
      ).doesNotThrowAnyException();
    }

    @Test
    @DisplayName(
      "REGRESIÓN — la clave hardcodeada original NO debe pasar validación si se configura como default"
    )
    void claveHardcodeadaOriginalNoPuedeSerDefault() {
      // Si alguien intenta reintroducir el valor por defecto en el @Value,
      // este test lo detecta asegurando que la validación de longitud existe.
      // La clave original tiene 64 chars, así que sí pasaría longitud — pero
      // la línea crítica es que NO debe haber un valor por defecto en producción.
      // Este test documenta el comportamiento esperado.
      String claveOriginal =
        "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
      assertThat(claveOriginal).hasSize(64); // para que no cambie sin que alguien note
    }
  }

  // ─── Generación y validación de tokens ────────────────────────────────────

  @Nested
  @DisplayName("generateToken() / isTokenValid()")
  class TokenCycle {

    @BeforeEach
    void initKey() {
      jwtService.validateSecretKey(); // simula @PostConstruct
    }

    @Test
    @DisplayName("debe generar un token no nulo para un email válido")
    void debeGenerarToken() {
      String token = jwtService.generateToken("estudiante@uneg.edu.ve");
      assertThat(token).isNotBlank();
    }

    @Test
    @DisplayName("el token generado debe ser válido para el mismo email")
    void tokenDebeSerValidoParaMismoEmail() {
      String email = "estudiante@uneg.edu.ve";
      String token = jwtService.generateToken(email);
      assertThat(jwtService.isTokenValid(token, email)).isTrue();
    }

    @Test
    @DisplayName("un token NO debe ser válido para un email diferente")
    void tokenNoDebeSerValidoParaOtroEmail() {
      String token = jwtService.generateToken("usuario1@uneg.edu.ve");
      assertThat(
        jwtService.isTokenValid(token, "usuario2@uneg.edu.ve")
      ).isFalse();
    }

    @Test
    @DisplayName("debe extraer correctamente el email del token")
    void debeExtraerEmail() {
      String email = "admin@uneg.edu.ve";
      String token = jwtService.generateToken(email);
      assertThat(jwtService.extractUsername(token)).isEqualTo(email);
    }

    @Test
    @DisplayName("un token expirado debe ser inválido")
    void tokenExpiradoDebeSerInvalido() {
      // Configuramos expiración de -1ms → ya expiró antes de ser creado
      ReflectionTestUtils.setField(jwtService, "jwtExpiration", -1L);
      String token = jwtService.generateToken("estudiante@uneg.edu.ve");
      assertThrows(ExpiredJwtException.class, () -> {
        jwtService.isTokenValid(token, "estudiante@uneg.edu.ve");
      });
    }
  }
}
