package com.yoko.backend.services;

import com.yoko.backend.DTOs.StatsResponse;
import com.yoko.backend.DTOs.TopQuestionDTO;
import com.yoko.backend.repositories.ChatSessionRepository;
import com.yoko.backend.repositories.MessageRepository;
import com.yoko.backend.repositories.UserRepository;
import com.yoko.backend.repositories.YokoDocumentRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatsService {

  private final ChatSessionRepository chatSessionRepository;
  private final MessageRepository messageRepository;
  private final UserRepository userRepository;
  private final YokoDocumentRepository yokoDocumentRepository;

  // ✅ Recibe orgId en lugar de contar todo
  public StatsResponse buildStats(UUID orgId) {
    long totalUsers = userRepository.countByOrganizationId(orgId);
    long totalMessages = messageRepository.countByChatSessionOrganizationId(
      orgId
    );

    LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
    long activeSessions = chatSessionRepository.countSessionsSinceByOrg(
      orgId,
      last24Hours
    );

    LocalDateTime last7Days = LocalDateTime.now().minusDays(7);
    List<Object[]> rawCounts = messageRepository.countMessagesPerDayFromByOrg(
      last7Days,
      orgId
    );

    Map<LocalDate, Long> countByDay = rawCounts
      .stream()
      .collect(
        Collectors.toMap(
          row -> ((java.sql.Date) row[0]).toLocalDate(),
          row -> (Long) row[1]
        )
      );

    List<Long> messagesLastWeek = new ArrayList<>();
    for (int i = 6; i >= 0; i--) {
      LocalDate day = LocalDate.now().minusDays(i);
      messagesLastWeek.add(countByDay.getOrDefault(day, 0L));
    }

    long totalDocuments = yokoDocumentRepository.countByOrganizationId(orgId);
    List<TopQuestionDTO> topQuestions = buildTopQuestions(orgId);

    return new StatsResponse(
      totalUsers,
      activeSessions,
      totalMessages,
      totalDocuments,
      messagesLastWeek,
      topQuestions
    );
  }

  private List<TopQuestionDTO> buildTopQuestions(UUID orgId) {
    List<Object[]> firstMessages =
      messageRepository.findFirstUserMessagePerSessionByOrg(orgId);

    Map<String, Long> freq = firstMessages
      .stream()
      .map(row -> ((String) row[0]).trim().toLowerCase())
      .collect(Collectors.groupingBy(text -> text, Collectors.counting()));

    return freq
      .entrySet()
      .stream()
      .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
      .limit(5)
      .map(e -> new TopQuestionDTO(e.getValue(), e.getKey()))
      .collect(Collectors.toList());
  }
}
