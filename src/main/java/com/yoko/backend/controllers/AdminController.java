package com.yoko.backend.controllers;

import com.yoko.backend.DTOs.AuthResponse;
import com.yoko.backend.DTOs.DataEntryRequest;
import com.yoko.backend.DTOs.LoginRequest;
import com.yoko.backend.DTOs.StatsResponse;
import com.yoko.backend.DTOs.UserDTO;
import com.yoko.backend.DTOs.YokoDocDTO;
import com.yoko.backend.entities.User;
import com.yoko.backend.repositories.ChatSessionRepository;
import com.yoko.backend.repositories.MessageRepository;
import com.yoko.backend.repositories.UserRepository;
import com.yoko.backend.repositories.YokoDocumentRepository;
import com.yoko.backend.services.AuthService;
import com.yoko.backend.services.DataEntryService;
import com.yoko.backend.services.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
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

  public AdminController(
    DataEntryService dataEntryService,
    UserRepository userRepository,
    StatsService statsService,
    YokoDocumentRepository yokoDocumentRepository
  ) {
    this.dataEntryService = dataEntryService;
    this.userRepository = userRepository;
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
    dataEntryService.ingest(request, currentUser.getOrganization().getId());
    return ResponseEntity.ok(Map.of("message", "Data loaded successfully"));
  }

  @GetMapping("/users")
  @Operation(
    summary = "Get all users",
    description = "Endpoint to retrieve all users"
  )
  public ResponseEntity<List<UserDTO>> getUsers() {
    List<UserDTO> users = userRepository
      .findAll()
      .stream()
      .map(UserDTO::fromUser)
      .toList();

    log.info("Retrieved all users from the database");
    return ResponseEntity.ok(users);
  }

  @GetMapping("/stats")
  @Operation(
    summary = "Get system statistics",
    description = "Endpoint to retrieve statistics about users, chat sessions, and messages"
  )
  public ResponseEntity<StatsResponse> getStats() {
    StatsResponse stats = statsService.buildStats();
    // Implement logic to gather statistics about the system
    System.out.println("Stats: " + stats);
    return ResponseEntity.ok(stats);
  }

  @GetMapping("/docs")
  public ResponseEntity<List<YokoDocDTO>> getDocs() {
    List<YokoDocDTO> docs = yokoDocumentRepository.findAll();
    log.info("Retrieved {} documents from vector_store", docs.size());
    return ResponseEntity.ok(docs);
  }
}
