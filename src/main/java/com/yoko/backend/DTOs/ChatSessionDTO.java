package com.yoko.backend.DTOs;

import com.yoko.backend.entities.ChatSession;
import java.time.LocalDateTime;
import java.util.UUID;

public record ChatSessionDTO(UUID id, String title, LocalDateTime createdAt) {
  public static ChatSessionDTO from(ChatSession s) {
    return new ChatSessionDTO(s.getId(), s.getTitle(), s.getCreatedAt());
  }
}
