package com.yoko.backend.DTOs;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChangePlanRequest {

  @NotBlank(message = "Plan is required")
  private String plan; // "FREE" | "PRO" | "ENTERPRISE"
}
