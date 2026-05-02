package com.yoko.backend.DTOs;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

// ─── Login del Super Admin ────────────────────────────────────────────────────
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SuperAdminLoginRequest {

  @NotBlank(message = "Username is required")
  private String username;

  @NotBlank(message = "Password is required")
  private String password;
}
