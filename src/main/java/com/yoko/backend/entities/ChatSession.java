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
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "chat_sessions")
public class ChatSession {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;


  // ✅ Nullable — guests no tienen usuario registrado
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = true)
  @JsonIgnore
  private User user;

  // ✅ Columna dedicada para identificar guests
  @Column(name = "guest_id")
  private UUID guestId;

  private String title;

  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @OneToMany(
    mappedBy = "chatSession",
    cascade = CascadeType.ALL,
    orphanRemoval = true
  )
  private List<Message> messages;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "organization_id", nullable = false)
  private Organization organization;

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
  }
}