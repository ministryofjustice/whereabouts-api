plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "1.0.2"
  kotlin("plugin.spring") version "1.4.0"
  kotlin("plugin.jpa") version "1.4.0"
}


configurations {
  implementation { exclude(module = "tomcat-jdbc") }
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencies {
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
  annotationProcessor("org.projectlombok:lombok:1.18.12")

  compileOnly("org.projectlombok:lombok:1.18.12")

  runtime("com.h2database:h2:1.4.200")
  runtime("org.flywaydb:flyway-core:6.5.5")
  runtime("org.postgresql:postgresql:42.2.14")

  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-cache")
  implementation("org.springframework.boot:spring-boot-starter-security")

  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")

  implementation("org.springframework:spring-webflux")
  implementation("org.springframework.boot:spring-boot-starter-reactor-netty")

  implementation("javax.annotation:javax.annotation-api:1.3.2")
  implementation("javax.xml.bind:jaxb-api:2.4.0-b180830.0359")
  implementation("com.sun.xml.bind:jaxb-impl:3.0.0-M4")
  implementation("com.sun.xml.bind:jaxb-core:3.0.0-M4")
  implementation("javax.activation:activation:1.1.1")
  implementation("javax.transaction:javax.transaction-api:1.3")
  implementation("javax.validation:validation-api:2.0.1.Final")

  implementation("io.springfox:springfox-boot-starter:3.0.0")

  implementation("net.sf.ehcache:ehcache:2.10.6")
  implementation("org.apache.commons:commons-lang3:3.11")
  implementation("org.apache.commons:commons-text:1.9")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.11.2")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.11.2")
  implementation("com.pauldijou:jwt-core_2.11:4.3.0")
  implementation("com.google.code.gson:gson:2.8.6")
  implementation("com.google.guava:guava:29.0-jre")
  implementation("org.jetbrains.kotlin:kotlin-reflect")

  implementation("org.springframework:spring-jms")
  implementation("com.amazonaws:amazon-sqs-java-messaging-lib:1.0.8")

  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("io.github.http-builder-ng:http-builder-ng-apache:1.0.4")
  testImplementation("net.javacrumbs.json-unit:json-unit-assertj:2.18.1")
  testImplementation("com.github.tomakehurst:wiremock-standalone:2.27.1")
  testImplementation("com.nhaarman:mockito-kotlin-kt1.1:1.6.0")

  testCompileOnly("org.projectlombok:lombok:1.18.12")
  testImplementation("io.jsonwebtoken:jjwt:0.9.1")
}
