package com.yoko.backend.DTOs;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// ─── Login del Super Admin ────────────────────────────────────────────────────
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SuperAdminLoginRequest {

  @NotBlank(message = "Username is required")
  private String username;

  @NotBlank(message = "Password is required")
  private String password;
}
