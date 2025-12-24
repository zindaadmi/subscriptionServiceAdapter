# Multi-stage build for optimized image size
FROM gradle:8.8-jdk17 AS build

WORKDIR /app

# Copy build files
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# Copy source code
COPY framework-core ./framework-core
COPY src ./src

# Build application
RUN gradle build --no-daemon -x test

# Runtime stage
FROM openjdk:17-jre-slim

WORKDIR /app

# Copy built JAR
COPY --from=build /app/build/libs/*.jar app.jar

# Expose port
EXPOSE 8080

# Health check (using wget as it's more commonly available)
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]

