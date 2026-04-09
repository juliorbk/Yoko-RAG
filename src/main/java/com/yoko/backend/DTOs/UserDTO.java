package com.yoko.backend.DTOs;

import com.yoko.backend.entities.UserRole;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDTO {

  private UUID id;
  private String name;
  private String email;
  private UserRole role;
  private String career;
  private int currentSemester;
}
