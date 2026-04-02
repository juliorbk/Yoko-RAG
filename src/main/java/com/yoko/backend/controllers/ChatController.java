package com.yoko.backend.controllers;

import com.yoko.backend.entities.ChatSession;
import com.yoko.backend.entities.Message;
import com.yoko.backend.repositories.ChatSessionRepository;
import com.yoko.backend.repositories.UserRepository;
import com.yoko.backend.services.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sessions")
@Tag(name = "Chat")
public class ChatController {

  private final ChatService chatService;
  private final ChatSessionRepository sessionRepository;

  //Inyeccion de dependencias
  public ChatController(
    ChatService chatService,
    ChatSessionRepository sessionRepository
  ) {
    this.chatService = chatService;
    this.sessionRepository = sessionRepository;
  }

  //Endpoint para nuevo chat

  @PostMapping("/{userId}")
  @Operation(summary = "Create a new chat session")
  public ResponseEntity<ChatSession> newChat(@PathVariable UUID userId) {
    ChatSession newSession = chatService.createChatSession(userId);

    return ResponseEntity.ok(newSession);
  }

  //Endpoint para enviar mensaje
  @PostMapping("/{chatId}/messages")
  @Operation(summary = "Send a message")
  public ResponseEntity<String> sendMessage(
    @PathVariable UUID chatId,
    @RequestBody String message
  ) {
    String response = chatService.handleMessage(chatId, message);

    return ResponseEntity.ok(response);
  }

  @GetMapping("/{chatId}")
  @Operation(summary = "Get chat history")
  public ResponseEntity<List<Message>> getHistory(@PathVariable UUID chatId) {
    List<Message> history = chatService.recentHistory(chatId);
    return ResponseEntity.ok(history);
  }

  @GetMapping("/{userId}/chats")
  @Operation(summary = "Get user's chats")
  public ResponseEntity<Page<ChatSession>> getChats(
    @PathVariable UUID userId,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size
  ) {
    Pageable pageable = PageRequest.of(page, size);
    return ResponseEntity.ok(chatService.getUserChats(userId, pageable));
  }

  @DeleteMapping("/{chatId}")
  @Operation(summary = "Delete a chat session")
  public ResponseEntity<?> deleteChat(@PathVariable @NotNull UUID chatId) {
    sessionRepository.deleteById(chatId);
    return ResponseEntity.ok().build();
  }
}
