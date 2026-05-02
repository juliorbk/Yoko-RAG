package com.yoko.backend;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.yoko.backend.DTOs.*;
import com.yoko.backend.entities.*;
import com.yoko.backend.repositories.*;
import com.yoko.backend.services.JwtService;
import com.yoko.backend.services.SuperAdminService;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
@DisplayName("SuperAdminService — cobertura completa")
class SuperAdminServiceTest {

  @Mock
  OrganizationRepository organizationRepository;

  @Mock
  UserRepository userRepository;

  @Mock
  ChatSessionRepository chatSessionRepository;

  @Mock
  MessageRepository messageRepository;

  @Mock
  YokoDocumentRepository yokoDocumentRepository;

  @Mock
  JwtService jwtService;

  @InjectMocks
  SuperAdminService superAdminService;

  private Organization orgActiva;
  private Organization orgInactiva;
  private User adminActivo;

  @BeforeEach
  void setUp() {
    UUID orgId1 = UUID.randomUUID();
    UUID orgId2 = UUID.randomUUID();

    orgActiva = Organization.builder()
      .id(orgId1)
      .name("UNEG")
      .slug("uneg")
      .plan("FREE")
      .active(true)
      .sector(OrgSector.EDUCACION)
      .build();

    orgInactiva = Organization.builder()
      .id(orgId2)
      .name("Hotel Llovizna")
      .slug("hotel-llovizna")
      .plan("PRO")
      .active(false)
      .sector(OrgSector.HOSPITALIDAD)
      .build();

    adminActivo = User.builder()
      .id(UUID.randomUUID())
      .email("admin@uneg.edu.ve")
      .name("Admin UNEG")
      .role(UserRole.ADMIN)
      .status("ACTIVE")
      .organization(orgActiva)
      .build();
  }

  // ─── listAllOrganizations ─────────────────────────────────────────────────

  @Nested
  @DisplayName("listAllOrganizations()")
  class ListAllOrgs {

    @Test
    @DisplayName("debe devolver página de orgs con métricas")
    void devuelvePaginaConMetricas() {
      var pageable = PageRequest.of(0, 10);
      var page = new PageImpl<>(List.of(orgActiva, orgInactiva));
      when(organizationRepository.findAll(pageable)).thenReturn(page);
      when(userRepository.countByOrganizationId(any())).thenReturn(5L);
      when(yokoDocumentRepository.countByOrganizationId(any())).thenReturn(12L);
      when(
        messageRepository.countByChatSessionOrganizationId(any())
      ).thenReturn(300L);
      when(
        chatSessionRepository.countSessionsSinceByOrg(any(), any())
      ).thenReturn(3L);

      var result = superAdminService.listAllOrganizations(pageable);

      assertThat(result.getTotalElements()).isEqualTo(2);
      assertThat(result.getContent().get(0).getTotalUsers()).isEqualTo(5L);
      assertThat(result.getContent().get(0).getTotalMessages()).isEqualTo(300L);
    }

    @Test
    @DisplayName("página vacía cuando no hay orgs")
    void paginaVacia() {
      var pageable = PageRequest.of(0, 10);
      when(organizationRepository.findAll(pageable)).thenReturn(
        new PageImpl<>(List.of())
      );

      var result = superAdminService.listAllOrganizations(pageable);

      assertThat(result.getContent()).isEmpty();
      verifyNoInteractions(userRepository); // no debe llamar repos si no hay orgs
    }
  }

  // ─── setOrganizationActive ────────────────────────────────────────────────

  @Nested
  @DisplayName("setOrganizationActive()")
  class SetOrgActive {

    @Test
    @DisplayName("desactivar una org activa debe funcionar")
    void desactivarOrgActiva() {
      when(organizationRepository.findById(orgActiva.getId())).thenReturn(
        Optional.of(orgActiva)
      );
      when(userRepository.countByOrganizationId(any())).thenReturn(0L);
      when(yokoDocumentRepository.countByOrganizationId(any())).thenReturn(0L);
      when(
        messageRepository.countByChatSessionOrganizationId(any())
      ).thenReturn(0L);
      when(
        chatSessionRepository.countSessionsSinceByOrg(any(), any())
      ).thenReturn(0L);
      when(organizationRepository.save(any())).thenAnswer(i ->
        i.getArgument(0)
      );

      var result = superAdminService.setOrganizationActive(
        orgActiva.getId(),
        false,
        "yokoadmin"
      );

      assertThat(result.isActive()).isFalse();
      verify(organizationRepository).save(argThat(org -> !org.isActive()));
    }

    @Test
    @DisplayName("activar una org ya activa debe lanzar CONFLICT")
    void activarOrgYaActivaLanzaConflict() {
      when(organizationRepository.findById(orgActiva.getId())).thenReturn(
        Optional.of(orgActiva)
      );

      assertThatThrownBy(() ->
        superAdminService.setOrganizationActive(
          orgActiva.getId(),
          true,
          "yokoadmin"
        )
      )
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("ya está");
    }

    @Test
    @DisplayName("org no encontrada lanza 404")
    void orgNoEncontradaLanza404() {
      UUID fakeId = UUID.randomUUID();
      when(organizationRepository.findById(fakeId)).thenReturn(
        Optional.empty()
      );

      assertThatThrownBy(() ->
        superAdminService.setOrganizationActive(fakeId, false, "yokoadmin")
      )
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("no encontrada");
    }
  }

  // ─── changePlan ───────────────────────────────────────────────────────────

  @Nested
  @DisplayName("changePlan()")
  class ChangePlan {

    @Test
    @DisplayName("cambio a PRO debe persistir y devolver dto actualizado")
    void cambioAPro() {
      when(organizationRepository.findById(orgActiva.getId())).thenReturn(
        Optional.of(orgActiva)
      );
      when(organizationRepository.save(any())).thenAnswer(i ->
        i.getArgument(0)
      );
      when(userRepository.countByOrganizationId(any())).thenReturn(0L);
      when(yokoDocumentRepository.countByOrganizationId(any())).thenReturn(0L);
      when(
        messageRepository.countByChatSessionOrganizationId(any())
      ).thenReturn(0L);
      when(
        chatSessionRepository.countSessionsSinceByOrg(any(), any())
      ).thenReturn(0L);

      var result = superAdminService.changePlan(
        orgActiva.getId(),
        "pro",
        "yokoadmin"
      );

      assertThat(result.getPlan()).isEqualTo("PRO");
    }

    @Test
    @DisplayName("plan inválido debe lanzar BAD_REQUEST")
    void planInvalidoLanzaBadRequest() {
      when(organizationRepository.findById(orgActiva.getId())).thenReturn(
        Optional.of(orgActiva)
      );

      assertThatThrownBy(() ->
        superAdminService.changePlan(orgActiva.getId(), "PREMIUM", "yokoadmin")
      )
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("Plan inválido");
    }

    @Test
    @DisplayName("el plan se normaliza a mayúsculas")
    void planSeNormalizaAMayusculas() {
      when(organizationRepository.findById(orgActiva.getId())).thenReturn(
        Optional.of(orgActiva)
      );
      when(organizationRepository.save(any())).thenAnswer(i ->
        i.getArgument(0)
      );
      when(userRepository.countByOrganizationId(any())).thenReturn(0L);
      when(yokoDocumentRepository.countByOrganizationId(any())).thenReturn(0L);
      when(
        messageRepository.countByChatSessionOrganizationId(any())
      ).thenReturn(0L);
      when(
        chatSessionRepository.countSessionsSinceByOrg(any(), any())
      ).thenReturn(0L);

      superAdminService.changePlan(
        orgActiva.getId(),
        "enterprise",
        "yokoadmin"
      );

      verify(organizationRepository).save(
        argThat(org -> "ENTERPRISE".equals(org.getPlan()))
      );
    }
  }

  // ─── impersonateOrgAdmin ──────────────────────────────────────────────────

  @Nested
  @DisplayName("impersonateOrgAdmin()")
  class Impersonate {

    @Test
    @DisplayName("debe devolver token de impersonación con los datos correctos")
    void devuelveTokenDeImpersonacion() {
      when(organizationRepository.findById(orgActiva.getId())).thenReturn(
        Optional.of(orgActiva)
      );
      when(userRepository.findByOrganizationId(orgActiva.getId())).thenReturn(
        List.of(adminActivo)
      );
      when(jwtService.generateImpersonationToken(any(), any())).thenReturn(
        "impersonation-token-fake"
      );

      var result = superAdminService.impersonateOrgAdmin(
        orgActiva.getId(),
        "yokoadmin"
      );

      assertThat(result.getToken()).isEqualTo("impersonation-token-fake");
      assertThat(result.getImpersonatedEmail()).isEqualTo("admin@uneg.edu.ve");
      assertThat(result.getImpersonatedOrgName()).isEqualTo("UNEG");
      assertThat(result.getWarning()).isNotBlank();
    }

    @Test
    @DisplayName("sin admins activos en la org debe lanzar 404")
    void sinAdminsActivosLanza404() {
      // Solo hay usuarios con rol USER
      User usuarioNormal = User.builder()
        .id(UUID.randomUUID())
        .email("user@uneg.edu.ve")
        .role(UserRole.USER)
        .status("ACTIVE")
        .build();

      when(organizationRepository.findById(orgActiva.getId())).thenReturn(
        Optional.of(orgActiva)
      );
      when(userRepository.findByOrganizationId(orgActiva.getId())).thenReturn(
        List.of(usuarioNormal)
      );

      assertThatThrownBy(() ->
        superAdminService.impersonateOrgAdmin(orgActiva.getId(), "yokoadmin")
      )
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("No hay ningún admin activo");
    }

    @Test
    @DisplayName("admin inactivo no debe ser impersonado")
    void adminInactivoNoDebeSerImpersonado() {
      User adminInactivo = User.builder()
        .id(UUID.randomUUID())
        .email("admin-inactivo@uneg.edu.ve")
        .role(UserRole.ADMIN)
        .status("INACTIVE")
        .build();

      when(organizationRepository.findById(orgActiva.getId())).thenReturn(
        Optional.of(orgActiva)
      );
      when(userRepository.findByOrganizationId(orgActiva.getId())).thenReturn(
        List.of(adminInactivo)
      );

      assertThatThrownBy(() ->
        superAdminService.impersonateOrgAdmin(orgActiva.getId(), "yokoadmin")
      ).isInstanceOf(ResponseStatusException.class);
    }
  }

  // ─── listAllUsers ─────────────────────────────────────────────────────────

  @Nested
  @DisplayName("listAllUsers()")
  class ListAllUsers {

    @Test
    @DisplayName("debe devolver todos los usuarios sin filtro de org")
    void devuelveTodosLosUsuarios() {
      var pageable = PageRequest.of(0, 50);
      var page = new PageImpl<>(List.of(adminActivo));
      when(userRepository.findAll(pageable)).thenReturn(page);

      var result = superAdminService.listAllUsers(pageable);

      assertThat(result.getTotalElements()).isEqualTo(1);
      assertThat(result.getContent().get(0).getEmail()).isEqualTo(
        "admin@uneg.edu.ve"
      );
    }
  }
}
