# --- Stage 1: Build the application ---
FROM gradle:8-jdk17 AS builder
WORKDIR /home/gradle/project

# Copy build files first to leverage Docker cache for dependencies
COPY build.gradle settings.gradle ./
# Copy the gradle wrapper files if you use them
COPY gradlew ./
COPY gradle ./gradle

# Copy the rest of the source code
COPY src ./src

# Build the application (skipping tests for speed)
RUN ./gradlew bootJar --no-daemon -x test

# --- Stage 2: Create the runtime image ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create a secure, non-root user to run the app
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy the compiled JAR from the builder stage
# Gradle places the built JAR in the build/libs/ directory
COPY --from=builder /home/gradle/project/build/libs/*.jar app.jar

# Expose Spring Boot's standard port
EXPOSE 8080

# Run the jar with recommended flags for container optimization
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]