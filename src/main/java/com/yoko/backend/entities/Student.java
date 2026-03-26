package com.yoko.backend.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.List;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "app_user")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  private String name;

  @Column(unique = true, nullable = false)
  private String email;

  // ¡La seguridad es primero!
  @Column(nullable = false)
  private String password;

  // JPA guardará "STUDENT" o "ADMIN" como texto en Postgres
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role;

  private String career;

  @Column(name = "current_semester")
  private Integer currentSemester;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  @JsonIgnore
  private List<ChatSession> chatSessions;
}
