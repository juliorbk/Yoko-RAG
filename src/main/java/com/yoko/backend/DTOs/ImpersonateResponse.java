package com.yoko.backend.DTOs;

import java.util.UUID;
import lombok.*;

/**
 * Respuesta al impersonar un admin de organización.
 *
 * El token devuelto es un JWT estándar válido por 30 minutos,
 * con un claim adicional "impersonatedBy: <superAdminUsername>"
 * para trazabilidad en los logs de auditoría.
 *
 * El frontend debe mostrar un banner visible mientras este token esté activo.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ImpersonateResponse {

  private String token; // JWT de corta duración (30 min)
  private String impersonatedEmail;
  private UUID impersonatedOrgId;
  private String impersonatedOrgName;
  private String warning; // Siempre presente: recordatorio de que es impersonación
}
