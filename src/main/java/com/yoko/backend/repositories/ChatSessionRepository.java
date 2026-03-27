package com.yoko.backend.repositories;

import com.yoko.backend.entities.ChatSession;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatSessionRepository
  extends JpaRepository<ChatSession, UUID>
{
  List<ChatSession> findByUserIdOrderByCreatedAtDesc(UUID UserId);
}
// findByStudentIdOrderByCreatedAtDesc old function
