package com.yoko.backend.DTOs;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangePlanRequest {

  @NotBlank(message = "Plan is required")
  private String plan; // "FREE" | "PRO" | "ENTERPRISE"
}
