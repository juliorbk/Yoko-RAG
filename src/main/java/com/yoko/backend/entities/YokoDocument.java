package com.yoko.backend.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.document.Document;

@Builder
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "yoko_documents") // Buena práctica: nombrar la tabla en plural
public class YokoDocument {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id; // ¡Faltaba la llave primaria!

  @Column(columnDefinition = "TEXT") // Vital para guardar párrafos o documentos largos
  private String content;

  private String titulo; // "Reglamento Estudiantil"
  private String categoria; // "reglamento", "pensum", "calendario", etc.
  private String subcategoria; // "pasantias", "inscripcion", "historia", etc.
  private String fuente; // slug del documento, ej: "reglamento_estudiantil"

  @ManyToOne(fetch = FetchType.LAZY) // Relación obligatoria para el modelo SaaS
  @JoinColumn(name = "organization_id", nullable = false)
  private Organization organization;

  public Document toSpringAiDocument() {
    // PRECAUCIÓN: Esto lanzará NullPointerException si llamas a este método
    // ANTES de hacer documentRepository.save() porque el 'id' será null.
    return new Document(
      content,
      Map.of(
        "organizationId",
        organization.getId().toString(),
        "yokoDocumentId",
        id.toString(), // Ahora sí existe 'id'
        "title",
        titulo,
        "categoria",
        categoria,
        "subcategoria",
        subcategoria != null ? subcategoria : "general",
        "fuente",
        fuente
      )
    );
  }
}
