package uk.gov.justice.digital.hmpps.whereabouts.integration

import com.google.gson.Gson
import groovy.util.logging.Slf4j
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import uk.gov.justice.digital.hmpps.whereabouts.common.getGson


@RunWith(SpringJUnit4ClassRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = ["classpath:test-application-override.properties"])
@ContextConfiguration
@Slf4j
abstract class IntegrationTest {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    internal val gson: Gson = getGson()

    @Value("\${token}")
    private val token: String? = null

    init {
        SecurityContextHolder.getContext().authentication = TestingAuthenticationToken("user", "pw")
    }

    internal fun createHeaderEntity(entity: Any): HttpEntity<*> {
        val headers = HttpHeaders()
        headers.add("Authorization", "bearer $token")
        headers.contentType = MediaType.APPLICATION_JSON
        return HttpEntity(entity, headers)
    }
}
