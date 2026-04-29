package com.yoko.backend.repositories;

import com.yoko.backend.entities.YokoDocument;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface YokoDocumentRepository
  extends JpaRepository<YokoDocument, UUID>
{
  // ¡Con solo escribir esta línea, Spring crea el SQL por debajo!
  // Y lo más importante: separa los documentos por cada empresa de tu SaaS
  List<YokoDocument> findByOrganizationId(UUID organizationId);
}
