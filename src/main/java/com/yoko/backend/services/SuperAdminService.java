package com.yoko.backend.services;

import com.yoko.backend.DTOs.*;
import com.yoko.backend.entities.Organization;
import com.yoko.backend.entities.User;
import com.yoko.backend.entities.UserRole;
import com.yoko.backend.repositories.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Lógica de negocio del Super Admin.
 *
 * Todos los métodos operan SIN filtro de organización — tienen acceso global.
 * Por eso cada método loguea explícitamente quién hizo qué (auditoría).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SuperAdminService {

  private static final List<String> VALID_PLANS = List.of(
    "FREE",
    "PRO",
    "ENTERPRISE"
  );

  private final OrganizationRepository organizationRepository;
  private final UserRepository userRepository;
  private final ChatSessionRepository chatSessionRepository;
  private final MessageRepository messageRepository;
  private final YokoDocumentRepository yokoDocumentRepository;
  private final JwtService jwtService;

  // ─── ORGANIZACIONES ───────────────────────────────────────────────────────

  /**
   * Lista todas las organizaciones con sus métricas agregadas.
   * Paginado para no traer todo a memoria.
   */
  public Page<OrgDetailDTO> listAllOrganizations(Pageable pageable) {
    return organizationRepository
      .findAll(pageable)
      .map(org -> {
        OrgDetailDTO dto = OrgDetailDTO.fromOrg(org);
        dto.setTotalUsers(userRepository.countByOrganizationId(org.getId()));
        dto.setTotalDocuments(
          yokoDocumentRepository.countByOrganizationId(org.getId())
        );
        dto.setTotalMessages(
          messageRepository.countByChatSessionOrganizationId(org.getId())
        );
        dto.setActiveSessions(
          chatSessionRepository.countSessionsSinceByOrg(
            org.getId(),
            LocalDateTime.now().minusHours(24)
          )
        );
        return dto;
      });
  }

  /**
   * Detalle de una organización específica.
   */
  public OrgDetailDTO getOrganizationDetail(UUID orgId) {
    Organization org = findOrgOrThrow(orgId);
    OrgDetailDTO dto = OrgDetailDTO.fromOrg(org);
    dto.setTotalUsers(userRepository.countByOrganizationId(orgId));
    dto.setTotalDocuments(yokoDocumentRepository.countByOrganizationId(orgId));
    dto.setTotalMessages(
      messageRepository.countByChatSessionOrganizationId(orgId)
    );
    dto.setActiveSessions(
      chatSessionRepository.countSessionsSinceByOrg(
        orgId,
        LocalDateTime.now().minusHours(24)
      )
    );
    return dto;
  }

  // ─── ACTIVAR / DESACTIVAR ORGANIZACIÓN ───────────────────────────────────

  /**
   * Cambia el estado activo/inactivo de una organización.
   * Desactivar una org bloquea el login de todos sus usuarios (User.isEnabled() revisa status).
   *
   * IMPORTANTE: No elimina ni modifica usuarios — solo cambia el flag de la org.
   * Los usuarios quedan bloqueados mientras la org esté inactiva.
   */
  @Transactional
  public OrgDetailDTO setOrganizationActive(
    UUID orgId,
    boolean active,
    String superAdminUsername
  ) {
    Organization org = findOrgOrThrow(orgId);

    if (org.isActive() == active) {
      String state = active ? "activa" : "inactiva";
      throw new ResponseStatusException(
        HttpStatus.CONFLICT,
        "La organización ya está " + state
      );
    }

    org.setActive(active);
    organizationRepository.save(org);

    log.warn(
      "[SUPER_ADMIN] {} {} la organización '{}' ({})",
      superAdminUsername,
      active ? "ACTIVÓ" : "DESACTIVÓ",
      org.getName(),
      orgId
    );

    return getOrganizationDetail(orgId);
  }

  // ─── CAMBIO DE PLAN ───────────────────────────────────────────────────────

  /**
   * Cambia el plan de una organización.
   * Planes válidos: FREE, PRO, ENTERPRISE.
   */
  @Transactional
  public OrgDetailDTO changePlan(
    UUID orgId,
    String newPlan,
    String superAdminUsername
  ) {
    String planUpper = newPlan.toUpperCase();
    if (!VALID_PLANS.contains(planUpper)) {
      throw new ResponseStatusException(
        HttpStatus.BAD_REQUEST,
        "Plan inválido. Opciones: " + String.join(", ", VALID_PLANS)
      );
    }

    Organization org = findOrgOrThrow(orgId);
    String oldPlan = org.getPlan();
    org.setPlan(planUpper);
    organizationRepository.save(org);

    log.info(
      "[SUPER_ADMIN] {} cambió el plan de '{}' de {} → {}",
      superAdminUsername,
      org.getName(),
      oldPlan,
      planUpper
    );

    return getOrganizationDetail(orgId);
  }

  // ─── USUARIOS GLOBALES ────────────────────────────────────────────────────

  /**
   * Lista todos los usuarios del sistema, sin importar la organización.
   * Incluye nombre de la org para contexto.
   */
  public Page<UserDTO> listAllUsers(Pageable pageable) {
    return userRepository.findAll(pageable).map(UserDTO::fromUser);
  }

  /**
   * Usuarios de una organización específica.
   */
  public List<UserDTO> listUsersByOrg(UUID orgId) {
    findOrgOrThrow(orgId); // valida que la org exista
    return userRepository
      .findByOrganizationId(orgId)
      .stream()
      .map(UserDTO::fromUser)
      .collect(Collectors.toList());
  }

  // ─── IMPERSONACIÓN ────────────────────────────────────────────────────────

  /**
   * Genera un token de corta duración (30 min) que actúa como el admin de una org.
   *
   * Diseño de seguridad:
   * - Solo puede impersonar usuarios con rol ADMIN (no a otros SUPER_ADMIN ni usuarios normales).
   * - El token tiene el claim "impersonatedBy" para trazabilidad en logs.
   * - Duración fija de 30 minutos — no configurable por el caller.
   *
   * @param orgId    UUID de la organización cuyo admin se quiere impersonar
   * @param superAdminUsername username del super admin que solicita la impersonación
   */
  public ImpersonateResponse impersonateOrgAdmin(
    UUID orgId,
    String superAdminUsername
  ) {
    Organization org = findOrgOrThrow(orgId);

    // Busca el primer admin activo de la organización
    User adminToImpersonate = userRepository
      .findByOrganizationId(orgId)
      .stream()
      .filter(
        u ->
          u.getRole() == UserRole.ADMIN &&
          "ACTIVE".equalsIgnoreCase(u.getStatus())
      )
      .findFirst()
      .orElseThrow(() ->
        new ResponseStatusException(
          HttpStatus.NOT_FOUND,
          "No hay ningún admin activo en la organización '" +
            org.getName() +
            "'"
        )
      );

    String impersonationToken = jwtService.generateImpersonationToken(
      adminToImpersonate.getEmail(),
      superAdminUsername
    );

    log.warn(
      "[SUPER_ADMIN] {} está IMPERSONANDO al admin '{}' de la org '{}' ({})",
      superAdminUsername,
      adminToImpersonate.getEmail(),
      org.getName(),
      orgId
    );

    return ImpersonateResponse.builder()
      .token(impersonationToken)
      .impersonatedEmail(adminToImpersonate.getEmail())
      .impersonatedOrgId(orgId)
      .impersonatedOrgName(org.getName())
      .warning(
        "Este token expira en 30 minutos. Toda acción queda registrada en auditoría."
      )
      .build();
  }

  // ─── ESTADÍSTICAS GLOBALES ────────────────────────────────────────────────

  /**
   * Métricas globales del sistema — sin filtro de organización.
   */
  public GlobalStatsResponse getGlobalStats() {
    long totalOrgs = organizationRepository.count();
    long activeOrgs = organizationRepository.countByActiveTrue();
    long totalUsers = userRepository.count();
    long totalMessages = messageRepository.count();
    long totalDocs = yokoDocumentRepository.count();
    long activeSessions = chatSessionRepository.countSessionsSince(
      LocalDateTime.now().minusHours(24)
    );

    // Mensajes de los últimos 7 días (global)
    LocalDateTime last7Days = LocalDateTime.now().minusDays(7);
    List<Object[]> rawCounts = messageRepository.countMessagesPerDayFrom(
      last7Days
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
      messagesLastWeek.add(
        countByDay.getOrDefault(LocalDate.now().minusDays(i), 0L)
      );
    }

    
    return GlobalStatsResponse.builder()
      .totalOrganizations(totalOrgs)
      .activeOrganizations(activeOrgs)
      .totalUsers(totalUsers)
      .totalMessages(totalMessages)
      .totalDocuments(totalDocs)
      .activeSessionsLast24h(activeSessions)
      .messagesLastWeek(messagesLastWeek)
      .topQuestionsGlobal(List.of()) // Extensión futura
      .build();
  }

  // ─── HELPER ──────────────────────────────────────────────────────────────

  private Organization findOrgOrThrow(UUID orgId) {
    return organizationRepository
      .findById(orgId)
      .orElseThrow(() ->
        new ResponseStatusException(
          HttpStatus.NOT_FOUND,
          "Organización no encontrada: " + orgId
        )
      );
  }
}
