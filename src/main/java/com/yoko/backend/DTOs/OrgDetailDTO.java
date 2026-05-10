package com.yoko.backend.DTOs;

import java.util.UUID;

import com.yoko.backend.entities.OrgSector;
import com.yoko.backend.entities.Organization;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// ─────────────────────────────────────────────────────────────────────────────
// Vista de una organización con métricas agregadas para el panel del SuperAdmin
// ─────────────────────────────────────────────────────────────────────────────
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrgDetailDTO {

    private UUID id;
    private String name;
    private String slug;
    private String plan;
    private boolean active;
    private OrgSector sector;
    private String aiPersona; // Prompt contextual para Yoko

    // Métricas
    private long totalUsers;
    private long totalMessages;
    private long totalDocuments;
    private long activeSessions; // últimas 24h

    /**
     * Convierte la entidad base. Las métricas se setean por separado porque requieren consultas
     * adicionales al repositorio.
     */
    public static OrgDetailDTO fromOrg(Organization org) {
        return OrgDetailDTO.builder().id(org.getId()).name(org.getName()).slug(org.getSlug())
                .plan(org.getPlan()).active(org.isActive()).sector(org.getSector()).build();
    }
}
