package com.yoko.backend.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "organization")
public class Organization {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  private String name;
  private String plan;
  private boolean active;

  @Column(columnDefinition = "TEXT")
  private String aiPersona; // Ej: "Eres el recepcionista virtual del Hotel Llovizna Suites. Tu tono es formal y servicial..."

  @OneToMany(mappedBy = "organization")
  private List<User> users;

  @Column(unique = true, nullable = false)
  private String slug; // Ej: "hotel-llovizna", "uneg", "clinica-chilemex"
}
