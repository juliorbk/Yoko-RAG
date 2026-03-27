package com.yoko.backend.controllers;

import com.yoko.backend.DTOs.LoginRequest;
import com.yoko.backend.DTOs.RegisterRequest;
import com.yoko.backend.entities.User;
import com.yoko.backend.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // Permite peticiones desde tu frontend
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  @Operation(summary = "Register a new user")
  public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
    System.out.println(request.toString());
    try {
      User registeredUser = authService.register(request);
      return ResponseEntity.ok(registeredUser); // Retorna el usuario registrado 201
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(e.getMessage()); // Retorna un error 400
    }
  }

  @PostMapping("/login")
  @Operation(summary = "Login a user")
  public ResponseEntity<?> login(@RequestBody LoginRequest request) {
    try {
      User loggedUser = authService.login(
        request.getEmail(),
        request.getPassword()
      );
      return ResponseEntity.ok(loggedUser); // Retorna el usuario registrado 201
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(e.getMessage()); // Retorna un error 400
    }
  }
}
