package com.yoko.backend.controllers;

import com.yoko.backend.entities.ChatSession;
import com.yoko.backend.entities.Message;
import com.yoko.backend.entities.User;
import com.yoko.backend.repositories.ChatSessionRepository;
import com.yoko.backend.repositories.UserRepository;
import com.yoko.backend.services.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sessions")
@CrossOrigin(origins = "*")
@Tag(name = "Chat")
public class ChatController {

  private final ChatService chatService;
  private final ChatSessionRepository sessionRepository;
  private final UserRepository userRepository;

  //Inyeccion de dependencias
  public ChatController(
    ChatService chatService,
    ChatSessionRepository sessionRepository,
    UserRepository userRepository
  ) {
    this.chatService = chatService;
    this.sessionRepository = sessionRepository;
    this.userRepository = userRepository;
  }

  //Endpoint para nuevo chat

  @PostMapping("/{userId}")
  @Operation(summary = "Create a new chat session")
  public ResponseEntity<ChatSession> newChat(@PathVariable UUID userId) {
    User user = userRepository
      .findById(userId)
      .orElseThrow(() -> new RuntimeException("Student id not found"));

    ChatSession newSession = ChatSession.builder()
      .user(user)
      .title("New chat with Yoko :)")
      .build();

    return ResponseEntity.ok(sessionRepository.save(newSession));
  }

  //Endpoint para enviar mensaje
  @PostMapping("/{chatId}/enviar")
  @Operation(summary = "Send a message")
  public ResponseEntity<String> sendMessage(
    @PathVariable UUID chatId,
    @RequestBody String message
  ) {
    String response = chatService.handleMessage(chatId, message);
    
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{chatId}/historial")
  @Operation(summary = "Get chat history")
  public ResponseEntity<List<Message>> getHistory(@PathVariable UUID chatId) {
    List<Message> history = chatService.recentHistory(chatId);
    return ResponseEntity.ok(history);
  }
}
