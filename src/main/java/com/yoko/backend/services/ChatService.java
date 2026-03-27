package com.yoko.backend.services;

import com.yoko.backend.entities.ChatSession;
import com.yoko.backend.entities.Message;
import com.yoko.backend.entities.MessageRole;
import com.yoko.backend.entities.UserRole;
import com.yoko.backend.repositories.ChatSessionRepository;
import com.yoko.backend.repositories.MessageRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

  private static final int MAX_HISTORY = 20; // mensajes recientes a enviar al LLM
  private static final int TOP_K = 3; // fragmentos RAG a recuperar
  private static final double SIMILARITY_THRESHOLD = 0.65; // umbral mínimo de relevancia
  //Inyeccion de dependencias

  private final ChatSessionRepository sessionRepository;
  private final MessageRepository messageRepository;
  private final VectorStore vectorStore;
  private final ChatClient chatClient;

  public ChatService(
    ChatSessionRepository sessionRepository,
    MessageRepository messageRepository,
    VectorStore vectorStore,
    ChatClient chatClient
  ) {
    this.sessionRepository = sessionRepository;
    this.messageRepository = messageRepository;
    this.vectorStore = vectorStore;
    this.chatClient = chatClient;
  }

  //funcion para procesar mensaje

  /**
   * Processes a message from the user, saving it to the database and generating a title for the chat session if necessary.
   * Then, it retrieves the most recent messages from the database, and uses them to generate a prompt for the AI.
   * The prompt is built by concatenating the most recent messages, and the context extracted from the database using pgvector.
   * Finally, it calls the AI with the prompt and the user's message, and saves the response to the database.
   * @param sessionId the ID of the chat session
   * @param userText the message from the user
   * @return the response from the AI
   */
  public String handleMessage(UUID sessionId, String userText) {
    //Obtenemos la sesión
    ChatSession session = sessionRepository
      .findById(sessionId)
      .orElseThrow(() -> new RuntimeException("Error: Chat session not found"));

    //Acá generamos llamamos a generateChatTitle para generar el titulo
    if (session.getTitle().equals("New chat with Yoko :)")) {
      try {
        String newTitle = generateChatTitle(userText);
        session.setTitle(newTitle);
        sessionRepository.save(session);
      } catch (Exception e) {
        System.out.println("Can't generate title" + e.getMessage());
      }
    }

    //Mostramos los ultimos mensajes
    List<Message> historyBD =
      messageRepository.findByChatSessionIdOrderByCreatedAtAsc(sessionId);

    List<Message> recentHistory =
      historyBD.size() > MAX_HISTORY
        ? historyBD.subList(historyBD.size() - MAX_HISTORY, historyBD.size())
        : historyBD;

    List<org.springframework.ai.chat.messages.Message> historySpringAi =
      recentHistory
        .stream()
        .map(msg ->
          msg.getRole().equals(UserRole.STUDENT)
            ? new UserMessage(msg.getContent())
            : new AssistantMessage(msg.getContent())
        )
        .collect(Collectors.toList());

    //guardamos la pregunta(msg) del  user en el historial

    Message userMessage = Message.builder()
      .chatSession(session)
      .content(userText)
      .role(MessageRole.USER)
      .build();

    messageRepository.save(userMessage);

    //Usamos RAG para obtener el contexto con pgvector buscando los 3 fragmentos mas relevantes relacionados con la pregunta

    List<Document> documentosContexto = vectorStore.similaritySearch(
      SearchRequest.builder()
        .query(userText)
        .topK(TOP_K)
        .similarityThreshold(SIMILARITY_THRESHOLD)
        .build()
    );

    String context = documentosContexto.isEmpty()
      ? "No hay informacion disponible"
      : documentosContexto
          .stream()
          .map(doc -> {
            String fuente = (String) doc
              .getMetadata()
              .getOrDefault("fuente", "desconocida");
            return "[" + fuente + "]\n" + doc.getText();
          })
          .collect(Collectors.joining("\n\n"));

    //Construimos el prompt

    String systemPrompt = """
      Eres Yoko, el asistente virtual oficial de la Universidad Nacional Experimental de Guayana (UNEG).
      Tu objetivo es ayudar a estudiantes de ingeniería y otras carreras con sus dudas académicas y administrativas.
      Responde de forma natural y amigable, puedes usar dialecto venezolano, y sé conciso.
      Cuando uses información del contexto, menciona de qué reglamento o fuente proviene (por ejemplo: "según el reglamento estudiantil...").
      Responde ÚNICAMENTE basándote en el siguiente contexto extraído de la base de datos:

      CONTEXTO:
      %s

      Si la respuesta no se encuentra en el contexto, indica educadamente que no dispones de esa información por el momento.
      """.formatted(context);

    // Enviamos el prompt al LLM

    String yokoResponse = chatClient
      .prompt()
      .system(systemPrompt)
      .messages(historySpringAi)
      .user(userText)
      .call()
      .content();

    //Guardamos la respuesta de Yoko en el historial

    Message aiMessage = Message.builder()
      .chatSession(session)
      .content(yokoResponse)
      .role(MessageRole.ASSISTANT)
      .build();

    messageRepository.save(aiMessage);

    return yokoResponse;
  }

  public List<Message> recentHistory(UUID sessionId) {
    return messageRepository.findByChatSessionIdOrderByCreatedAtAsc(sessionId);
  }

  //Nueva funcion para generar titulo del chat en base al primer mensaje
  public String generateChatTitle(String firstMessage) {
    String prompt =
      " Actúa como un resumidor. Genera un título muy corto (máximo 4 a 5 palabras) para este mensaje. " +
      "Debe ser directo, en el mismo idioma del mensaje, sin comillas, sin puntos finales y sin introducciones. " +
      "Mensaje: " +
      firstMessage;

    return chatClient.prompt(prompt).call().content();
  }
}
