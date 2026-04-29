package com.yoko.backend.DTOs;

import com.yoko.backend.entities.YokoDocument;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder // Añadir Builder siempre es útil
@NoArgsConstructor
@AllArgsConstructor
public class YokoDocDTO {

  // Cambiamos de String a UUID para mantener la consistencia estricta de tipos
  private UUID id;
  private String titulo; // Cambiado a 'titulo' para que coincida exactamente con tu entidad
  private String categoria;
  private String subcategoria;
  private String fuente;

  /**
   * Método de conveniencia para convertir la Entidad (Postgres) al DTO.
   * Fíjate que omitimos deliberadamente el campo 'content' y 'organization'.
   * - No enviamos el 'content' porque pesaría megabytes si es un PDF de 100 páginas.
   * - No enviamos 'organization' por seguridad (el usuario ya sabe en qué org está).
   */
  public static YokoDocDTO fromEntity(YokoDocument doc) {
    return YokoDocDTO.builder()
      .id(doc.getId())
      .titulo(doc.getTitulo())
      .categoria(doc.getCategoria())
      .subcategoria(doc.getSubcategoria())
      .fuente(doc.getFuente())
      .build();
  }
}
