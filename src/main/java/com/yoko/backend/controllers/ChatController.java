package com.yoko.backend.controllers;

import com.yoko.backend.entities.ChatSession;
import com.yoko.backend.entities.Message;
import com.yoko.backend.entities.Student;
import com.yoko.backend.repositories.ChatSessionRepository;
import com.yoko.backend.repositories.StudentRepository;
import com.yoko.backend.services.ChatService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sessions")
@CrossOrigin(origins = "*")
public class ChatController {

  private final ChatService chatService;
  private final ChatSessionRepository sessionRepository;
  private final StudentRepository studentRepository;

  //Inyeccion de dependencias
  public ChatController(
    ChatService chatService,
    ChatSessionRepository sessionRepository,
    StudentRepository studentRepository
  ) {
    this.chatService = chatService;
    this.sessionRepository = sessionRepository;
    this.studentRepository = studentRepository;
  }

  //Endpoint para nuevo chat

  @PostMapping("/find-or-create")
  public ResponseEntity<Student> findOrCreate(@RequestBody Student student) {
    return studentRepository
      .findByEmail(student.getEmail())
      .map(ResponseEntity::ok)
      .orElseGet(() -> ResponseEntity.ok(studentRepository.save(student)));
  }

  @PostMapping("/{studentId}")
  public ResponseEntity<ChatSession> newChat(@PathVariable UUID studentId) {
    Student student = studentRepository
      .findById(studentId)
      .orElseThrow(() -> new RuntimeException("Student id not found"));

    ChatSession newSession = ChatSession.builder()
      .student(student)
      .title("New chat with Yoko :)")
      .build();

    return ResponseEntity.ok(sessionRepository.save(newSession));
  }

  //Endpoint para enviar mensaje
  @PostMapping("/{chatId}/enviar")
  public ResponseEntity<String> sendMessage(
    @PathVariable UUID chatId,
    @RequestBody String message
  ) {
    String response = chatService.handleMessage(chatId, message);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{chatId}/historial")
  public ResponseEntity<List<Message>> getHistory(@PathVariable UUID chatId) {
    List<Message> history = chatService.recentHistory(chatId);
    return ResponseEntity.ok(history);
  }
}
