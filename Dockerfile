# Build
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

# Runtime
FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache curl \
    && adduser -D -h /app appuser
WORKDIR /app

COPY --from=build /app/target/sistema-ventas.war app.war
RUN mkdir -p uploads && chown -R appuser:appuser /app

USER appuser
EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod
ENV APP_UPLOADS_DIR=/app/uploads

HEALTHCHECK --interval=30s --timeout=5s --start-period=120s --retries=3 \
  CMD curl -fsS http://127.0.0.1:8080/login || exit 1

ENTRYPOINT ["java", "-jar", "app.war"]
