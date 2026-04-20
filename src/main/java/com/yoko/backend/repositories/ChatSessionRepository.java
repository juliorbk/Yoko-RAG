package com.yoko.backend.repositories;

import com.yoko.backend.entities.ChatSession;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatSessionRepository
  extends JpaRepository<ChatSession, UUID>
{
  Page<ChatSession> findByUserIdOrderByCreatedAtDesc(
    UUID UserId,
    Pageable pageable
  );

  // ChatSessions del dia actual
  @Query("SELECT COUNT(s) FROM ChatSession s WHERE s.createdAt >= :since")
  long countSessionsSince(@Param("since") LocalDateTime since);
}
// findByStudentIdOrderByCreatedAtDesc old function
