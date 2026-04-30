package com.yoko.backend.DTOs;

import com.yoko.backend.entities.OrgSector;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrgRegisterRequest {

  // Datos de la organización
  @NotBlank(message = "Organization name is required")
  private String organizationName;

  // Datos del admin
  @NotBlank(message = "Name is required")
  private String adminName;

  @NotBlank(message = "Email is required")
  @Email(message = "Invalid email format")
  private String adminEmail;

  @NotBlank(message = "Password is required")
  private String adminPassword;

  @NotNull(message = "Sector is required")
  private OrgSector sector;
}
