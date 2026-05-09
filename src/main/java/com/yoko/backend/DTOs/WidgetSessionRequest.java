package com.yoko.backend.DTOs;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WidgetSessionRequest {

  @NotBlank(message = "Organization slug is required")
  private String organizationSlug;
}
