package com.yoko.backend.DTOs;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataEntryRequest {

  @NotBlank(message = "Content is required")
  private String content;

  @NotBlank(message = "Title is required")
  private String titulo;

  @NotBlank(message = "Category is required")
  private String categoria;

  private String subcategoria;
}
