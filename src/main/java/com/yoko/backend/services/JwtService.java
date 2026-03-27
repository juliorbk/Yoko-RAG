package com.yoko.backend.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  // Esta es tu firma digital. Es una cadena Hexadecimal de 256 bits.
  // Le puse un valor por defecto para que compile directo, pero en producción
  // se lee del application.properties
  @Value(
    "${application.security.jwt.secret-key:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}"
  )
  private String secretKey;

  @Value("${application.security.jwt.expiration:86400000}") // 1 día en milisegundos
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
