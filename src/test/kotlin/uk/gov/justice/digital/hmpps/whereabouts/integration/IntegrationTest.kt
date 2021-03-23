package uk.gov.justice.digital.hmpps.whereabouts.integration

import com.google.gson.Gson
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.whereabouts.integration.wiremock.CaseNotesMockServer
import uk.gov.justice.digital.hmpps.whereabouts.integration.wiremock.OAuthMockServer
import uk.gov.justice.digital.hmpps.whereabouts.integration.wiremock.PrisonApiMockServer
import uk.gov.justice.digital.hmpps.whereabouts.utils.JwtAuthHelper
import wiremock.org.apache.commons.io.IOUtils
import java.nio.charset.StandardCharsets

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration
abstract class IntegrationTest {

  @Autowired
  lateinit var webTestClient: WebTestClient

  @Autowired
  lateinit var gson: Gson

  @Autowired
  lateinit var jwtAuthHelper: JwtAuthHelper

  companion object {
    @JvmField
    internal val prisonApiMockServer = PrisonApiMockServer()

    @JvmField
    internal val oauthMockServer = OAuthMockServer()

    @JvmField
    internal val caseNotesMockServer = CaseNotesMockServer()

    private var lastBookingId = 0L

    fun getNextBookingId(): Long {
      lastBookingId += 1
      return lastBookingId
    }

    @BeforeAll
    @JvmStatic
    fun startMocks() {
      prisonApiMockServer.start()
      oauthMockServer.start()
      caseNotesMockServer.start()
    }

    @AfterAll
    @JvmStatic
    fun stopMocks() {
      prisonApiMockServer.stop()
      oauthMockServer.stop()
      caseNotesMockServer.stop()
    }
  }

  init {
    SecurityContextHolder.getContext().authentication = TestingAuthenticationToken("user", "pw")
    // Resolves an issue where Wiremock keeps previous sockets open from other tests causing connection resets
    System.setProperty("http.keepAlive", "false")
  }

  @BeforeEach
  fun resetStubs() {
    prisonApiMockServer.resetAll()
    oauthMockServer.resetAll()
    caseNotesMockServer.resetAll()
    oauthMockServer.stubGrantToken()
  }

  internal fun setHeaders(contentType: MediaType = MediaType.APPLICATION_JSON): (HttpHeaders) -> Unit = {
    it.setBearerAuth(jwtAuthHelper.createJwt(subject = "ITAG_USER", roles = listOf("ROLE_LICENCE_CA", "ROLE_KW_ADMIN"), clientId = "elite2apiclient"))
    it.contentType = contentType
  }

  fun loadJsonFile(jsonFile: String): String =
    IOUtils.toString(javaClass.getResourceAsStream(jsonFile), StandardCharsets.UTF_8.toString())
}
