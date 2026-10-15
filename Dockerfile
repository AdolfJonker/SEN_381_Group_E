# CivicConnect staging image: Vite build + Spring Boot jar (frontend served from /static).
# Build from repo root: docker build -t civicconnect:staging .

FROM node:22-alpine AS frontend
WORKDIR /frontend
COPY src/frontend/package.json src/frontend/package-lock.json ./
RUN npm ci
COPY src/frontend/ ./
ENV VITE_API_BASE_URL=/api/v1
RUN npm run build

FROM eclipse-temurin:25-jdk AS backend
WORKDIR /backend
COPY src/backend/mvnw src/backend/pom.xml ./
COPY src/backend/.mvn ./.mvn
COPY src/backend/src ./src
COPY --from=frontend /frontend/dist ./src/main/resources/static
RUN chmod +x mvnw && ./mvnw -q -DskipTests package

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=backend /backend/target/civicconnect-0.0.1-SNAPSHOT.jar app.jar
ENV SPRING_PROFILES_ACTIVE=postgres
ENV SERVER_PORT=8080
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
