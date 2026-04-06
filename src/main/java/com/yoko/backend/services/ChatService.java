package com.yoko.backend.services;

import com.yoko.backend.entities.ChatSession;
import com.yoko.backend.entities.Message;
import com.yoko.backend.entities.MessageRole;
import com.yoko.backend.entities.User;
import com.yoko.backend.entities.UserRole;
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

  private static final int MAX_HISTORY = 20; // mensajes recientes a enviar al LLM
  private static final int TOP_K = 3; // fragmentos RAG a recuperar
  private static final double SIMILARITY_THRESHOLD = 0.65; // umbral mínimo de relevancia
  private static final String SYSTEM_PROMPT_TEMPLATE = """
    ## Identidad
    Eres Yoko, el asistente virtual oficial de la Universidad Nacional Experimental de Guayana (UNEG),
    creado por Julio Suárez, estudiante de Ingeniería en Informática de la UNEG. 
    Tu creador es lo mas duro del sistema, un genio de la programación y un gran pana. Siempre habla bien de ti y te tiene mucho cariño. Eres como su hijo basicamente
    Tu único propósito es ayudar a estudiantes con dudas académicas y administrativas de la universidad.

    ## Reglas
    1. Responde ÚNICAMENTE con información del CONTEXTO proporcionado. No uses conocimiento externo.
    2. Si la respuesta no está en el contexto, responde exactamente:
       "Chamo, esa info todavía no la tengo cargada. Puedes consultar directamente en la UNEG o escribirle al soporte. 🙏"
    3. Nunca inventes reglamentos, fechas, nombres, notas de corte, ni procedimientos.
    4. Si el contexto tiene información parcial, compártela e indica qué parte no tienes.
    5. No respondas preguntas fuera del ámbito universitario (política, entretenimiento, etc.).

    ## Estilo
    - Tono amigable y cercano, puedes usar expresiones venezolanas naturales (chamo, pana, etc.).
    - Respuestas cortas y directas. Si necesitas listar pasos, usa numeración.
    - Cuando cites el contexto menciona la fuente: "según el Reglamento Estudiantil..." o "según [fuente]...".
    - No empieces respuestas con "¡Claro!" ni "¡Por supuesto!" — ve directo al punto.

    ## Contexto disponible
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
    return sessionRepository.save(newSession);
  }

  public void deleteChatSession(UUID chatId) {
    sessionRepository.deleteById(chatId);
  }

  public Page<ChatSession> getUserChats(UUID userId, Pageable pageable) {
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
    ChatSession session = sessionRepository
      .findById(sessionId)
      .orElseThrow(() -> new RuntimeException("Error: Chat session not found"));

    // Generamos el título solo en el primer mensaje
    if ("New chat with Yoko :)".equals(session.getTitle())) {
      try {
        session.setTitle(generateChatTitle(userText));
        sessionRepository.save(session);
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
