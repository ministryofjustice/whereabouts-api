#!/bin/sh
exec java ${JAVA_OPTS} \
  -XX:+AlwaysActAsServerClassMachine \
  -Dcom.sun.management.jmxremote.local.only=false \
  -Djava.security.egd=file:/dev/./urandom \
  -javaagent:/app/agent.jar \
  -jar /app/app.jar
