package com.yoko.backend.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  @Value("${application.security.jwt.secret-key}") // Se inyecta la clave secreta desde application.properties
  private String secretKey;

  @PostConstruct
  public void validateSecretKey() {
    if (secretKey == null || secretKey.isEmpty() || secretKey.length() < 64) {
      throw new IllegalStateException(
        "JWT secret key must be set in application properties, be at least 64 characters long, and not be empty"
      );
    }
  }

  @Value("${application.security.jwt.expiration: 10800000}") // 3 horas en milisegundos
  private long jwtExpiration;

  // 1. Extraer el correo del token (que es nuestro Subject)
  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  // 2. Método genérico para extraer cualquier dato del token
  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  // 3. Generar un token solo con el correo
  public String generateToken(String email) {
    return generateToken(new HashMap<>(), email);
  }

  // 4. Generar un token con datos extra (Claims)
  public String generateToken(Map<String, Object> extraClaims, String email) {
    return Jwts.builder()
      .claims(extraClaims)
      .subject(email) // El "dueño" del token es el correo
      .issuedAt(new Date(System.currentTimeMillis()))
      .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
      .signWith(getSignInKey(), Jwts.SIG.HS256) // Firmado con algoritmo HS256
      .compact();
  }

  // 5. Validar que el token pertenezca al usuario y no haya expirado
  public boolean isTokenValid(String token, String userEmail) {
    final String username = extractUsername(token);
    return (username.equals(userEmail)) && !isTokenExpired(token);
  }

  private boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  private Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  // Desencriptar el token para leer lo que tiene adentro
  private Claims extractAllClaims(String token) {
    return Jwts.parser()
      .verifyWith(getSignInKey())
      .build()
      .parseSignedClaims(token)
      .getPayload();
  }

  // Obtener la llave maestra en el formato que exige la librería
  private SecretKey getSignInKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    return Keys.hmacShaKeyFor(keyBytes);
  }
}
