import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "4.1.3-beta"
  kotlin("plugin.spring") version "1.6.10"
  kotlin("plugin.jpa") version "1.6.10"
}

configurations {
  implementation { exclude(module = "tomcat-jdbc") }
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencies {
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
  annotationProcessor("org.projectlombok:lombok:1.18.22")

  compileOnly("org.projectlombok:lombok:1.18.22")

  runtimeOnly("com.h2database:h2:2.1.210")
  runtimeOnly("org.flywaydb:flyway-core:8.5.5")
  runtimeOnly("org.postgresql:postgresql:42.3.3")

  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-cache")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
  implementation("org.springframework.boot:spring-boot-starter-webflux")

  implementation("javax.annotation:javax.annotation-api:1.3.2")
  implementation("javax.xml.bind:jaxb-api:2.4.0-b180830.0359")
  implementation("com.sun.xml.bind:jaxb-impl:3.0.2")
  implementation("com.sun.xml.bind:jaxb-core:3.0.2")
  implementation("javax.activation:activation:1.1.1")
  implementation("javax.transaction:javax.transaction-api:1.3")
  implementation("javax.validation:validation-api")

  implementation("io.springfox:springfox-boot-starter:3.0.0")

  implementation("org.apache.commons:commons-lang3")
  implementation("org.apache.commons:commons-text:1.9")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-csv")
  implementation("com.google.code.gson:gson")
  implementation("com.google.guava:guava")
  implementation("org.jetbrains.kotlin:kotlin-reflect")

  implementation("org.springframework:spring-jms")
  implementation("com.amazonaws:amazon-sqs-java-messaging-lib:1.0.8")

  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.springframework.security:spring-security-test")

  testImplementation("io.github.http-builder-ng:http-builder-ng-apache:1.0.4")
  testImplementation("net.javacrumbs.json-unit:json-unit-assertj:2.32.0")
  testImplementation("com.github.tomakehurst:wiremock-standalone:2.27.2")
  testImplementation("io.mockk:mockk:1.12.3")
  testCompileOnly("org.projectlombok:lombok:1.18.22")
  testImplementation("io.jsonwebtoken:jjwt:0.9.1")
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
    "javax.persistence.Entity",
    "javax.persistence.MappedSuperclass",
    "javax.persistence.Embeddable"
  )
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
      jvmTarget = "17"
    }
  }
}
