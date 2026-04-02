# Etapa 1: Construcción
FROM maven:3.9.6-eclipse-temurin-21 AS build
COPY . .
RUN mvn clean package -DskipTests

# Etapa 2: Ejecución
FROM eclipse-temurin:21-jre
COPY --from=build /target/*.jar app.jar
EXPOSE 8080

# === LA DIETA ESTRICTA PARA RENDER ===
# -Xmx300m: Máximo 300MB de RAM.
# -XX:+UseSerialGC: Usa el recolector de basura ahorrador.
# -XX:MaxMetaspaceSize=128m: Limita la memoria para las clases de Spring.
ENTRYPOINT ["java", "-Xmx300m", "-Xms150m", "-XX:+UseSerialGC", "-XX:MaxMetaspaceSize=128m", "-jar", "app.jar"]