package com.yoko.backend.config;

import com.yoko.backend.services.SuperAdminAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SuperAdminInitializer implements CommandLineRunner {

  private final SuperAdminAuthService superAdminAuthService;

  // Configura en application.properties o variables de entorno:
  // super.admin.username=${SUPER_ADMIN_USERNAME:yokoadmin}
  // super.admin.password=${SUPER_ADMIN_PASSWORD}  ← sin valor por defecto en producción
  @Value("${super.admin.username:yokoadmin}")
  private String username;

  @Value("${super.admin.password}")
  private String password;

  @Override
  public void run(String... args) {
    superAdminAuthService.createInitialCredentials(username, password);
  }
}
