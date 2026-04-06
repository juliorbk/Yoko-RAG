package com.yoko.backend.repositories;

import com.yoko.backend.entities.ChatSession;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatSessionRepository
  extends JpaRepository<ChatSession, UUID>
{
  Page<ChatSession> findByUserIdOrderByCreatedAtDesc(
    UUID UserId,
    Pageable pageable
  );
}
// findByStudentIdOrderByCreatedAtDesc old function
