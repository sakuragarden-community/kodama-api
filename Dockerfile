# syntax=docker/dockerfile:1

# --- Build ---
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline

COPY src/ src/
RUN ./mvnw -B package -DskipTests

# --- Runtime ---
FROM eclipse-temurin:25-jre
WORKDIR /app

RUN groupadd --system kodama && useradd --system --gid kodama kodama
COPY --from=build /app/target/*.jar app.jar

USER kodama
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]