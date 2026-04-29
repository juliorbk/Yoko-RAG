package com.yoko.backend.repositories;

import com.yoko.backend.DTOs.YokoDocDTO;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class YokoDocumentRepository {

  private final JdbcTemplate jdbc;

  public YokoDocumentRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public List<YokoDocDTO> findAll() {
    // Spring AI guarda los metadatos como JSONB en la columna 'metadata'
    String sql = """
          SELECT
              id::text,
              metadata->>'title'       AS title,
              metadata->>'categoria'   AS categoria,
              metadata->>'subcategoria' AS subcategoria,
              metadata->>'fuente'      AS fuente
          FROM vector_store
          ORDER BY metadata->>'title' ASC
      """;

    return jdbc.query(sql, (rs, rowNum) ->
      new YokoDocDTO(
        rs.getString("id"),
        rs.getString("title"),
        rs.getString("categoria"),
        rs.getString("subcategoria"),
        rs.getString("fuente")
      )
    );
  }

  public long count() {
    Long result = jdbc.queryForObject(
      "SELECT COUNT(*) FROM vector_store",
      Long.class
    );
    return result != null ? result : 0L;
  }
}
