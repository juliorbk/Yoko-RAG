package com.yoko.backend.repositories;

import com.yoko.backend.entities.Message;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {
  List<Message> findByChatSessionIdOrderByCreatedAtAsc(UUID chatSessionId);
}
