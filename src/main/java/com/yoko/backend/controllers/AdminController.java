package com.yoko.backend.controllers;

import com.yoko.backend.DTOs.DataEntryRequest;
import com.yoko.backend.DTOs.StatsResponse;
import com.yoko.backend.DTOs.UserDTO;
import com.yoko.backend.DTOs.YokoDocDTO;
import com.yoko.backend.entities.ChatSession;
import com.yoko.backend.entities.Message;
import com.yoko.backend.entities.User;
import com.yoko.backend.repositories.ChatSessionRepository;
import com.yoko.backend.repositories.MessageRepository;
import com.yoko.backend.repositories.UserRepository;
import com.yoko.backend.repositories.YokoDocumentRepository;
import com.yoko.backend.services.DataEntryService;
import com.yoko.backend.services.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@CrossOrigin(origins = "https://yoko-frontend-rho.vercel.app")
@RestController
@RequestMapping("/api/admin")
@Tag(
  name = "Admin",
  description = "Endpoints for administrative tasks like loading data into the vector store"
)
public class AdminController {

  private final DataEntryService dataEntryService;
  private final UserRepository userRepository;
  private final StatsService statsService;
  private final YokoDocumentRepository yokoDocumentRepository;
  private final ChatSessionRepository chatSessionRepository;
  private final MessageRepository messageRepository;

  public AdminController(
    DataEntryService dataEntryService,
    UserRepository userRepository,
    StatsService statsService,
    YokoDocumentRepository yokoDocumentRepository,
    ChatSessionRepository chatSessionRepository,
    MessageRepository messageRepository
  ) {
    this.dataEntryService = dataEntryService;
    this.userRepository = userRepository;
    this.statsService = statsService;
    this.yokoDocumentRepository = yokoDocumentRepository;
    this.chatSessionRepository = chatSessionRepository;
    this.messageRepository = messageRepository;
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
    dataEntryService.ingest(request, currentUser);
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
}
