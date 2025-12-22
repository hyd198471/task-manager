# Backend Dockerfile (Spring Boot)
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app/backend
# Copy parent and module POMs
COPY ../pom.xml ../pom.xml
COPY backend/pom.xml ./pom.xml
# Copy source
COPY backend/src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
COPY --from=build /app/backend/target/*.jar app.jar
EXPOSE 8080
ENV APP_CORS_ALLOWEDORIGINS=http://localhost:5173
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]
