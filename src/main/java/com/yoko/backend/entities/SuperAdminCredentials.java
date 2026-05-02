package com.yoko.backend.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data; // Asumo que usas Lombok para getters/setters
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(name = "super_admin_credentials")
@Data // Tus anotaciones de Lombok o tus Getters/Setters manuales
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuperAdminCredentials implements UserDetails {

  // 🔥 1. IMPLEMENTAR LA INTERFAZ

  @Id
  @GeneratedValue
  private UUID id;

  @Column(nullable = false, unique = true)
  private String username;

  @Column(nullable = false)
  private String password;

  @Column(nullable = false)
  private boolean active;

  private LocalDateTime createdAt;
  private LocalDateTime lastLoginAt;

  // ====================================================================
  // 🔥 2. AGREGAR LOS MÉTODOS OBLIGATORIOS DE USERDETAILS
  // ====================================================================

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    // Le damos el poder absoluto.
    // Nota: En tu SecurityConfig pusiste .hasAuthority("SUPER_ADMIN"), así que debe coincidir exacto.
    return List.of(new SimpleGrantedAuthority("SUPER_ADMIN"));
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true; // Podrías atarlo a tu variable 'active' si quieres bloquear superadmins
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return active; // Si active es false, Spring Security no lo dejará loguearse
  }
}
