package com.yoko.backend.repositories;

import com.yoko.backend.entities.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
  Optional<User> findByEmail(String email);
  List<User> findByOrganizationId(UUID organizationId);
  Long countByOrganizationId(UUID organizationId);
}
