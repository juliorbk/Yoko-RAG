package com.yoko.backend.services;

import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
public class InputSanitizer {

  public static final int MAX_MESSAGE_LENGTH = 2000;

  public static final List<String> INJECTION_PATTERNS = List.of(
    "ignore previous instructions",
    "ignore las instrucciones anteriores",
    "ignora las reglas",
    "olvida todo lo anterior",
    "forget everything",
    "you are now",
    "ahora eres",
    "act as",
    "actúa como",
    "</system>",
    "[[",
    "]]",
    "you are",
    "you are a",
    "eres un",
    "pretend you are",
    "fin de system",
    "end of system",
    "modo desarrollador",
    "developer mode"
  );

  public static String sanitizeUserInput(String input) {
    if (input == null || input.isBlank()) {
      throw new IllegalArgumentException("El mensaje no puede estar vacío");
    }
    if (input.length() > MAX_MESSAGE_LENGTH) {
      throw new IllegalArgumentException(
        "Mensaje demasiado largo. Máximo " + MAX_MESSAGE_LENGTH + " caracteres."
      );
    }
    String normalized = input
      .toLowerCase()
      .replaceAll("[áàäâ]", "a")
      .replaceAll("[éèëê]", "e")
      .replaceAll("[íìïî]", "i")
      .replaceAll("[óòöô]", "o")
      .replaceAll("[úùüû]", "u")
      .replaceAll("\\s+", " ");

    for (String pattern : INJECTION_PATTERNS) {
      if (normalized.contains(pattern)) {
        throw new IllegalArgumentException("prompt injection detected");
      }
    }
    return input.trim();
  }
}
