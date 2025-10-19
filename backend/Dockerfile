# Backend Dockerfile (Spring Boot)
# Multi-stage build: first compile with Maven, then run on a slim JRE image
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
# Copy pom.xml and download dependencies (better layer caching)
COPY pom.xml ./
RUN mvn -q -DskipTests dependency:go-offline
# Copy source and build
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app
# Create non-root user
RUN addgroup -S app && adduser -S app -G app
# Copy the built (repackaged) Spring Boot jar; wildcard handles version changes
COPY --from=build /app/target/*SNAPSHOT.jar app.jar
# If you later use a release version without SNAPSHOT, adjust pattern or copy explicitly.
EXPOSE 8080
ENV APP_CORS_ALLOWEDORIGINS=http://localhost:5173
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]
