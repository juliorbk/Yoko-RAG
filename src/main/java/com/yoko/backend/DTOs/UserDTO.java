package com.yoko.backend.DTOs;

import com.yoko.backend.entities.User;
import com.yoko.backend.entities.UserRole;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

  private UUID id;
  private String name;
  private String email;
  private UserRole role;
  private UUID organizationId;
  private String organizationName;

  public static UserDTO fromUser(User user) {
    UserDTO dto = new UserDTO();
    dto.setId(user.getId());
    dto.setEmail(user.getEmail());
    dto.setName(user.getName());
    dto.setRole(user.getRole());
    if (user.getOrganization() != null) {
      dto.setOrganizationId(user.getOrganization().getId());
      dto.setOrganizationName(user.getOrganization().getName());
    }
    return dto;
  }
}
