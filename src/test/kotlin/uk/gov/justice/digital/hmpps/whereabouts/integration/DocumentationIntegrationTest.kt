package uk.gov.justice.digital.hmpps.whereabouts.integration

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DocumentationIntegrationTest {
  @Autowired
  lateinit var testRestTemplate: TestRestTemplate

  @Autowired
  lateinit var mapper: ObjectMapper

  @Test
  fun `openApi generates valid json`() {
    val response = testRestTemplate.getForEntity("/v3/api-docs", String::class.java)

    assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    mapper.readTree(response.body)
  }
}
