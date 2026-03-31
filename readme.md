
# Yoko AI - Backend 🐊

El núcleo inteligente de **Yoko**, un asistente académico impulsado por Inteligencia Artificial diseñado para optimizar los procesos administrativos y estudiantiles en la **UNEG**. Construido con Java y Spring Boot, este backend gestiona la lógica de negocio, la seguridad, el almacenamiento vectorial y la comunicación con modelos de lenguaje de última generación.

## 🚀 Tecnologías Principales

* **Framework:** Java 17+ / Spring Boot 3.x
* **Inteligencia Artificial:** Spring AI integrado con LLaMA 3.1 (vía Groq)
* **Base de Datos:** PostgreSQL con la extensión `pgvector` para Búsqueda Semántica y RAG (Generación Aumentada por Recuperación)
* **Seguridad:** Spring Security con autenticación basada en JWT (JSON Web Tokens)
* **Documentación de API:** Swagger / OpenAPI
* **Gestión de Dependencias:** Maven

## 📂 Estructura del Proyecto

La arquitectura sigue estrictamente el principio de separación de responsabilidades para garantizar la escalabilidad y el fácil mantenimiento:

```text
src/main/java/com/yoko/backend/
├── config/         # Configuraciones globales (CORS, Swagger, Beans)
├── controllers/    # Endpoints REST (Auth, Chat, Data Entry)
├── dtos/           # Data Transfer Objects (ej. MessageRequest, AuthResponse)
├── entities/       # Modelos de base de datos JPA (User, ChatSession, Message)
├── enums/          # Enumeraciones (Roles de usuario, estados)
├── repositories/   # Interfaces de Spring Data JPA
├── security/       # Filtros JWT, validación de tokens y configuración de accesos
├── services/       # Lógica de negocio principal (ChatService, AuthService) aislando a los controladores
└── YokoBackendApplication.java  # Clase principal de ejecución
🛠️ Requisitos Previos
Java Development Kit (JDK) 17 o superior.

Maven instalado en el sistema.

Docker (recomendado para levantar la base de datos con soporte vectorial de forma rápida).

Una API Key válida de Groq.

⚙️ Configuración y Ejecución
1. Levantar la Base de Datos (PostgreSQL + pgvector)
Para que Yoko pueda buscar información en los reglamentos y guías, la base de datos necesita manejar dimensiones vectoriales. Usa Docker para levantar el entorno:

Bash
docker run -d \
  --name yoko-db \
  -e POSTGRES_PASSWORD=tu_password \
  -e POSTGRES_DB=yoko_db \
  -p 5432:5432 \
  ankane/pgvector
Una vez creado el contenedor, es crucial activar la extensión vectorial ingresando al mismo:

Bash
docker exec -it yoko-db psql -U postgres -d yoko_db -c "CREATE EXTENSION IF NOT EXISTS vector;"
2. Variables de Entorno
Configura tu archivo src/main/resources/application.properties con tus credenciales locales:

Properties
# Conexión a Base de Datos
spring.datasource.url=jdbc:postgresql://localhost:5432/yoko_db
spring.datasource.username=postgres
spring.datasource.password=tu_password
spring.jpa.hibernate.ddl-auto=update

# Seguridad JWT
jwt.secret=TU_CLAVE_SECRETA_MUY_SEGURA_AQUI

# Configuración de IA (Groq)
spring.ai.openai.api-key=TU_API_KEY_DE_GROQ
spring.ai.openai.chat.options.model=llama-3.1-8b-instant
3. Compilar y Ejecutar
Desde la raíz del proyecto en tu terminal, ejecuta los siguientes comandos:

Bash
mvn clean install
mvn spring-boot:run
El servidor se iniciará en el puerto 8080.

📡 Documentación de la API
Una vez que el servidor esté corriendo, puedes explorar y probar todos los endpoints disponibles (incluyendo la creación de chats y el envío de mensajes) a través de la interfaz visual de Swagger UI ingresando a:

http://localhost:8080/swagger-ui/index.html