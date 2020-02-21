package uk.gov.justice.digital.hmpps.whereabouts.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.whereabouts.integration.IntegrationTest

class JwkClientTest: IntegrationTest() {

  @Autowired
  private lateinit var jwkClient: JwkClient

  @Test
  fun `findJwkSet - returns valid JWK set - extracts public key`() {
    val publicKey = jwkClient.findJwkSet()

    assertThat(publicKey).contains("RSA public key")
  }
}