package com.yoko.backend.DTOs;

import com.yoko.backend.entities.UserRole;
import lombok.Builder;
import lombok.Data;
import java.util.UUID;

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
