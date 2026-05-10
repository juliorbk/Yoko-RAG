package com.yoko.backend.DTOs;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WidgetSessionRequest {

  @NotBlank(message = "Organization slug is required")
  private String organizationSlug;
}
