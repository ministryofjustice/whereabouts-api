package uk.gov.justice.digital.hmpps.whereabouts.integration

import com.google.gson.Gson
import groovy.util.logging.Slf4j
import org.junit.Before
import org.junit.ClassRule
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import uk.gov.justice.digital.hmpps.whereabouts.common.getGson
import uk.gov.justice.digital.hmpps.whereabouts.dto.AttendanceDto
import uk.gov.justice.digital.hmpps.whereabouts.integration.wiremock.CaseNotesMockServer
import uk.gov.justice.digital.hmpps.whereabouts.integration.wiremock.Elite2MockServer
import uk.gov.justice.digital.hmpps.whereabouts.integration.wiremock.OAuthMockServer

class ListOfAttendanceDtoReferenceType : ParameterizedTypeReference<List<AttendanceDto>>()

@RunWith(SpringJUnit4ClassRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration
@Slf4j
abstract class IntegrationTest {
  @Autowired
  lateinit var restTemplate: TestRestTemplate

  internal val gson: Gson = getGson()

  @Value("\${token}")
  private val token: String? = null

  companion object {
    @get:ClassRule
    @JvmStatic
    val elite2MockServer = Elite2MockServer()

    @get:ClassRule
    @JvmStatic
    val oauthMockServer = OAuthMockServer()

    @get:ClassRule
    @JvmStatic
    val caseNotesMockServer = CaseNotesMockServer()
  }

  init {
    SecurityContextHolder.getContext().authentication = TestingAuthenticationToken("user", "pw")
    // Resolves an issue where Wiremock keeps previous sockets open from other tests causing connection resets
    System.setProperty("http.keepAlive", "false");
  }

  @Before
  fun resetStubs() {
    elite2MockServer.resetAll()
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
}
