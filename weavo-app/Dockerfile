# Multi-stage Dockerfile for KalaSetu Artisan Marketplace (Java 21 + Spring Boot)
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /build

COPY pom.xml .
COPY kalasetu-domain/pom.xml kalasetu-domain/
COPY kalasetu-payment/pom.xml kalasetu-payment/
COPY kalasetu-persistence/pom.xml kalasetu-persistence/
COPY kalasetu-concurrency/pom.xml kalasetu-concurrency/
COPY kalasetu-fileio/pom.xml kalasetu-fileio/
COPY kalasetu-web/pom.xml kalasetu-web/

RUN mvn -B dependency:go-offline

COPY kalasetu-domain/src kalasetu-domain/src
COPY kalasetu-payment/src kalasetu-payment/src
COPY kalasetu-persistence/src kalasetu-persistence/src
COPY kalasetu-concurrency/src kalasetu-concurrency/src
COPY kalasetu-fileio/src kalasetu-fileio/src
COPY kalasetu-web/src kalasetu-web/src

RUN mvn -B clean package -DskipTests=false

FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

RUN addgroup -S kalasetu && adduser -S kalasetu -G kalasetu
RUN mkdir -p /app/runtime-data && chown -R kalasetu:kalasetu /app

COPY --from=builder /build/kalasetu-web/target/kalasetu-web-1.0.0.jar /app/kalasetu.jar
USER kalasetu

EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=25s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/kalasetu.jar"]
