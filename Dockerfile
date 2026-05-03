# Etapa 1: Construcción
FROM maven:3.9.6-eclipse-temurin-21 AS build
COPY . .
RUN mvn clean package -DskipTests

# Etapa 2: Ejecución
FROM eclipse-temurin:21-jre
COPY --from=build /target/*.jar app.jar
EXPOSE 8080

ENTRYPOINT ["java", "-Xmx700m", "-Xms300m", "-XX:+UseSerialGC", "-XX:MaxMetaspaceSize=200m", "-jar", "app.jar"]