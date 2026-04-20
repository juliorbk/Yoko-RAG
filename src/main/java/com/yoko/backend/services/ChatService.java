package com.yoko.backend.services;

import com.yoko.backend.entities.ChatSession;
import com.yoko.backend.entities.Message;
import com.yoko.backend.entities.MessageRole;
import com.yoko.backend.entities.User;
import com.yoko.backend.repositories.ChatSessionRepository;
import com.yoko.backend.repositories.MessageRepository;
import com.yoko.backend.repositories.UserRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ChatService {

  private static final int MAX_HISTORY = 6; // mensajes recientes a enviar al LLM
  private static final int TOP_K = 3; // fragmentos RAG a recuperar
  private static final double SIMILARITY_THRESHOLD = 0.50; // umbral mínimo de relevancia
  private static final String SYSTEM_PROMPT_TEMPLATE = """
    ## Identidad
    Eres Yoko, asistente virtual oficial de la Universidad Nacional Experimental de Guayana (UNEG).
    Fuiste creado por Julio Suárez, estudiante de Ingeniería en Informática de la UNEG — un pro del código y tu creador de confianza.
    Tu propósito es ayudar a estudiantes con dudas académicas, administrativas, horarios de clases, profesores y ubicación de aulas de la UNEG.

    ## Reglas de respuesta
    1. Responde SOLO con información del CONTEXTO proporcionado abajo. Cero conocimiento externo.
    2. Si la información exacta no está en el contexto, responde con algo como esto:
       "Chamo, esa info todavía no la tengo cargada. Consulta directamente en la UNEG o escríbele al soporte. 🙏"
    3. Nunca inventes reglamentos, fechas, nombres de profesores, aulas, notas de corte ni procedimientos.
    4. Si el contexto tiene información parcial (ej. tienes el profesor pero no el aula), compártela e indica claramente qué parte falta.
    5. No respondas temas fuera del ámbito universitario (política, farándula, entretenimiento, etc.).

    ## Reglas de citación — MUY IMPORTANTE
    - PROHIBIDO mencionar nombres de archivos, rutas, IDs de documentos, formato JSON o metadata técnica.
    - Ejemplos de lo que NUNCA debes escribir:
        ✗ "Según [reglamento_pasantia.pdf]..."
        ✗ "De acuerdo a la metadata..."
        ✗ "El contexto dice que..."
    - Cuando necesites citar una fuente, usa lenguaje natural e institucional:
        ✓ "Según el Reglamento de Pasantías..."
        ✓ "De acuerdo al horario de Ingeniería en Informática..."
        ✓ "Para esa materia, el horario indica que..."

    ## Estilo de respuesta
    - Tono amigable y directo, con expresiones venezolanas naturales (chamo, pana, fino, etc.) pero sin exagerar.
    - Respuestas cortas y al grano. Usa listas o viñetas para desglosar horarios si hay varios días.
    - Nunca empieces con "¡Claro!", "¡Por supuesto!" ni "¡Hola!". Ve directo al punto.
    - Si el estudiante saluda, responde el saludo brevemente y pregunta en qué puedes ayudar.

    ## Contexto
    %s
    """;
  //Inyeccion de dependencias

  private final ChatSessionRepository sessionRepository;
  private final MessageRepository messageRepository;
  private final UserRepository userRepository;
  private final VectorStore vectorStore;
  private final ChatClient chatClient;

  public ChatService(
    ChatSessionRepository sessionRepository,
    MessageRepository messageRepository,
    UserRepository userRepository,
    VectorStore vectorStore,
    ChatClient chatClient
  ) {
    this.sessionRepository = sessionRepository;
    this.messageRepository = messageRepository;
    this.userRepository = userRepository;
    this.vectorStore = vectorStore;
    this.chatClient = chatClient;
  }

  public ChatSession createChatSession(UUID userId) {
    User user = userRepository
      .findById(userId)
      .orElseThrow(() -> new RuntimeException("Student id not found"));

    ChatSession newSession = ChatSession.builder()
      .user(user)
      .title("New chat with Yoko :)")
      .build();
    log.debug(
      "Creating new chat session for user {}: {}",
      user.getEmail(),
      newSession
    );
    return sessionRepository.save(newSession);
  }

  public void deleteChatSession(UUID chatId) {
    sessionRepository.deleteById(chatId);
    log.debug("Chat session {} deleted", chatId);
  }

  public Page<ChatSession> getUserChats(UUID userId, Pageable pageable) {
    log.debug(
      "Retrieving chat sessions for user {}: page {}, size {}",
      userId,
      pageable.getPageNumber(),
      pageable.getPageSize()
    );
    return sessionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
  }

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
    //revisar
    ChatSession session = sessionRepository
      .findById(sessionId)
      .orElseThrow(() -> new RuntimeException("Error: Chat session not found"));

    if ("New chat with Yoko :)".equals(session.getTitle())) {
      try {
        session.setTitle(generateChatTitle(userText));
      } catch (Exception e) {
        log.warn("No se pudo generar el título del chat: {}", e.getMessage());
      }
    }

    List<Message> recentHistory = messageRepository
      .findTopByChatSessionIdOrderByCreatedAtDesc(
        sessionId,
        PageRequest.of(0, MAX_HISTORY)
      )
      .reversed();

    List<org.springframework.ai.chat.messages.Message> historySpringAi =
      recentHistory
        .stream()
        .map(msg ->
          msg.getRole() == MessageRole.USER
            ? new UserMessage(msg.getContent())
            : new AssistantMessage(msg.getContent())
        )
        .collect(Collectors.toList());

    messageRepository.save(
      Message.builder()
        .chatSession(session)
        .content(userText)
        .role(MessageRole.USER)
        .build()
    );

    List<Document> documentosContexto = vectorStore.similaritySearch(
      SearchRequest.builder()
        .query(userText)
        .topK(TOP_K)
        .similarityThreshold(SIMILARITY_THRESHOLD)
        .build()
    );
    documentosContexto.forEach(doc ->
      log.info("Metadata disponible: {}", doc.getMetadata())
    );

    String context = documentosContexto.isEmpty()
      ? "No hay información disponible."
      : documentosContexto
          .stream()
          .map(this::formatearDocumento) // ✅ ahora sí lo encuentra
          .collect(Collectors.joining("\n\n---\n\n"));

    String systemPrompt = SYSTEM_PROMPT_TEMPLATE.formatted(context);
    String yokoResponse = chatClient
      .prompt()
      .system(systemPrompt)
      .messages(historySpringAi)
      .user(userText)
      .call()
      .content();

    messageRepository.save(
      Message.builder()
        .chatSession(session)
        .content(yokoResponse)
        .role(MessageRole.ASSISTANT)
        .build()
    );
    sessionRepository.save(session);
    return yokoResponse;
  }

  private String formatearDocumento(Document doc) {
    String titulo =
      doc.getMetadata() != null
        ? (String) doc.getMetadata().getOrDefault("title", null)
        : null;

    if (titulo == null || titulo.isBlank()) {
      return doc.getFormattedContent();
    }

    return "[Fuente: " + titulo + "]\n" + doc.getFormattedContent();
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
