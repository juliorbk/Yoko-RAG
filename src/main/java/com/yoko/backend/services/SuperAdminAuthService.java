package com.yoko.backend.services;

import com.yoko.backend.DTOs.SuperAdminLoginRequest;
import com.yoko.backend.entities.SuperAdminCredentials;
import com.yoko.backend.repositories.SuperAdminCredentialsRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class SuperAdminAuthService {

  private final SuperAdminCredentialsRepository adminCredentialsRepository;
  private final JwtService jwtService;
  private final PasswordEncoder passwordEncoder;

  public String login(SuperAdminLoginRequest request) {
    SuperAdminCredentials credentials = adminCredentialsRepository
      .findByUsername(request.getUsername())
      .orElseThrow(() -> {
        // Mensaje genérico — no revelar si el username existe
        log.warn(
          "Intento de login super admin fallido para username: {}",
          request.getUsername()
        );
        return new BadCredentialsException("Credenciales inválidas");
      });

    if (!credentials.isActive()) {
      log.warn(
        "Intento de login super admin fallido para username: {}",
        request.getUsername()
      );
      throw new BadCredentialsException("Credenciales inválidas");
    }

    if (
      !passwordEncoder.matches(request.getPassword(), credentials.getPassword())
    ) {
      log.warn(
        "Intento de login super admin fallido para username: {}",
        request.getUsername()
      );
      throw new BadCredentialsException("Credenciales inválidas");
    }

    credentials.setLastLoginAt(LocalDateTime.now());
    adminCredentialsRepository.save(credentials);
    String jwtToken = jwtService.generateToken(credentials.getUsername());
    log.info("Super admin autenticado: {}", credentials.getUsername());
    return jwtToken;
  }

  /**
   * Crea las credenciales iniciales del super admin.
   * Solo debe llamarse una vez, desde un @CommandLineRunner de setup o migraciones.
   * En producción, protege este endpoint o elimínalo tras el primer uso.
   */

  public void createInitialCredentials(String username, String password) {
    if (adminCredentialsRepository.findByUsername(username).isPresent()) {
      log.info("SuperAdmin {} is already registered", username);
      return;
    }

    SuperAdminCredentials credentials = SuperAdminCredentials.builder()
      .username(username)
      .password(passwordEncoder.encode(password))
      .active(true)
      .build();

    adminCredentialsRepository.save(credentials);
    log.info("SuperAdmin {} created", username);
  }
}
