# ------------------------------------------------------------------------------
# Build Stage
# ------------------------------------------------------------------------------
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

# ------------------------------------------------------------------------------
# Runtime Stage
# ------------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S flowguard && adduser -S flowguard -G flowguard
USER flowguard:flowguard

COPY --from=builder /app/target/flowguard-backend-*.jar app.jar

ENV PORT=8080 \
    SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/api/v1/actuator/health || exit 1

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]

