# ⛏️ Multi-stage build for Spring Boot application
FROM eclipse-temurin:21-jdk-alpine AS builder

# Set working directory
WORKDIR /app

# Copy gradle files
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Make gradlew executable
RUN chmod +x ./gradlew

# Download dependencies
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src src

# Build the application
RUN ./gradlew build -x test --no-daemon

# 🏃 Runtime stage
FROM eclipse-temurin:21-jre-alpine

# Install timezone data
RUN apk add --no-cache tzdata

# Set timezone
ENV TZ=Asia/Seoul

# Create app user
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Set working directory
WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Change ownership
RUN chown -R appuser:appgroup /app

# Switch to app user
USER appuser

# Expose port
EXPOSE 8080

# Health check (Spring Boot actuator must be enabled)
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# JVM options with external config support
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseContainerSupport \
 -Dspring.config.location=file:/app/config/application.yml"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]