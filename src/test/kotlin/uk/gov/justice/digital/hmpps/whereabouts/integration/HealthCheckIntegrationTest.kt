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

    val response = restTemplate.getForEntity("/health", String::class.java)

    System.out.println(response.body)
    assertThatJson(response.body).node("components.elite2ApiHealth.details.HttpStatus").isEqualTo("OK")
    assertThatJson(response.body).node("components.OAuthApiHealth.details.HttpStatus").isEqualTo("OK")
    assertThatJson(response.body).node("components.caseNotesApiHealth.details.HttpStatus").isEqualTo("OK")
    assertThatJson(response.body).node("status").isEqualTo("UP")
    assertThat(response.statusCodeValue).isEqualTo(200)
  }

  @Test
  fun `Health page reports down`() {
    subPing(404)

    val response = restTemplate.getForEntity("/health", String::class.java)

    assertThatJson(response.body).node("components.elite2ApiHealth.details.error").isEqualTo("org.springframework.web.client.HttpClientErrorException\$NotFound: 404 Not Found: [some error]")
    assertThatJson(response.body).node("components.OAuthApiHealth.details.error").isEqualTo("org.springframework.web.client.HttpClientErrorException\$NotFound: 404 Not Found: [some error]")
    assertThatJson(response.body).node("components.caseNotesApiHealth.details.error").isEqualTo("org.springframework.web.client.HttpClientErrorException\$NotFound: 404 Not Found: [some error]")
    assertThatJson(response.body).node("status").isEqualTo("DOWN")
    assertThat(response.statusCodeValue).isEqualTo(503)
  }

  @Test
  fun `Health page reports a teapot`() {
    subPing(418)

    val response = restTemplate.getForEntity("/health", String::class.java)

    assertThatJson(response.body).node("components.elite2ApiHealth.details.error").isEqualTo("org.springframework.web.client.HttpClientErrorException: 418 418: [some error]")
    assertThatJson(response.body).node("components.OAuthApiHealth.details.error").isEqualTo("org.springframework.web.client.HttpClientErrorException: 418 418: [some error]")
    assertThatJson(response.body).node("components.caseNotesApiHealth.details.error").isEqualTo("org.springframework.web.client.HttpClientErrorException: 418 418: [some error]")
    assertThatJson(response.body).node("status").isEqualTo("DOWN")
    assertThat(response.statusCodeValue).isEqualTo(503)
  }

  @Test
  fun `Health page reports a timeout`() {
    subPingWithDelay(200)

    val response = restTemplate.getForEntity("/health", String::class.java)

    assertThatJson(response.body).node("components.elite2ApiHealth.details.error").isEqualTo("org.springframework.web.client.ResourceAccessException: I/O error on GET request for \\\"http://localhost:8999/ping\\\": Read timed out; nested exception is java.net.SocketTimeoutException: Read timed out")
    assertThatJson(response.body).node("components.OAuthApiHealth.details.HttpStatus").isEqualTo("OK")
    assertThatJson(response.body).node("components.caseNotesApiHealth.details.HttpStatus").isEqualTo("OK")
    assertThatJson(response.body).node("status").isEqualTo("DOWN")
    assertThat(response.statusCodeValue).isEqualTo(503)
  }

  @Test
  fun `Queue does not exist reports down`() {
    ReflectionTestUtils.setField(queueHealth, "queueName", "missing_queue")
    subPing(200)

    val response = restTemplate.getForEntity("/health", String::class.java)

    assertThatJson(response.body).node("status").isEqualTo("DOWN")
    assertThatJson(response.body).node("components.queueHealth.status").isEqualTo("DOWN")
    assertThat(response.statusCodeValue).isEqualTo(503)
  }

  @Test
  fun `Queue health ok and dlq health ok, reports everything up`() {
    subPing(200)

    val response = restTemplate.getForEntity("/health", String::class.java)

    assertThatJson(response.body).node("status").isEqualTo("UP")
    assertThatJson(response.body).node("components.queueHealth.status").isEqualTo("UP")
    assertThatJson(response.body).node("components.queueHealth.details.dlqStatus").isEqualTo(DlqStatus.UP.description)
    assertThat(response.statusCodeValue).isEqualTo(200)
  }

  @Test
  fun `Dlq health reports interesting attributes`() {
    subPing(200)

    val response = restTemplate.getForEntity("/health", String::class.java)

    assertThatJson(response.body).node("components.queueHealth.details.${QueueAttributes.MESSAGES_ON_DLQ.healthName}").isEqualTo(0)
  }

  @Test
  fun `Dlq down brings main health and queue health down`() {
    subPing(200)
    mockQueueWithoutRedrivePolicyAttributes()

    val response = restTemplate.getForEntity("/health", String::class.java)

    assertThatJson(response.body).node("status").isEqualTo("DOWN")
    assertThatJson(response.body).node("components.queueHealth.status").isEqualTo("DOWN")
    assertThatJson(response.body).node("components.queueHealth.details.dlqStatus").isEqualTo(DlqStatus.NOT_ATTACHED.description)
    assertThat(response.statusCodeValue).isEqualTo(503)
  }

  @Test
  fun `Main queue has no redrive policy reports dlq down`() {
    subPing(200)
    mockQueueWithoutRedrivePolicyAttributes()

    val response = restTemplate.getForEntity("/health", String::class.java)

    assertThatJson(response.body).node("components.queueHealth.details.dlqStatus").isEqualTo(DlqStatus.NOT_ATTACHED.description)
    assertThat(response.statusCodeValue).isEqualTo(503)
  }

  @Test
  fun `Dlq not found reports dlq down`() {
    subPing(200)
    ReflectionTestUtils.setField(queueHealth, "dlqName", "missing_queue")

    val response = restTemplate.getForEntity("/health", String::class.java)

    assertThatJson(response.body).node("components.queueHealth.details.dlqStatus").isEqualTo(DlqStatus.NOT_FOUND.description)
    assertThat(response.statusCodeValue).isEqualTo(503)
  }

  private fun subPing(status: Int) {
    elite2MockServer.stubFor(get("/ping").willReturn(aResponse()
        .withHeader("Content-Type", "application/json")
        .withBody(if (status == 200) "pong" else "some error")
        .withStatus(status)))

    oauthMockServer.stubFor(get("/auth/ping").willReturn(aResponse()
        .withHeader("Content-Type", "application/json")
        .withBody(if (status == 200) "pong" else "some error")
        .withStatus(status)))

    caseNotesMockServer.stubFor(get("/ping").willReturn(aResponse()
        .withHeader("Content-Type", "application/json")
        .withBody(if (status == 200) "pong" else "some error")
        .withStatus(status)))
  }

  private fun subPingWithDelay(status: Int) {
    elite2MockServer.stubFor(get("/ping").willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(if (status == 200) "pong" else "some error")
            .withStatus(status)
            .withFixedDelay(1000)))

    oauthMockServer.stubFor(get("/auth/ping").willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(if (status == 200) "pong" else "some error")
            .withStatus(status)))

    caseNotesMockServer.stubFor(get("/ping").willReturn(aResponse()
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
