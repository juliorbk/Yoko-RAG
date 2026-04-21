package com.yoko.backend.exceptions;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException; // <-- IMPORTA ESTO
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  // 1. Atrapa los errores de Validación (@NotBlank, @Email)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleValidationExceptions(
    MethodArgumentNotValidException ex
  ) {
    Map<String, String> errors = new HashMap<>();
    ex
      .getBindingResult()
      .getAllErrors()
      .forEach(error -> {
        String fieldName = ((FieldError) error).getField();
        String errorMessage = error.getDefaultMessage();
        errors.put(fieldName, errorMessage);
      });
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
  }

  // 2.Atrapa la excepción de Spring Security cuando las credenciales son incorrectas
  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<Map<String, String>> handleBadCredentials(
    BadCredentialsException ex
  ) {
    Map<String, String> error = new HashMap<>();
    // Devolvemos un mensaje seguro y genérico, como manda el manual
    error.put("error", "Correo o contraseña incorrectos");

    // 401 Unauthorized es el código HTTP correcto para credenciales inválidas
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
  }

  // 3. Atrapa otros RuntimeExceptions (ej. "El correo ya está registrado")
  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<Map<String, String>> handleRuntimeExceptions(
    RuntimeException ex
  ) {
    Map<String, String> error = new HashMap<>();
    error.put("error", ex.getMessage());

    if (ex.getMessage().contains("no encontrado")) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  // 4. Atrapa la excepción de Spring Security cuando no tienes permiso para acceder a un recurso
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<Map<String, String>> handleAccessDenied(
    AccessDeniedException ex
  ) {
    Map<String, String> error = new HashMap<>();
    error.put("error", "No tienes permiso para realizar esta acción");
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
  }
}
