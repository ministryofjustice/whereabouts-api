package uk.gov.justice.digital.hmpps.whereabouts.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.util.MimeType

@RunWith(SpringRunner::class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PingEndpointIntegrationTest {
  @Autowired
  lateinit var testRestTemplate: TestRestTemplate

  @Test
  fun `ping endpoint responds with pong`() {
    val response = testRestTemplate.getForEntity("/ping", String::class.java)

    assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

    assertThat(response.body).isEqualTo("pong")
    assertThat(response.headers.contentType).isEqualTo(MimeType.valueOf("text/plain;charset=UTF-8"))
  }

  @Test
  fun `health ping endpoint responds with ok`() {
    val response = testRestTemplate.getForEntity("/health/ping", String::class.java)

    assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

    assertThat(response.body).isEqualTo("{\"status\":\"UP\"}")
    assertThat(response.headers.contentType).isEqualTo(MimeType.valueOf("application/json"))
  }
}
