package com.yoko.backend.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

/**
 * Represents a single message within a chat session.
 *
 * <p>Each message belongs to a {@link ChatSession} and carries a role
 * (e.g., USER or ASSISTANT) to distinguish who sent it. Messages are
 * immutable after creation — the {@code createdAt} timestamp is set
 * automatically and cannot be updated.
 *
 * <p>Mapped to the {@code messages} table in the database.
 */
@Entity
@Table(name = "messages")
@Data // Lombok: generates getters, setters, equals, hashCode, toString
@NoArgsConstructor // Lombok: required by JPA (no-arg constructor)
@AllArgsConstructor // Lombok: constructor with all fields (used with @Builder)
@Builder // Lombok: enables the builder pattern → Message.builder().content("...").build()
@EqualsAndHashCode(of = "id")
public class Message {

  /**
   * Unique identifier for this message.
   * Auto-generated as a UUID by the database on insert.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  /**
   * The text content of the message.
   * Stored as TEXT in the DB to support long messages without length limits.
   * Cannot be null.
   */
  @Column(columnDefinition = "TEXT", nullable = false)
  private String content;

  /**
   * The role of the sender — typically USER or ASSISTANT.
   * Stored as a readable string (e.g., "USER") rather than an ordinal number,
   * so the DB stays human-readable even if the enum order changes.
   */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private MessageRole role;

  /**
   * Timestamp of when this message was created.
   * Set automatically in {@link #onCreate()} before the first DB insert.
   * Marked as non-updatable so it can never be overwritten after creation.
   */
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  /**
   * The chat session this message belongs to.
   *
   * <p>Many messages can belong to one session (Many-to-One).
   * Loaded lazily — the session data is only fetched from the DB when
   * explicitly accessed, keeping queries efficient.
   *
   * <p>The FK column {@code chat_session_id} cannot be null, meaning every
   * message must be associated with a session.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "chat_session_id", nullable = false)
  @JsonIgnore
  private ChatSession chatSession;

  /**
   * JPA lifecycle hook — runs automatically just before this entity is
   * persisted to the database for the first time.
   *
   * <p>Sets {@code createdAt} to the current date/time so the application
   * never needs to set it manually.
   */
  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
  }
}
