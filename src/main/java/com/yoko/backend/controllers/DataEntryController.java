package com.yoko.backend.controllers;

import java.util.List;
import java.util.Map;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/data-entry")
public class DataEntryController {

  private final VectorStore vectorStore;

  public DataEntryController(VectorStore vectorStore) {
    this.vectorStore = vectorStore;
  }

  /**
   * Endpoint to load data from the vector store.
   *
   * @return a ResponseEntity with a string indicating if the data was loaded successfully.
   */
  @PostMapping("/load-data")
  public ResponseEntity<String> loadData() {
    List<Document> documentosUneg = List.of(
      // ============================================================
      // DOCUMENTOS UNEG - Universidad Nacional Experimental de Guayana
      // Formato: new Document(contenido, Map.of(metadatos))
      // ============================================================
    );

    vectorStore.add(documentosUneg);
    return ResponseEntity.ok("Data loaded successfully");
  }
}
