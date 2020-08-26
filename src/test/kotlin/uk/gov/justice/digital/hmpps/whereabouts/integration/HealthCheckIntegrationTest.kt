package uk.gov.justice.digital.hmpps.whereabouts.integration

import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest
import com.amazonaws.services.sqs.model.GetQueueAttributesResult
import com.amazonaws.services.sqs.model.QueueAttributeName
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.nhaarman.mockito_kotlin.whenever
import net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.test.util.ReflectionTestUtils
import uk.gov.justice.digital.hmpps.whereabouts.services.health.DlqStatus
import uk.gov.justice.digital.hmpps.whereabouts.services.health.QueueAttributes
import uk.gov.justice.digital.hmpps.whereabouts.services.health.QueueHealth

class HealthCheckIntegrationTest : IntegrationTest() {

  @SpyBean
  @Qualifier("awsSqsClient")
  protected lateinit var awsSqsClient: AmazonSQS

  @Autowired
  private lateinit var queueHealth: QueueHealth

  @Autowired
  @Value("\${sqs.queue.name}")
  private lateinit var queueName: String

  @Autowired
  @Value("\${sqs.dlq.name}")
  private lateinit var dlqName: String

  @AfterEach
  fun tearDown() {
    ReflectionTestUtils.setField(queueHealth, "queueName", queueName)
    ReflectionTestUtils.setField(queueHealth, "dlqName", dlqName)
  }

  @Test
  fun `Health page reports ok`() {
    subPing(200)

    webTestClient.get()
        .uri("/health")
        .headers(setHeaders())
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.components.prisonApiHealth.details.HttpStatus").isEqualTo("OK")
        .jsonPath("$.components.OAuthApiHealth.details.HttpStatus").isEqualTo("OK")
        .jsonPath("$.components.caseNotesApiHealth.details.HttpStatus").isEqualTo("OK")
        .jsonPath("$.status").isEqualTo("UP")
  }

  @Test
  fun `Health page reports down`() {
    subPing(404)

    webTestClient.get()
        .uri("/health")
        .headers(setHeaders())
        .exchange()
        .expectStatus().is5xxServerError
        .expectBody()
        .jsonPath("$.components.prisonApiHealth.details.error").isEqualTo("org.springframework.web.reactive.function.client.WebClientResponseException\$NotFound: 404 Not Found from GET http://localhost:8999/health/ping")
        .jsonPath("$.components.prisonApiHealth.details.body").isEqualTo("some error")
        .jsonPath("$.components.OAuthApiHealth.details.error").isEqualTo("org.springframework.web.reactive.function.client.WebClientResponseException\$NotFound: 404 Not Found from GET http://localhost:8090/auth/health/ping")
        .jsonPath("$.components.OAuthApiHealth.details.body").isEqualTo("some error")
        .jsonPath("$.components.caseNotesApiHealth.details.error").isEqualTo("org.springframework.web.reactive.function.client.WebClientResponseException\$NotFound: 404 Not Found from GET http://localhost:8093/health/ping")
        .jsonPath("$.components.caseNotesApiHealth.details.body").isEqualTo("some error")
        .jsonPath("$.status").isEqualTo("DOWN")
  }

  @Test
  fun `Health page reports a teapot`() {
    subPing(418)

    webTestClient.get()
        .uri("/health")
        .headers(setHeaders())
        .exchange()
        .expectStatus().is5xxServerError
        .expectBody()
        .jsonPath("$.components.prisonApiHealth.details.error").isEqualTo("org.springframework.web.reactive.function.client.WebClientResponseException: 418 I'm a teapot from GET http://localhost:8999/health/ping")
        .jsonPath("$.components.prisonApiHealth.details.body").isEqualTo("some error")
        .jsonPath("$.components.OAuthApiHealth.details.error").isEqualTo("org.springframework.web.reactive.function.client.WebClientResponseException: 418 I'm a teapot from GET http://localhost:8090/auth/health/ping")
        .jsonPath("$.components.OAuthApiHealth.details.body").isEqualTo("some error")
        .jsonPath("$.components.caseNotesApiHealth.details.error").isEqualTo("org.springframework.web.reactive.function.client.WebClientResponseException: 418 I'm a teapot from GET http://localhost:8093/health/ping")
        .jsonPath("$.components.caseNotesApiHealth.details.body").isEqualTo("some error")
        .jsonPath("$.status").isEqualTo("DOWN")
  }

  @Test
  fun `Health page reports a timeout`() {
    subPingWithDelay(200)

    webTestClient.get()
        .uri("/health")
        .headers(setHeaders())
        .exchange()
        .expectStatus().is5xxServerError
        .expectBody()
        .jsonPath("$.components.prisonApiHealth.details.error").isEqualTo("java.lang.IllegalStateException: Timeout on blocking read for 1000 MILLISECONDS")
        .jsonPath("$.components.OAuthApiHealth.details.HttpStatus").isEqualTo("OK")
        .jsonPath("$.components.caseNotesApiHealth.details.HttpStatus").isEqualTo("OK")
        .jsonPath("$.status").isEqualTo("DOWN")
  }

  @Test
  fun `Queue does not exist reports down`() {
    ReflectionTestUtils.setField(queueHealth, "queueName", "missing_queue")
    subPing(200)

    webTestClient.get()
        .uri("/health")
        .headers(setHeaders())
        .exchange()
        .expectStatus().is5xxServerError
        .expectBody()
        .jsonPath("$.status").isEqualTo("DOWN")
        .jsonPath("$.components.queueHealth.status").isEqualTo("DOWN")
  }

  @Test
  fun `Queue health ok and dlq health ok, reports everything up`() {
    subPing(200)

    webTestClient.get()
        .uri("/health")
        .headers(setHeaders())
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.status").isEqualTo("UP")
        .jsonPath("$.components.queueHealth.status").isEqualTo("UP")
        .jsonPath("$.components.queueHealth.details.dlqStatus").isEqualTo(DlqStatus.UP.description)
  }

  @Test
  fun `Dlq health reports interesting attributes`() {
    subPing(200)

    webTestClient.get()
        .uri("/health")
        .headers(setHeaders())
        .exchange()
        .expectBody()
        .jsonPath("$.components.queueHealth.details.${QueueAttributes.MESSAGES_ON_DLQ.healthName}").isEqualTo(0)
  }

  @Test
  fun `Dlq down brings main health and queue health down`() {
    subPing(200)
    mockQueueWithoutRedrivePolicyAttributes()

    webTestClient.get()
        .uri("/health")
        .headers(setHeaders())
        .exchange()
        .expectStatus().is5xxServerError
        .expectBody()
        .jsonPath("$.status").isEqualTo("DOWN")
        .jsonPath("$.components.queueHealth.status").isEqualTo("DOWN")
        .jsonPath("$.components.queueHealth.details.dlqStatus").isEqualTo(DlqStatus.NOT_ATTACHED.description)
  }

  @Test
  fun `Main queue has no redrive policy reports dlq down`() {
    subPing(200)
    mockQueueWithoutRedrivePolicyAttributes()

    webTestClient.get()
        .uri("/health")
        .headers(setHeaders())
        .exchange()
        .expectStatus().is5xxServerError
        .expectBody()
        .jsonPath("$.components.queueHealth.details.dlqStatus").isEqualTo(DlqStatus.NOT_ATTACHED.description)
  }

  @Test
  fun `Dlq not found reports dlq down`() {
    subPing(200)
    ReflectionTestUtils.setField(queueHealth, "dlqName", "missing_queue")

    webTestClient.get()
        .uri("/health")
        .headers(setHeaders())
        .exchange()
        .expectStatus().is5xxServerError
        .expectBody()
        .jsonPath("$.components.queueHealth.details.dlqStatus").isEqualTo(DlqStatus.NOT_FOUND.description)
  }

  @Test
  fun `Health liveness page is accessible`() {
    webTestClient.get()
        .uri("/health/liveness")
        .headers(setHeaders())
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.status").isEqualTo("UP")
  }

  @Test
  fun `Health readiness page is accessible`() {
    webTestClient.get()
        .uri("/health/readiness")
        .headers(setHeaders())
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.status").isEqualTo("UP")
  }


  private fun subPing(status: Int) {
    prisonApiMockServer.stubFor(get("/health/ping").willReturn(aResponse()
        .withHeader("Content-Type", "application/json")
        .withBody(if (status == 200) "pong" else "some error")
        .withStatus(status)))

    oauthMockServer.stubFor(get("/auth/health/ping").willReturn(aResponse()
        .withHeader("Content-Type", "application/json")
        .withBody(if (status == 200) "pong" else "some error")
        .withStatus(status)))

    caseNotesMockServer.stubFor(get("/health/ping").willReturn(aResponse()
        .withHeader("Content-Type", "application/json")
        .withBody(if (status == 200) "pong" else "some error")
        .withStatus(status)))
  }

  private fun subPingWithDelay(status: Int) {
    prisonApiMockServer.stubFor(get("/health/ping").willReturn(aResponse()
        .withHeader("Content-Type", "application/json")
        .withBody(if (status == 200) "pong" else "some error")
        .withStatus(status)
        .withFixedDelay(1000)))

    oauthMockServer.stubFor(get("/auth/health/ping").willReturn(aResponse()
        .withHeader("Content-Type", "application/json")
        .withBody(if (status == 200) "pong" else "some error")
        .withStatus(status)))

    caseNotesMockServer.stubFor(get("/health/ping").willReturn(aResponse()
        .withHeader("Content-Type", "application/json")
        .withBody(if (status == 200) "pong" else "some error")
        .withStatus(status)))
  }

  private fun mockQueueWithoutRedrivePolicyAttributes() {
    val queueName = ReflectionTestUtils.getField(queueHealth, "queueName") as String
    val queueUrl = awsSqsClient.getQueueUrl(queueName)
    whenever(awsSqsClient.getQueueAttributes(GetQueueAttributesRequest(queueUrl.queueUrl).withAttributeNames(listOf(QueueAttributeName.All.toString()))))
        .thenReturn(GetQueueAttributesResult())
  }
}
