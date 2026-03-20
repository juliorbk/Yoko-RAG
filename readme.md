# 🤖 Yoko Backend - University AI Assistant

Este repositorio contiene el código fuente del backend de **Yoko**, un chatbot impulsado por Inteligencia Artificial diseñado para asistir a los estudiantes universitarios con sus consultas académicas y administrativas.

Este sistema está construido bajo una arquitectura **RAG (Retrieval-Augmented Generation)**, lo que permite al modelo de lenguaje (LLM) generar respuestas precisas y contextualizadas basadas exclusivamente en la base de conocimientos oficial de la universidad.

## 🚀 Tecnologías Principales

- **Framework:** Java 21+ / Spring Boot 4.x
- **Base de Datos Relacional:** PostgreSQL (Historial de sesiones y usuarios)
- **Base de Datos Vectorial:** pgvector (Búsqueda semántica de documentos y FAQs)
- **Integración IA:** Spring AI / LangChain4j

## ⚙️ Arquitectura

El backend expone una API REST consumible por clientes web e implementa un flujo de procesamiento que incluye:

1.  **Gestión de Sesiones:** Mantenimiento de memoria a corto plazo por hilo de conversación.
2.  **Búsqueda Semántica:** Vectorización de consultas para recuperar el contexto institucional más relevante.
3.  **Generación Aumentada:** Orquestación de _prompts_ combinando el historial del usuario, el contexto recuperado y las directrices del sistema antes de consultar al LLM.

## Estructura:

com.yoko.backend
├── YokoApplication.java            # Archivo principal que arranca la app
├── config/                         # Configuraciones globales
│   ├── AiConfig.java               # Configuración del LLM y Vector DB
│   └── CorsConfig.java             # Para permitir que tu frontend web se conecte
├── controllers/                    # Los endpoints REST (la cara de la API)
│   ├── ChatController.java         # Recibe las peticiones de los mensajes
│   └── StudentController.java      # Maneja la info del estudiante
├── dtos/                           # Data Transfer Objects
│   ├── request/                    # Lo que el frontend envía (ej. MessageRequest)
│   └── response/                   # Lo que devuelves (ej. ChatResponse)
├── entities/                       # El modelado de la base de datos
│   ├── Student.java
│   ├── ChatSession.java
│   ├── Message.java
│   └── FaqDocument.java            # La tabla vectorial
├── exceptions/                     # Manejo de errores
│   └── GlobalExceptionHandler.java # Para devolver JSONs limpios si algo falla
├── repositories/                   # Comunicación con PostgreSQL (Spring Data JPA)
│   ├── ChatSessionRepository.java
│   └── MessageRepository.java
└── services/                       # El "cerebro" donde ocurre la magia
    ├── ChatService.java            # Lógica de guardar mensajes y recuperar historial
    └── YokoRagService.java         # Búsqueda semántica + armado de prompt para la IA
