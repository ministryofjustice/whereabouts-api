import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "5.4.0"
  kotlin("plugin.spring") version "1.9.0"
  kotlin("plugin.jpa") version "1.9.0"
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
  runtimeOnly("org.flywaydb:flyway-core")
  runtimeOnly("org.postgresql:postgresql")

  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-cache")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:2.0.1")

  implementation("io.swagger:swagger-annotations:1.6.11")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.1.0")

  implementation("org.apache.commons:commons-lang3")
  implementation("org.apache.commons:commons-text:1.10.0")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-csv")
  implementation("com.google.code.gson:gson")
  implementation("com.google.guava:guava:32.1.1-jre")
  implementation("org.jetbrains.kotlin:kotlin-reflect")

  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.springframework.security:spring-security-test")

  testImplementation("io.github.http-builder-ng:http-builder-ng-apache:1.0.4")
  testImplementation("net.javacrumbs.json-unit:json-unit-assertj:3.0.0")
  testImplementation("com.github.tomakehurst:wiremock-jre8-standalone:2.35.0")
  testImplementation("io.mockk:mockk:1.13.5")
  testCompileOnly("org.projectlombok:lombok")
  testImplementation("io.jsonwebtoken:jjwt-impl:0.11.5")
  testImplementation("io.jsonwebtoken:jjwt-jackson:0.11.5")
  testImplementation("io.opentelemetry:opentelemetry-sdk-testing:1.28.0")
}

/**
 * Without this Kotlin compiler setting Java Bean validator annotations do not work on Kotlin lists.
 * See for example AppointmentLocationsSpecification#appointmentIntervals
 * The alternative ito this is to use Java classes instead.
 */
tasks.withType<KotlinCompile>().configureEach {
  kotlinOptions {
    freeCompilerArgs += "-Xemit-jvm-type-annotations"
  }
}

allOpen {
  annotations(
    "jakarta.persistence.Entity",
    "jakarta.persistence.MappedSuperclass",
    "jakarta.persistence.Embeddable"
  )
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(19))
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
      jvmTarget = "19"
    }
  }

  test {
    minHeapSize = "128m"
    maxHeapSize = "512m"
    jvmArgs("--add-opens", "java.base/java.lang=ALL-UNNAMED")
  }
}
