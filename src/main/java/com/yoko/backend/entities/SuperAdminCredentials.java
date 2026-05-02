package com.yoko.backend.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

/**
 * Credenciales del Super Admin — completamente separadas del sistema de usuarios normales.
 *
 * No implementa UserDetails porque no usa el filtro JWT estándar.
 * La autenticación se hace en /api/super/login con su propio servicio.
 * El token generado incluye el claim "role: SUPER_ADMIN" para que los guards lo distingan.
 */
@Entity
@Table(name = "super_admin_credentials")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuperAdminCredentials {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(unique = true, nullable = false)
  private String username; // no email — identificador interno

  @Column(nullable = false)
  private String password; // BCrypt

  @Column(nullable = false)
  @Builder.Default
  private boolean active = true;

  @Column(name = "last_login_at")
  private LocalDateTime lastLoginAt;

  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
  }
}
