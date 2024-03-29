package uk.gov.justice.digital.hmpps.whereabouts.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.util.MimeType

@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PingEndpointIntegrationTest {
  @Autowired
  lateinit var testRestTemplate: TestRestTemplate

  @Test
  fun `health ping endpoint responds with ok`() {
    val response = testRestTemplate.getForEntity("/health/ping", String::class.java)

    assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

    assertThat(response.body).isEqualTo("{\"status\":\"UP\"}")
    assertThat(response.headers.contentType).isEqualTo(MimeType.valueOf("application/json"))
  }
}
