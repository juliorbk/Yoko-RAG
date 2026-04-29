package com.yoko.backend.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class YokoDocDTO {

  private String id;
  private String title;
  private String categoria;
  private String subcategoria;
  private String fuente;
}
