import uk.gov.justice.digital.hmpps.gradle.PortForwardRDSTask
import uk.gov.justice.digital.hmpps.gradle.PortForwardRedisTask
import uk.gov.justice.digital.hmpps.gradle.RevealSecretsTask

plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "10.5.4"
  kotlin("plugin.spring") version "2.4.0"
  kotlin("plugin.jpa") version "2.4.0"
}

configurations {
  implementation { exclude(module = "tomcat-jdbc") }
  testImplementation { exclude(group = "org.junit.vintage") }
}

repositories {
  maven { url = uri("https://repo.spring.io/milestone") }
  mavenCentral()
}
dependencies {
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
  annotationProcessor("org.projectlombok:lombok")

  compileOnly("org.projectlombok:lombok")

  runtimeOnly("com.h2database:h2")
  runtimeOnly("org.flywaydb:flyway-database-postgresql")
  runtimeOnly("org.postgresql:postgresql:42.7.13")

  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:2.5.0")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-cache")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-webclient")
  implementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:7.4.0")
  implementation("org.springframework.boot:spring-boot-starter-flyway")
  implementation("org.springframework.boot:spring-boot-jackson2")

  implementation("io.swagger:swagger-annotations:1.6.16")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.3")

  implementation("org.apache.commons:commons-lang3")
  implementation("org.apache.commons:commons-text:1.15.0")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-csv")
  implementation("com.google.code.gson:gson")
  implementation("com.google.guava:guava:33.6.0-jre")
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("io.opentelemetry.instrumentation:opentelemetry-instrumentation-annotations:2.29.0")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
  implementation("uk.gov.service.notify:notifications-java-client:6.0.0-RELEASE")

  testImplementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter-test:2.5.0")
  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
  testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
  testImplementation("org.springframework.boot:spring-boot-starter-webflux-test")
  testImplementation("io.github.http-builder-ng:http-builder-ng-apache:1.0.4")
  testImplementation("net.javacrumbs.json-unit:json-unit-assertj:6.0.1")
  testImplementation("org.wiremock:wiremock-standalone:3.13.2")
  testImplementation("io.mockk:mockk:1.14.11")
  testImplementation("io.jsonwebtoken:jjwt-impl:0.13.0")
  testImplementation("io.jsonwebtoken:jjwt-jackson:0.13.0")
  testImplementation("io.opentelemetry:opentelemetry-sdk-testing:1.63.0")
  testImplementation("io.swagger.parser.v3:swagger-parser:2.1.45") {
    exclude(group = "io.swagger.core.v3")
  }
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks {
  register<PortForwardRDSTask>("portForwardRDS") {
    namespacePrefix = "whereabouts-api"
  }

  register<PortForwardRedisTask>("portForwardRedis") {
    namespacePrefix = "whereabouts-api"
  }

  register<RevealSecretsTask>("revealSecrets") {
    namespacePrefix = "whereabouts-api"
  }

  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
      freeCompilerArgs.set(listOf("-Xemit-jvm-type-annotations"))
      jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25)
    }
  }
}
