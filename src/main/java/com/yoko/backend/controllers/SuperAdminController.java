package com.yoko.backend.controllers;

import com.yoko.backend.DTOs.*;
import com.yoko.backend.services.SuperAdminAuthService;
import com.yoko.backend.services.SuperAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints exclusivos del Super Admin.
 *
 * Todos bajo /api/super — protegidos con hasAuthority("SUPER_ADMIN") en SecurityConfig.
 * El principal se extrae del JWT via el filtro JwtAuthenticationFilter estándar,
 * que para tokens de super admin pone el username (no el email) como subject.
 *
 * NOTA: Principal.getName() devuelve el subject del JWT, que para super admin es su username.
 */
@RestController
@RequestMapping("/api/super")
@RequiredArgsConstructor
@Tag(
  name = "Super Admin",
  description = "Panel de administración global del sistema"
)
public class SuperAdminController {

  private final SuperAdminAuthService superAdminAuthService;
  private final SuperAdminService superAdminService;

  // ─── AUTENTICACIÓN ────────────────────────────────────────────────────────

  @PostMapping("/login")
  @Operation(summary = "Login exclusivo del Super Admin")
  public ResponseEntity<Map<String, String>> login(
    @Valid @RequestBody SuperAdminLoginRequest request
  ) {
    String token = superAdminAuthService.login(request);

    return ResponseEntity.ok(Map.of("token", token));
  }

  // ─── ESTADÍSTICAS GLOBALES ────────────────────────────────────────────────

  @GetMapping("/stats")
  @Operation(summary = "Métricas globales del sistema (todas las orgs)")
  public ResponseEntity<GlobalStatsResponse> getGlobalStats() {
    return ResponseEntity.ok(superAdminService.getGlobalStats());
  }

  // ─── ORGANIZACIONES ───────────────────────────────────────────────────────

  @GetMapping("/organizations")
  @Operation(summary = "Lista todas las organizaciones con métricas")
  public ResponseEntity<Page<OrgDetailDTO>> listOrgs(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size
  ) {
    Pageable pageable = PageRequest.of(page, size);
    return ResponseEntity.ok(superAdminService.listAllOrganizations(pageable));
  }

  @GetMapping("/organizations/{orgId}")
  @Operation(summary = "Detalle de una organización específica")
  public ResponseEntity<OrgDetailDTO> getOrg(@PathVariable UUID orgId) {
    return ResponseEntity.ok(superAdminService.getOrganizationDetail(orgId));
  }

  @PatchMapping("/organizations/{orgId}/activate")
  @Operation(summary = "Activa una organización")
  public ResponseEntity<OrgDetailDTO> activateOrg(
    @PathVariable UUID orgId,
    Principal principal
  ) {
    return ResponseEntity.ok(
      superAdminService.setOrganizationActive(orgId, true, principal.getName())
    );
  }

  @PatchMapping("/organizations/{orgId}/deactivate")
  @Operation(
    summary = "Desactiva una organización (bloquea a todos sus usuarios)"
  )
  public ResponseEntity<OrgDetailDTO> deactivateOrg(
    @PathVariable UUID orgId,
    Principal principal
  ) {
    return ResponseEntity.ok(
      superAdminService.setOrganizationActive(orgId, false, principal.getName())
    );
  }

  @PatchMapping("/organizations/{orgId}/plan")
  @Operation(
    summary = "Cambia el plan de una organización (FREE / PRO / ENTERPRISE)"
  )
  public ResponseEntity<OrgDetailDTO> changePlan(
    @PathVariable UUID orgId,
    @Valid @RequestBody ChangePlanRequest request,
    Principal principal
  ) {
    return ResponseEntity.ok(
      superAdminService.changePlan(
        orgId,
        request.getPlan(),
        principal.getName()
      )
    );
  }

  // ─── USUARIOS GLOBALES ────────────────────────────────────────────────────

  @GetMapping("/users")
  @Operation(summary = "Lista todos los usuarios del sistema (paginado)")
  public ResponseEntity<Page<UserDTO>> listAllUsers(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "50") int size
  ) {
    Pageable pageable = PageRequest.of(page, size);
    return ResponseEntity.ok(superAdminService.listAllUsers(pageable));
  }

  @GetMapping("/organizations/{orgId}/users")
  @Operation(summary = "Lista los usuarios de una organización específica")
  public ResponseEntity<java.util.List<UserDTO>> listOrgUsers(
    @PathVariable UUID orgId
  ) {
    return ResponseEntity.ok(superAdminService.listUsersByOrg(orgId));
  }

  // ─── IMPERSONACIÓN ────────────────────────────────────────────────────────

  @PostMapping("/organizations/{orgId}/impersonate")
  @Operation(
    summary = "Genera un token de impersonación del admin de la org",
    description = "Token válido por 30 minutos. Toda acción queda en auditoría."
  )
  public ResponseEntity<ImpersonateResponse> impersonate(
    @PathVariable UUID orgId,
    Principal principal
  ) {
    return ResponseEntity.ok(
      superAdminService.impersonateOrgAdmin(orgId, principal.getName())
    );
  }
}
