package com.yoko.backend.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.*;

/**
 * Represents a chat session initiated by a Student.
 *
 * A chat session acts as a container for a conversation thread — it groups
 * a series of Message objects under a single titled session, tied to one student.
 *
 * Deleting a session automatically deletes all of its messages
 * via CascadeType.ALL and orphanRemoval = true.
 *
 * Mapped to the "chat_sessions" table in the database.
 */
@Entity
@Table(name = "chat_sessions")
@Data // Lombok: generates getters, setters, equals, hashCode, toString
@NoArgsConstructor // Lombok: required by JPA (no-arg constructor)
@AllArgsConstructor // Lombok: constructor with all fields (used with @Builder)
@Builder // Lombok: enables → ChatSession.builder().title("...").build()
@EqualsAndHashCode(of = "id")
public class ChatSession {

  /**
   * Unique identifier for this chat session.
   * Auto-generated as a UUID by the database on insert.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  /**
   * A human-readable title for the session (e.g., "Math Homework Help").
   * Optional — no nullable = false constraint is enforced, so sessions
   * can exist without a title (e.g., before the user names them).
   */
  private String title;

  /**
   * Timestamp of when this session was created.
   * Set automatically by onCreate() on first persist.
   * Non-updatable — once written, it cannot be changed.
   */
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  /**
   * The student who owns this chat session.
   *
   * Many sessions can belong to one student (Many-to-One).
   * Loaded lazily to avoid fetching the full student record on every
   * session query.
   *
   * The FK column "user_id" is required — every session
   * must be associated with a student.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  @JsonIgnore
  private User user;

  /**
   * The list of messages that belong to this session.
   *
   * One session has many messages (One-to-Many), mapped by the
   * "chatSession" field on the Message entity.
   *
   * CascadeType.ALL — any operation on the session (persist, merge,
   * remove, etc.) is automatically propagated to its messages.
   * Saving a session saves its messages too.
   *
   * orphanRemoval = true — if a message is removed from this list,
   * it is automatically deleted from the DB, even without calling
   * remove() explicitly.
   */
  @OneToMany(
    mappedBy = "chatSession", // FK lives on the Message side
    cascade = CascadeType.ALL, // Propagate all operations to child messages
    orphanRemoval = true // Delete messages removed from this list
  )
  private List<Message> messages;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "organization_id", nullable = false)
  private Organization organization;

  /**
   * JPA lifecycle hook — runs automatically just before this entity is
   * first persisted to the database.
   *
   * Sets createdAt to the current date/time, so callers using the
   * builder never need to set it manually.
   */
  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
  }
}
