# syntax=docker/dockerfile:1

# ---------------------------------------------------------------------------
# Stage 1: build the React SPA
# ---------------------------------------------------------------------------
FROM node:20 AS frontend
WORKDIR /spa
COPY frontend/package.json frontend/package-lock.json* ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# ---------------------------------------------------------------------------
# Stage 2: build the Spring Boot jar (runs tests + JaCoCo) and bundle the SPA
# ---------------------------------------------------------------------------
FROM eclipse-temurin:21-jdk AS backend-build
WORKDIR /app

# Maven wrapper first for dependency-layer caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && ./mvnw -B -q dependency:go-offline || true

# Application sources
COPY src/ src/

# Drop the freshly built SPA into the classpath static dir so the jar serves it at /
COPY --from=frontend /spa/dist/ src/main/resources/static/

# Build (runs unit tests + jacoco report). Offline where possible.
RUN ./mvnw -B clean package

# ---------------------------------------------------------------------------
# Stage 3: slim runtime
# ---------------------------------------------------------------------------
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app
ENV JAVA_OPTS=""
COPY --from=backend-build /app/target/motointel.jar app.jar
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
