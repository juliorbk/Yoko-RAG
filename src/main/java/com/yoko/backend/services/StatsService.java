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
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class StatsService {

  //Inyectamos los repositorios necesarios
  private final ChatSessionRepository chatSessionRepository;
  private final MessageRepository messageRepository;
  private final UserRepository userRepository;
  private YokoDocumentRepository yokoDocumentRepository;

  public StatsService(
    ChatSessionRepository chatSessionRepository,
    MessageRepository messageRepository,
    UserRepository userRepository,
    YokoDocumentRepository yokoDocumentRepository
  ) {
    this.chatSessionRepository = chatSessionRepository;
    this.messageRepository = messageRepository;
    this.userRepository = userRepository;
    this.yokoDocumentRepository = yokoDocumentRepository;
  }

  //Estadisticas generales
  public StatsResponse buildStats() {
    //Conteos sencillos
    long totalUsers = userRepository.count();
    long totalMessages = messageRepository.count();

    //Conteos del día actual
    LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
    long activeSessions = chatSessionRepository.countSessionsSince(last24Hours);

    //Conteos de la semana actual
    LocalDateTime last7Days = LocalDateTime.now().minusDays(7);

    List<Object[]> rawCounts = messageRepository.countMessagesPerDayFrom(
      last7Days
    );

    // Construir un mapa fecha → conteo para rellenar días sin mensajes con 0
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

    long totalDocuments = yokoDocumentRepository.count();

    List<TopQuestionDTO> topQuestions = buildTopQuestions();

    return new StatsResponse(
      totalUsers,
      activeSessions,
      totalMessages,
      totalDocuments,
      messagesLastWeek,
      topQuestions
    );
  }

  /**
   * Construye una lista de objetos TopQuestionDTO que contienen la frecuencia de las preguntas
   * más comunes en el chat de la UNEG. La frecuencia se calcula a partir de los
   * primeros mensajes de usuario de cada sesión, normalizando a minúsculas y
   * trim. La lista se ordena descendente por frecuencia y se toman los 5 primeros
   * elementos.
   * @return una lista de objetos TopQuestionDTO que contiene la frecuencia de las preguntas
   * más comunes en el chat de la UNEG
   */
  private List<TopQuestionDTO> buildTopQuestions() {
    // Trae todos los primeros mensajes de usuario de cada sesión
    List<Object[]> firstMessages =
      messageRepository.findFirstUserMessagePerSession();

    // Agrupa por texto exacto y cuenta (normaliza a minúsculas y trim)
    Map<String, Long> freq = firstMessages
      .stream()
      .map(row -> ((String) row[0]).trim().toLowerCase())
      .collect(Collectors.groupingBy(text -> text, Collectors.counting()));

    // Ordena descendente y toma top 5
    return freq
      .entrySet()
      .stream()
      .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
      .limit(5)
      .map(e -> new TopQuestionDTO(e.getValue(), e.getKey()))
      .collect(Collectors.toList());
  }
}
