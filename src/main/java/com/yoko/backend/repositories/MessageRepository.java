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

  // Mensajes del dia actual
  @Query(
    """
    SELECT CAST(m.createdAt AS date) as day, COUNT(m) as total
    FROM Message m
    WHERE m.createdAt >= :since
    GROUP BY CAST(m.createdAt AS date)
    ORDER BY CAST(m.createdAt AS date) ASC
    """
  )
  List<Object[]> countMessagesPerDayFrom(@Param("since") LocalDateTime since);

  /**
   * Finds the first message sent by each user in each chat session.
   * Returns a list of objects containing the message content.
   * @return a list of objects containing the message content
   */
  @Query(
    """
    SELECT m.content
    FROM Message m
    WHERE m.role = 'USER'
      AND m.createdAt = (
          SELECT MIN(m2.createdAt)
          FROM Message m2
          WHERE m2.chatSession.id = m.chatSession.id
            AND m2.role = 'USER'
      )
    """
  )
  List<Object[]> findFirstUserMessagePerSession();
}
