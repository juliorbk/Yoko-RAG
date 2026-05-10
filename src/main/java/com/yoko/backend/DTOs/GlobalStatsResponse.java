package com.yoko.backend.DTOs;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Snapshot global del sistema — equivalente al StatsResponse pero sin filtro de org.
 */
@Getter
@Setter
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
