ARG BASE_IMAGE=ghcr.io/ministryofjustice/hmpps-eclipse-temurin:25-jre-jammy
ARG BUILDER_IMAGE=eclipse-temurin:25-jdk-jammy
FROM ${BUILDER_IMAGE} AS builder
# USER root

ARG BUILD_NUMBER
ENV BUILD_NUMBER=${BUILD_NUMBER:-1_0_0}

WORKDIR /app
ADD . .
RUN ./gradlew assemble -Dorg.gradle.daemon=false

# Grab AWS RDS Root cert
RUN apt-get update && apt-get install -y curl
RUN curl https://truststore.pki.rds.amazonaws.com/global/global-bundle.pem  > root.crt

FROM ${BASE_IMAGE}
LABEL maintainer="HMPPS Digital Studio <info@digital.justice# .gov.uk>"
USER root

ARG BUILD_NUMBER
ENV BUILD_NUMBER=${BUILD_NUMBER:-1_0_0}

RUN install -d -m 0755 /var/lib/apt/lists/partial && \
    apt-get update && \
    apt-get -y upgrade && \
    apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/*

ENV TZ=Europe/London
RUN ln -snf "/usr/share/zoneinfo/$TZ" /etc/localtime && echo "$TZ" > /etc/timezone

RUN if ! getent group appgroup >/dev/null; then addgroup --gid 2000 --system appgroup; fi && \
    if ! id -u appuser >/dev/null 2>&1; then adduser --uid 2000 --system appuser --gid 2000; fi

# Install AWS RDS Root cert into Java truststore
RUN mkdir -p /home/appuser/.postgresql
COPY --from=builder --chown=appuser:appgroup /app/root.crt /home/appuser/.postgresql/root.crt

WORKDIR /app

COPY --from=builder --chown=appuser:appgroup /app/build/libs/whereabouts-api*.jar /app/app.jar
COPY --from=builder --chown=appuser:appgroup /app/build/libs/applicationinsights-agent*.jar /app/agent.jar
COPY --from=builder --chown=appuser:appgroup /app/run.sh /app
COPY --from=builder --chown=appuser:appgroup /app/applicationinsights.json /app

USER 2000

ENTRYPOINT ["/bin/sh", "/app/run.sh"]
