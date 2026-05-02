package com.yoko.backend.DTOs;

import java.util.List;
import lombok.*;

/**
 * Snapshot global del sistema — equivalente al StatsResponse pero sin filtro de org.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GlobalStatsResponse {

  private long totalOrganizations;
  private long activeOrganizations;
  private long totalUsers;
  private long totalMessages;
  private long totalDocuments;
  private long activeSessionsLast24h;

  private List<Long> messagesLastWeek; // [lun, mar, mié, jue, vie, sáb, dom]
  private List<TopQuestionDTO> topQuestionsGlobal;
}
