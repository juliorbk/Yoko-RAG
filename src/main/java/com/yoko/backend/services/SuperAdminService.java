// package com.yoko.backend.services;

// import com.yoko.backend.DTOs.StatsResponse;
// import com.yoko.backend.DTOs.TopQuestionDTO;
// import com.yoko.backend.repositories.ChatSessionRepository;
// import com.yoko.backend.repositories.MessageRepository;
// import com.yoko.backend.repositories.OrganizationRepository;

// import com.yoko.backend.repositories.YokoDocumentRepository;
// import java.time.LocalDate;
// import java.time.LocalDateTime;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.Map;
// import java.util.stream.Collectors;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.stereotype.Service;

// @Service
// @Slf4j
// public class SuperAdminService {

//   //Inyectamos los repositorios necesarios
//   private final ChatSessionRepository chatSessionRepository;
//   private final MessageRepository messageRepository;
//   private final OrganizationRepository organizationRepository;
//   private YokoDocumentRepository yokoDocumentRepository;

//   public SuperAdminService(
//     ChatSessionRepository chatSessionRepository,
//     MessageRepository messageRepository,
//     OrganizationRepository organizationRepository,
//     YokoDocumentRepository yokoDocumentRepository
//   ) {
//     this.chatSessionRepository = chatSessionRepository;
//     this.messageRepository = messageRepository;
//     this.organizationRepository = organizationRepository;
//     this.yokoDocumentRepository = yokoDocumentRepository;
//   }

//   //Estadisticas generales
//   public StatsResponse buildAdminStats() {
//     //Conteos sencillos
//     long totalOrganizations = organizationRepository.count();
//     long totalMessages = messageRepository.count();

//     //Conteos del día actual
//     LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
//     long activeSessions = chatSessionRepository.countSessionsSince(last24Hours);

//     //Conteos de la semana actual
//     LocalDateTime last7Days = LocalDateTime.now().minusDays(7);

//     List<Object[]> rawCounts = messageRepository.countMessagesPerDayFrom(
//       last7Days
//     );

//     // Construir un mapa fecha → conteo para rellenar días sin mensajes con 0
//     Map<LocalDate, Long> countByDay = rawCounts
//       .stream()
//       .collect(
//         Collectors.toMap(
//           row -> ((java.sql.Date) row[0]).toLocalDate(),
//           row -> (Long) row[1]
//         )
//       );

//     List<Long> messagesLastWeek = new ArrayList<>();
//     for (int i = 6; i >= 0; i--) {
//       LocalDate day = LocalDate.now().minusDays(i);
//       messagesLastWeek.add(countByDay.getOrDefault(day, 0L));
//     }

//     long totalDocuments = yokoDocumentRepository.count();

//     return new StatsResponse(
//       totalOrganizations,
//       activeSessions,
//       totalMessages,
//       totalDocuments,
//       messagesLastWeek
//     );
//   }
// }
