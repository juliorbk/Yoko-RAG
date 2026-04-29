package com.yoko.backend.services;

import com.yoko.backend.DTOs.DataEntryRequest;
import com.yoko.backend.entities.Organization;
import com.yoko.backend.entities.User;
import com.yoko.backend.entities.YokoDocument;
import com.yoko.backend.repositories.YokoDocumentRepository;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataEntryService {

  private final VectorStore vectorStore;
  private final YokoDocumentRepository yokoDocumentRepository;

  @Transactional
  public void ingest(DataEntryRequest req, User currentUser) {
    Organization org = currentUser.getOrganization();

    if (org == null) {
      throw new IllegalStateException(
        "El usuario no pertenece a ninguna organización"
      );
    }

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
      .organization(org)
      .fuente(fuente)
      .build();

    YokoDocument savedDoc = yokoDocumentRepository.save(yokoDoc);

    Document springDoc = new Document(
      savedDoc.getContent(),
      Map.of(
        "organizationId",
        org.getId().toString(),
        "yokoDocumentId",
        savedDoc.getId().toString(),
        "title",
        savedDoc.getTitulo(),
        "categoria",
        savedDoc.getCategoria(),
        "subcategoria",
        savedDoc.getSubcategoria(),
        "fuente",
        savedDoc.getFuente()
      )
    );
    // 4. Chunking (Fragmentación del texto para el LLM)
    // Esto divide un texto largo en pedazos de ~800 tokens con un solapamiento (overlap) para no cortar ideas.
    TokenTextSplitter textSplitter = new TokenTextSplitter(
      800,
      400,
      5,
      10000,
      true
    );
    List<Document> chunks = textSplitter.apply(List.of(springDoc));

    vectorStore.add(chunks);

    log.info(
      "Documento '{}' Chunks: {} Organizacion: '{}' Categoría: '{}' vectorizado correctamente",
      req.getTitulo(),
      chunks.size(),
      org.getName(),
      req.getCategoria()
    );
  }
}
