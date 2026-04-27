package com.yoko.backend.services;

import com.yoko.backend.DTOs.DataEntryRequest;
import com.yoko.backend.entities.YokoDocument;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataEntryService {

  private final VectorStore vectorStore;

  public void ingest(DataEntryRequest req, UUID organizationId) {
    // Genera el slug de fuente a partir del título
    String fuente = req
      .getTitulo()
      .toLowerCase()
      .replaceAll("[^a-z0-9\\s]", "")
      .trim()
      .replaceAll("\\s+", "_");

    YokoDocument yokoDoc = YokoDocument.builder()
      .content(req.getContent())
      .titulo(req.getTitulo())
      .categoria(req.getCategoria())
      .subcategoria(
        req.getSubcategoria() != null ? req.getSubcategoria() : "general"
      )
      .organizationId(organizationId.toString())
      .fuente(fuente)
      .build();

    vectorStore.add(List.of(yokoDoc.toSpringAiDocument()));

    log.info(
      "Documento '{}' vectorizado correctamente [{}/{}]",
      req.getTitulo(),
      req.getCategoria(),
      req.getCategoria()
    );
  }
}
