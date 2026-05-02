package com.yoko.backend.repositories;

import com.yoko.backend.entities.SuperAdminCredentials;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SuperAdminCredentialsRepository
  extends JpaRepository<SuperAdminCredentials, UUID>
{
  Optional<SuperAdminCredentials> findByUsername(String username);
}
