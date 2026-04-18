package com.yoko.backend.DTOs; // Ajusta tu paquete

import java.util.List;

// Este es el equivalente exacto a tu interfaz de React
public record StatsResponse(
  long totalUsers,
  long activeSessions, // Cambiado para coincidir con tu front
  long totalMessages,
  long totalDocuments, // Nuevo campo
  List<Long> messagesLastWeek, // Arreglo de números
  List<TopQuestionDTO> topQuestions // Arreglo de objetos
) {}
