package uk.gov.justice.digital.hmpps.whereabouts.integration

import com.google.gson.Gson
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.json.JsonContent
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.core.ResolvableType
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.whereabouts.common.getGson
import uk.gov.justice.digital.hmpps.whereabouts.integration.wiremock.CaseNotesMockServer
import uk.gov.justice.digital.hmpps.whereabouts.integration.wiremock.PrisonApiMockServer
import uk.gov.justice.digital.hmpps.whereabouts.integration.wiremock.OAuthMockServer
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration
abstract class IntegrationTest {

    @Autowired
    @Deprecated(message = "Use webTestClient")
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var webTestClient: WebTestClient

    internal val gson: Gson = getGson()

    @Value("\${token}")
    private val token: String? = null

    companion object {
        @JvmField
        internal val prisonApiMockServer = PrisonApiMockServer()

        @JvmField
        internal val oauthMockServer = OAuthMockServer()

        @JvmField
        internal val caseNotesMockServer = CaseNotesMockServer()

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

    internal fun createHeaderEntity(entity: Any): HttpEntity<*> {
        val headers = HttpHeaders()
        headers.add("Authorization", "bearer $token")
        headers.contentType = MediaType.APPLICATION_JSON
        return HttpEntity(entity, headers)
    }

  internal fun setHeaders(): (HttpHeaders) -> Unit {
    return {
      it.setBearerAuth(token)
      it.setContentType(MediaType.APPLICATION_JSON)
    }
  }

  fun <T> assertThatStatus(response: ResponseEntity<T>, status: Int) {
        Assertions.assertThat(response.statusCodeValue).withFailMessage("Expecting status code value <%s> to be equal to <%s> but it was not.\nBody was\n%s", response.statusCodeValue, status, response.body).isEqualTo(status)
    }

    fun assertThatJsonFileAndStatus(response: ResponseEntity<String>, status: Int, jsonFile: String?) {
        assertThatStatus(response, status)
        Assertions.assertThat(getBodyAsJsonContent<Any>(response)).isEqualToJson(jsonFile)
    }

    fun <T> getBodyAsJsonContent(response: ResponseEntity<String>): JsonContent<T>? {
        return JsonContent(javaClass, ResolvableType.forType(String::class.java), Objects.requireNonNull(response.body))
    }

}
