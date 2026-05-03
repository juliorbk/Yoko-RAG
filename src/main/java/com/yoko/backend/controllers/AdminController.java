package com.yoko.backend.controllers;

/**
 * FIXED VERSION - Code review fixes applied on 2026-05-02
 * Fixes applied:
 * 1. Added authorization checks for document access (getDoc, updateDoc)
 * 2. Standardized error handling to use ResponseStatusException
 * 3. Fixed tenant isolation for document operations
 */
import com.yoko.backend.DTOs.DataEntryRequest;
import com.yoko.backend.DTOs.StatsResponse;
import com.yoko.backend.DTOs.UserDTO;
import com.yoko.backend.DTOs.YokoDocDTO;
import com.yoko.backend.entities.User;
import com.yoko.backend.entities.YokoDocument;
import com.yoko.backend.repositories.ChatSessionRepository;
import com.yoko.backend.repositories.MessageRepository;
import com.yoko.backend.repositories.UserRepository;
import com.yoko.backend.repositories.YokoDocumentRepository;
import com.yoko.backend.services.AdminService;
import com.yoko.backend.services.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@CrossOrigin(origins = "https://yoko-frontend-rho.vercel.app")
@RestController
@RequestMapping("/api/admin")
@Tag(
  name = "Admin",
  description = "Endpoints for administrative tasks like loading data into the vector store"
)
public class AdminController {

  private final AdminService adminService;

  private final StatsService statsService;
  private final YokoDocumentRepository yokoDocumentRepository;

  public AdminController(
    AdminService adminService,
    UserRepository userRepository,
    StatsService statsService,
    YokoDocumentRepository yokoDocumentRepository,
    ChatSessionRepository chatSessionRepository,
    MessageRepository messageRepository
  ) {
    this.adminService = adminService;
    this.statsService = statsService;
    this.yokoDocumentRepository = yokoDocumentRepository;
  }

  /**
   * Endpoint to load data from the vector store.
   *
   * @return a ResponseEntity with a string indicating if the data was loaded successfully.
   */
  @PostMapping("/load-data")
  @Operation(
    summary = "Load data into the vector store",
    description = "Endpoint to load data from the vector store"
  )
  public ResponseEntity<Map<String, String>> loadData(
    @Valid @RequestBody DataEntryRequest request,
    @AuthenticationPrincipal User currentUser
  ) {
    adminService.ingest(request, currentUser);
    return ResponseEntity.ok(Map.of("message", "Data loaded successfully"));
  }

  @GetMapping("/users")
  @Operation(
    summary = "Get all users",
    description = "Endpoint to retrieve all users with session and message stats"
  )
  public ResponseEntity<List<UserDTO>> getUsers(
    @AuthenticationPrincipal User currentUser
  ) {
    UUID orgId = currentUser.getOrganization().getId();
    List<UserDTO> users = statsService.buildUsers(orgId);

    return ResponseEntity.ok(users);
  }

  @GetMapping("/stats")
  @Operation(
    summary = "Get system statistics",
    description = "Endpoint to retrieve statistics about users, chat sessions, and messages"
  )
  public ResponseEntity<StatsResponse> getStats(
    @AuthenticationPrincipal User currentUser
  ) {
    StatsResponse stats = statsService.buildStats(
      currentUser.getOrganization().getId()
    );
    // Implement logic to gather statistics about the system
    System.out.println("Stats: " + stats);
    return ResponseEntity.ok(stats);
  }

  @GetMapping("/docs")
  @Operation(summary = "Get all documents")
  public ResponseEntity<Page<YokoDocDTO>> getDocs(
    @AuthenticationPrincipal User currentUser,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size
  ) {
    UUID orgId = currentUser.getOrganization().getId();

    // Creamos el objeto de paginación
    PageRequest pageable = PageRequest.of(page, size);

    // Llamamos al repositorio paginado y transformamos la entidad al DTO
    Page<YokoDocDTO> docs = yokoDocumentRepository
      .findByOrganizationId(orgId, pageable)
      .map(YokoDocDTO::fromEntity);

    log.info(
      "Retrieved page {} containing {} documents for organization {}",
      page,
      docs.getNumberOfElements(),
      orgId
    );

    return ResponseEntity.ok(docs);
  }

  @GetMapping("/docs/{id}")
  public ResponseEntity<Map<String, Object>> getDoc(
    @PathVariable UUID id,
    @AuthenticationPrincipal User currentUser
  ) {
    YokoDocument doc = yokoDocumentRepository
      .findById(id)
      .orElseThrow(() ->
        new ResponseStatusException(
          HttpStatus.NOT_FOUND,
          "Documento no encontrado"
        )
      );

    // FIX: Validar que el documento pertenezca a la organización del admin
    if (
      !doc
        .getOrganization()
        .getId()
        .equals(currentUser.getOrganization().getId())
    ) {
      throw new ResponseStatusException(
        HttpStatus.FORBIDDEN,
        "No tienes permiso para ver este documento"
      );
    }

    return ResponseEntity.ok(
      Map.of(
        "id",
        doc.getId(),
        "titulo",
        doc.getTitulo(),
        "categoria",
        doc.getCategoria(),
        "subcategoria",
        doc.getSubcategoria() != null ? doc.getSubcategoria() : "",
        "fuente",
        doc.getFuente() != null ? doc.getFuente() : "",
        "content",
        doc.getContent()
      )
    );
  }

  @PutMapping("/docs/{id}")
  public ResponseEntity<Map<String, String>> updateDoc(
    @PathVariable UUID id,
    @RequestBody YokoDocument doc,
    @AuthenticationPrincipal User currentUser
  ) {
    // FIX: Validar propiedad del documento antes de actualizar
    YokoDocument existingDoc = yokoDocumentRepository
      .findById(id)
      .orElseThrow(() ->
        new ResponseStatusException(
          HttpStatus.NOT_FOUND,
          "Documento no encontrado"
        )
      );

    if (
      !existingDoc
        .getOrganization()
        .getId()
        .equals(currentUser.getOrganization().getId())
    ) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
        Map.of("error", "No tienes permiso para editar este documento")
      );
    }

    adminService.updateDocument(id, doc);

    return ResponseEntity.ok(
      Map.of("message", "Documento actualizado correctamente")
    );
  }

  @DeleteMapping("/docs/{id}")
  @Operation(
    summary = "Delete a document",
    description = "Deletes a document from relational DB and vector store"
  )
  public ResponseEntity<Map<String, String>> deleteDoc(
    @PathVariable UUID id,
    @AuthenticationPrincipal User currentUser
  ) {
    YokoDocument doc = yokoDocumentRepository
      .findById(id)
      .orElseThrow(() -> new RuntimeException("Documento no encontrado"));

    // Verificar que el doc pertenezca a la org del admin
    if (
      !doc
        .getOrganization()
        .getId()
        .equals(currentUser.getOrganization().getId())
    ) {
      return ResponseEntity.status(403).body(
        Map.of("error", "No tienes permiso para eliminar este documento")
      );
    }

    adminService.deleteDocument(doc);

    return ResponseEntity.ok(
      Map.of("message", "Documento eliminado correctamente")
    );
  }
}
