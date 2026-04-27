package com.yoko.backend;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yoko.backend.entities.ChatSession;
import com.yoko.backend.entities.User;
import com.yoko.backend.entities.UserRole;
import com.yoko.backend.repositories.ChatSessionRepository;
import com.yoko.backend.repositories.UserRepository;
import com.yoko.backend.services.ChatService;
import com.yoko.backend.services.JwtService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Fix 4 — ChatController: autorización de sesiones por propietario")
class ChatAuthorizationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private JwtService jwtService;

  @MockBean
  private ChatService chatService;

  @MockBean
  private ChatSessionRepository chatSessionRepository;

  @MockBean
  private UserRepository userRepository;

  private User usuarioPropietario;
  private User usuarioAjeno;
  private ChatSession sesionDelPropietario;

  private String tokenPropietario;
  private String tokenAjeno;

  @BeforeEach
  void setUp() {
    UUID idPropietario = UUID.randomUUID();
    UUID idAjeno = UUID.randomUUID();
    UUID idSesion = UUID.randomUUID();

    usuarioPropietario = User.builder()
      .id(idPropietario)
      .email("propietario@uneg.edu.ve")
      .password("hashed")
      .role(UserRole.USER)
      .name("Propietario")
      .build();

    usuarioAjeno = User.builder()
      .id(idAjeno)
      .email("ajeno@uneg.edu.ve")
      .password("hashed")
      .role(UserRole.USER)
      .name("Ajeno")
      .build();

    sesionDelPropietario = ChatSession.builder()
      .id(idSesion)
      .user(usuarioPropietario)
      .title("Mi sesión")
      .build();

    // JWT para cada usuario
    tokenPropietario =
      "Bearer " + jwtService.generateToken(usuarioPropietario.getEmail());
    tokenAjeno = "Bearer " + jwtService.generateToken(usuarioAjeno.getEmail());

    // Mocks del repositorio
    when(userRepository.findByEmail("propietario@uneg.edu.ve")).thenReturn(
      Optional.of(usuarioPropietario)
    );
    when(userRepository.findByEmail("ajeno@uneg.edu.ve")).thenReturn(
      Optional.of(usuarioAjeno)
    );
    when(chatSessionRepository.findById(idSesion)).thenReturn(
      Optional.of(sesionDelPropietario)
    );
  }

  // ─── Sin token ────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("Requests sin autenticación")
  class SinToken {

    @Test
    @DisplayName("GET /api/sessions/{id} sin token debe devolver 401/403")
    void getHistorialSinToken() throws Exception {
      mockMvc
        .perform(get("/api/sessions/" + sesionDelPropietario.getId()))
        .andExpect(status().is(403));
    }

    @Test
    @DisplayName(
      "POST /api/sessions/{id}/messages sin token debe devolver 401/403"
    )
    void enviarMensajeSinToken() throws Exception {
      mockMvc
        .perform(
          post("/api/sessions/" + sesionDelPropietario.getId() + "/messages")
            .contentType(MediaType.APPLICATION_JSON)
            .content("\"¿Cuándo son las inscripciones?\"")
        )
        .andExpect(status().is(403));
    }

    @Test
    @DisplayName("DELETE /api/sessions/{id} sin token debe devolver 401/403")
    void eliminarSesionSinToken() throws Exception {
      mockMvc
        .perform(delete("/api/sessions/" + sesionDelPropietario.getId()))
        .andExpect(status().is(403));
    }
  }

  // ─── Usuario propietario ──────────────────────────────────────────────────

  @Nested
  @DisplayName("Usuario propietario de la sesión")
  class UsuarioPropietario {

    @Test
    @DisplayName("el propietario puede leer el historial de su sesión")
    void propietarioPuedeLeerHistorial() throws Exception {
      when(
        chatService.recentHistory(sesionDelPropietario.getId(), null)
      ).thenReturn(List.of());

      mockMvc
        .perform(
          get("/api/sessions/" + sesionDelPropietario.getId()).header(
            "Authorization",
            tokenPropietario
          )
        )
        .andExpect(status().isOk());
    }

    @Test
    @DisplayName("el propietario puede enviar mensajes a su sesión")
    void propietarioPuedeEnviarMensaje() throws Exception {
      when(chatService.handleMessage(any(), any(), any())).thenReturn(
        "Respuesta"
      );
      mockMvc
        .perform(
          post("/api/sessions/" + sesionDelPropietario.getId() + "/messages")
            .header("Authorization", tokenPropietario)
            .contentType(MediaType.APPLICATION_JSON)
            .content("\"¿Cuándo son las inscripciones?\"")
        )
        .andExpect(status().isOk());
    }

    @Test
    @DisplayName("el propietario puede eliminar su propia sesión")
    void propietarioPuedeEliminarSesion() throws Exception {
      mockMvc
        .perform(
          delete("/api/sessions/" + sesionDelPropietario.getId()).header(
            "Authorization",
            tokenPropietario
          )
        )
        .andExpect(status().isOk());
    }
  }

  // ─── Usuario ajeno — estos tests FALLARÁN hasta que implementes el Fix 4 ──

  @Nested
  @DisplayName(
    "Usuario ajeno a la sesión — deben devolver 403 (Fix 4 pendiente)"
  )
  class UsuarioAjeno {

    @Test
    @DisplayName("un usuario ajeno NO puede leer el historial de otra sesión")
    void ajenoNoPuedeLeerHistorial() throws Exception {
      // Le decimos al mock que simule el AccessDeniedException de tu ChatService
      when(chatService.recentHistory(any(), any())).thenThrow(
        new AccessDeniedException(
          "No tienes permiso para acceder a esta sesión"
        )
      );

      mockMvc
        .perform(
          get("/api/sessions/" + sesionDelPropietario.getId()).header(
            "Authorization",
            tokenAjeno
          )
        )
        .andExpect(status().isForbidden()); // espera 403
    }

    @Test
    @DisplayName("un usuario ajeno NO puede enviar mensajes a otra sesión")
    void ajenoNoPuedeEnviarMensaje() throws Exception {
      // Le decimos al mock que simule el ResponseStatusException de tu ChatService
      when(chatService.handleMessage(any(), any(), any())).thenThrow(
        new AccessDeniedException("No tienes permiso")
      );

      mockMvc
        .perform(
          post("/api/sessions/" + sesionDelPropietario.getId() + "/messages")
            .header("Authorization", tokenAjeno)
            .contentType(MediaType.APPLICATION_JSON)
            .content("\"Mensaje de intruso\"")
        )
        .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("un usuario ajeno NO puede eliminar una sesión ajena")
    void ajenoNoPuedeEliminar() throws Exception {
      // Para métodos void (como deleteChatSession), Mockito usa doThrow()
      doThrow(new AccessDeniedException("No tienes permiso"))
        .when(chatService)
        .deleteChatSession(any(), any());

      mockMvc
        .perform(
          delete("/api/sessions/" + sesionDelPropietario.getId()).header(
            "Authorization",
            tokenAjeno
          )
        )
        .andExpect(status().isForbidden());
    }
  }
}
