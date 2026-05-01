package com.yoko.backend.DTOs;

import com.yoko.backend.entities.Message;
import java.time.LocalDateTime;
import java.util.UUID;

public record MessageDTO(
  UUID id,
  String content,
  String role, // "USER" | "ASSISTANT"
  LocalDateTime createdAt
) {
  public static MessageDTO from(Message m) {
    return new MessageDTO(
      m.getId(),
      m.getContent(),
      m.getRole().name(),
      m.getCreatedAt()
    );
  }
}
