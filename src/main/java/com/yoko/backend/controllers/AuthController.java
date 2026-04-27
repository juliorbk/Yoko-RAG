package com.yoko.backend.controllers;

import com.yoko.backend.DTOs.AuthResponse;
import com.yoko.backend.DTOs.LoginRequest;
import com.yoko.backend.DTOs.OrgRegisterRequest;
import com.yoko.backend.DTOs.RegisterRequest;
import com.yoko.backend.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "https://yoko-frontend-rho.vercel.app")
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  @Operation(summary = "Register a new user")
  public ResponseEntity<?> register(
    @Valid @RequestBody RegisterRequest request
  ) {
    AuthResponse registeredUser = authService.register(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
  }

  /**
   * Logs in a user using the provided email and password.
   * @param request The LoginRequest object containing the user's email and password.
   * @return An AuthResponse object containing the JWT token and the user object.
   * @throws RuntimeException If the user is not found or the password is invalid.
   */

  @PostMapping("/login")
  @Operation(summary = "Login a user")
  public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
    AuthResponse loggedUser = authService.login(
      request.getEmail(),
      request.getPassword()
    );
    return ResponseEntity.ok(loggedUser);
  }

  @PostMapping("/register-organization")
  @Operation(summary = "Register a new organization with admin user")
  public ResponseEntity<?> organizationRegister(
    @Valid @RequestBody OrgRegisterRequest request
  ) {
    AuthResponse response = authService.organizationRegister(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }
}
