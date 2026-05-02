package com.yoko.backend.controllers;

import com.yoko.backend.DTOs.ChatSessionDTO;
import com.yoko.backend.DTOs.MessageDTO;
import com.yoko.backend.DTOs.WidgetSessionRequest;
import com.yoko.backend.entities.ChatSession;
import com.yoko.backend.entities.Message;
import com.yoko.backend.entities.User;
import com.yoko.backend.services.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

  //Inyeccion de dependencias
  public ChatController(ChatService chatService) {
    this.chatService = chatService;
  }

  //Endpoint para nuevo chat

  // ─── Endpoints ────────────────────────────────────────────────────────────

  @PostMapping("/{userId}")
  @Operation(summary = "Create a new chat session")
  public ResponseEntity<ChatSessionDTO> newChat(
    @AuthenticationPrincipal User currentUser
  ) {
    ChatSession newSession = chatService.createChatSession(currentUser.getId());
    return ResponseEntity.ok(ChatSessionDTO.from(newSession));
  }
  @PostMapping("/widget")
  @Operation(summary = "Create a new chat session in a widget")
  public ResponseEntity<ChatSessionDTO> newChat(
    @Valid @RequestBody WidgetSessionRequest widgetRequest
  ) {
    ChatSession newSession = chatService.createWidgetSession(widgetRequest.getOrganizationSlug());
    return ResponseEntity.ok(ChatSessionDTO.from(newSession));
  }

  @PostMapping("/{chatId}/messages")
  @Operation(summary = "Send a message")
  public ResponseEntity<String> sendMessage(
    @PathVariable UUID chatId,
    @RequestBody String message,
    @AuthenticationPrincipal User currentUser
  ) {
    String response = chatService.handleMessage(chatId, message, currentUser);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/widget/{sessionId}/message")
  @Operation(summary = "Send a message from widget (anonymous)")
  public ResponseEntity<String> sendWidgetMessage(
    @PathVariable UUID sessionId,
    @RequestBody String message
  ) {
    String response = chatService.handleWidgetMessage(sessionId, message);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{chatId}")
  @Operation(summary = "Get chat history")
  public ResponseEntity<List<MessageDTO>> getHistory(
    @PathVariable @NonNull UUID chatId,
    @AuthenticationPrincipal User currentUser
  ) {
    List<MessageDTO> history = chatService
      .recentHistory(chatId, currentUser.getId())
      .stream()
      .map(MessageDTO::from)
      .toList();
    return ResponseEntity.ok(history);
  }

  @GetMapping("/my-chats")
  @Operation(summary = "Get user's chats")
  public ResponseEntity<Page<ChatSessionDTO>> getChats(
    @AuthenticationPrincipal User currentUser,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size
  ) {
    Pageable pageable = PageRequest.of(page, size);
    Page<ChatSession> raw = chatService.getUserChats(
      currentUser.getId(),
      pageable
    );
    Page<ChatSessionDTO> dto = new PageImpl<>(
      raw.getContent().stream().map(ChatSessionDTO::from).toList(),
      pageable,
      raw.getTotalElements()
    );
    return ResponseEntity.ok(dto);
  }

  @DeleteMapping("/{chatId}")
  @Operation(summary = "Delete a chat session")
  public ResponseEntity<Void> deleteChat(
    @PathVariable @NotNull UUID chatId,
    @AuthenticationPrincipal User currentUser
  ) {
    chatService.deleteChatSession(chatId, currentUser.getId());
    return ResponseEntity.ok().build();
  }


  
}
