package com.yoko.backend.services;

import com.yoko.backend.DTOs.DataEntryRequest;
import com.yoko.backend.entities.Organization;
import com.yoko.backend.entities.User;
import com.yoko.backend.entities.YokoDocument;
import com.yoko.backend.repositories.YokoDocumentRepository;
import io.micrometer.common.lang.NonNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

  private final VectorStore vectorStore;
  private final YokoDocumentRepository yokoDocumentRepository;

  @SuppressWarnings("null")
  @Transactional
  public void ingest(DataEntryRequest req, User currentUser) {
    Organization org = currentUser.getOrganization();

    if (org == null) {
      throw new IllegalStateException(
        "El usuario no pertenece a ninguna organización"
      );
    }

    String fuente = generateSlug(req.getTitulo());

    YokoDocument yokoDoc = YokoDocument.builder()
      .content(req.getContent())
      .titulo(req.getTitulo())
      .categoria(req.getCategoria())
      .subcategoria(
        req.getSubcategoria() != null ? req.getSubcategoria() : "general"
      )
      .organization(org)
      .fuente(fuente)
      .build();

    YokoDocument savedDoc = yokoDocumentRepository.save(yokoDoc);

    // ← HashMap en vez de Map.of() para evitar NPE con valores null
    Map<String, Object> baseMetadata = new HashMap<>();
    baseMetadata.put("organizationId", org.getId().toString());
    baseMetadata.put("yokoDocumentId", savedDoc.getId().toString());
    baseMetadata.put(
      "title",
      savedDoc.getTitulo() != null ? savedDoc.getTitulo() : ""
    );
    baseMetadata.put(
      "categoria",
      savedDoc.getCategoria() != null ? savedDoc.getCategoria() : ""
    );
    baseMetadata.put(
      "subcategoria",
      savedDoc.getSubcategoria() != null
        ? savedDoc.getSubcategoria()
        : "general"
    );
    baseMetadata.put(
      "fuente",
      savedDoc.getFuente() != null ? savedDoc.getFuente() : ""
    );

    String[] fragments = req.getContent().split("---");
    List<Document> chunks = new ArrayList<>();

    for (String fragment : fragments) {
      if (!fragment.trim().isEmpty()) {
        chunks.add(new Document(fragment.trim(), new HashMap<>(baseMetadata)));
      }
    }

    vectorStore.add(chunks);

    log.info(
      "Documento '{}' dividido en {} chunks. Organización: '{}' Categoría: '{}' vectorizado correctamente",
      req.getTitulo(),
      chunks.size(),
      org.getName(),
      req.getCategoria()
    );
  }

  @Transactional
  public void deleteDocument(YokoDocument doc) {
    yokoDocumentRepository.delete(doc); // Eliminamos de la tabla de documentos relacional

    String filterExpression = String.format(
      "yokoDocumentId == '%s'",
      doc.getId().toString()
    );

    vectorStore.delete(filterExpression); // Eliminamos del vector store usando el filtro

    log.info(
      "Document '{}' y sus chunks asociados eliminados correctamente",
      doc.getId()
    );
  }

  @Transactional
  public void updateDocument(UUID id, YokoDocument incomingDoc) {
    YokoDocument existingDoc = yokoDocumentRepository
      .findById(id)
      .orElseThrow(() -> new RuntimeException("Documento no encontrado"));

    // Calcular fuente UNA sola vez
    String fuente = (incomingDoc.getFuente() != null &&
      !incomingDoc.getFuente().isBlank())
      ? incomingDoc.getFuente()
      : generateSlug(incomingDoc.getTitulo()); // ← desde el título nuevo, no el viejo

    existingDoc.setTitulo(incomingDoc.getTitulo());
    existingDoc.setCategoria(incomingDoc.getCategoria());
    existingDoc.setSubcategoria(
      incomingDoc.getSubcategoria() != null
        ? incomingDoc.getSubcategoria()
        : "general"
    );
    existingDoc.setFuente(fuente); // ← solo una vez, con el valor sanitizado
    existingDoc.setContent(incomingDoc.getContent());

    yokoDocumentRepository.save(existingDoc);

    Hibernate.initialize(existingDoc.getOrganization());
    Organization org = existingDoc.getOrganization();

    if (org == null) {
      throw new IllegalStateException(
        "El documento no pertenece a ninguna organización"
      );
    }

    String filterExpression = String.format(
      "yokoDocumentId == '%s'",
      id.toString()
    );
    vectorStore.delete(filterExpression);

    Map<String, Object> metadata = new HashMap<>();
    metadata.put("organizationId", org.getId().toString());
    metadata.put("yokoDocumentId", existingDoc.getId().toString());
    metadata.put(
      "title",
      existingDoc.getTitulo() != null ? existingDoc.getTitulo() : ""
    );
    metadata.put(
      "categoria",
      existingDoc.getCategoria() != null ? existingDoc.getCategoria() : ""
    );
    metadata.put(
      "subcategoria",
      existingDoc.getSubcategoria() != null
        ? existingDoc.getSubcategoria()
        : "general"
    );
    metadata.put(
      "fuente",
      existingDoc.getFuente() != null ? existingDoc.getFuente() : ""
    );

    String[] fragments = existingDoc.getContent().split("---");
    List<Document> chunks = new ArrayList<>();

    for (String fragment : fragments) {
      if (!fragment.trim().isEmpty()) {
        chunks.add(new Document(fragment.trim(), new HashMap<>(metadata)));
      }
    }

    vectorStore.add(chunks);

    log.info(
      "Documento '{}' re-vectorizado en {} chunks correctamente",
      existingDoc.getTitulo(),
      chunks.size()
    );
  }

  private String generateSlug(@NonNull String titulo) {
    return titulo
      .toLowerCase()
      .replaceAll("[^a-z0-9\\s]", "")
      .trim()
      .replaceAll("\\s+", "_");
  }
}
