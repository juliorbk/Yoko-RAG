package com.yoko.backend.repositories;

import com.yoko.backend.entities.Message;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {
  List<Message> findByChatSessionIdOrderByCreatedAtAsc(UUID chatSessionId);

  List<Message> findTopByChatSessionIdOrderByCreatedAtDesc(
    UUID sessionId,
    PageRequest of
  );

  //Mensajes por día filtrados por org
  @Query(
    """
    SELECT CAST(m.createdAt AS date) as day, COUNT(m) as total
    FROM Message m
    WHERE m.createdAt >= :since
      AND m.chatSession.organization.id = :orgId
    GROUP BY CAST(m.createdAt AS date)
    ORDER BY CAST(m.createdAt AS date) ASC
    """
  )
  List<Object[]> countMessagesPerDayFromByOrg(
    @Param("since") LocalDateTime since,
    @Param("orgId") UUID orgId
  );

  // Top preguntas filtradas por org
  @Query(
    """
    SELECT m.content
    FROM Message m
    WHERE m.role = 'USER'
      AND m.chatSession.organization.id = :orgId
      AND m.createdAt = (
          SELECT MIN(m2.createdAt)
          FROM Message m2
          WHERE m2.chatSession.id = m.chatSession.id
            AND m2.role = 'USER'
      )
    """
  )
  List<Object[]> findFirstUserMessagePerSessionByOrg(
    @Param("orgId") UUID orgId
  );

  Long countByChatSessionOrganizationId(UUID orgId);

  List<Message> findByChatSessionId(UUID chatSessionId);
}
