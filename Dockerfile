ARG BASE_IMAGE=ghcr.io/ministryofjustice/hmpps-eclipse-temurin:25-jre-jammy

FROM eclipse-temurin:25.0.3_9-jdk-jammy AS builder

ARG BUILD_NUMBER
ENV BUILD_NUMBER=${BUILD_NUMBER:-1_0_0}

WORKDIR /app
ADD . .
RUN ./gradlew assemble -Dorg.gradle.daemon=false

FROM ${BASE_IMAGE}

WORKDIR /app

COPY --from=builder --chown=appuser:appgroup /app/build/libs/whereabouts-api*.jar /app/app.jar
COPY --from=builder --chown=appuser:appgroup /app/build/libs/applicationinsights-agent*.jar /app/agent.jar
COPY --from=builder --chown=appuser:appgroup /app/run.sh /app
COPY --from=builder --chown=appuser:appgroup /app/applicationinsights.json /app

ENTRYPOINT ["/bin/sh", "/app/run.sh"]
