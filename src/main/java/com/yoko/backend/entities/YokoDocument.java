package com.yoko.backend.entities;

import java.util.Map;
import lombok.Builder;
import org.springframework.ai.document.Document;

@Builder
public class YokoDocument {

  private String content;
  private String titulo; // "Reglamento Estudiantil"
  private String categoria; // "reglamento", "pensum", "calendario", etc.
  private String subcategoria; // "pasantias", "inscripcion", "historia", etc.
  private String fuente; // slug del documento, ej: "reglamento_estudiantil"
  private String organizationId; // ID de la organización a la que pertenece el documento

  public Document toSpringAiDocument() {
    return new Document(
      content,
      Map.of(
        "title",
        titulo,
        "categoria",
        categoria,
        "subcategoria",
        subcategoria,
        "organizationId",
        organizationId,
        "fuente",
        fuente
      )
    );
  }

  // funcion lista para cuando se quiera dividir un documento en chunks y crear un YokoDocument por cada chunk, con subcategoria indicando el número de chunk. No se está usando actualmente, pero podría ser útil para documentos muy largos que necesiten ser divididos para una mejor vectorización.

  //   public static List<YokoDocument> fromChunks(List<String> chunks, String titulo, String categoria) {
  //     return IntStream.range(0, chunks.size())
  //         .mapToObj(i -> YokoDocument.builder()
  //             .content(chunks.get(i))
  //             .titulo(titulo)
  //             .categoria(categoria)
  //             .subcategoria("chunk_" + (i + 1))
  //             .fuente(titulo.toLowerCase().replaceAll("\\s+", "_"))
  //             .build())
  //         .toList();
}
