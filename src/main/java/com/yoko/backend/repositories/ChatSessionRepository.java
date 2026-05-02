package com.yoko.backend.repositories;

import com.yoko.backend.entities.ChatSession;
import java.time.LocalDateTime;
import java.util.List;
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

  @Query("SELECT COUNT(s) FROM ChatSession s WHERE s.createdAt >= :since")
  long countSessionsSince(@Param("since") LocalDateTime since);

  //Filtro por organizacion

  @Query(
    "SELECT COUNT(s) FROM ChatSession s WHERE s.organization.id = :orgId AND s.createdAt >= :since"
  )
  long countSessionsSinceByOrg(
    @Param("orgId") UUID orgId,
    @Param("since") LocalDateTime since
  );

  List<ChatSession> findByUserIdAndOrganizationId(UUID userId, UUID orgId);

  void deleteByOrganizationId(UUID orgId);

  Long countByOrganizationId(UUID orgId);
}

// findByStudentIdOrderByCreatedAtDesc old function
