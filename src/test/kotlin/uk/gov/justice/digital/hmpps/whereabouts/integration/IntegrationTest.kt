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
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import uk.gov.justice.digital.hmpps.whereabouts.common.getGson


@RunWith(SpringRunner::class)
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

    @Value("\${adminToken}")
    private val adminToken: String? = null

    internal fun createHeaderEntityForAdminUser(entity: Any): HttpEntity<*> {
        val headers = HttpHeaders()
        headers.add("Authorization", "bearer $adminToken")
        headers.contentType = MediaType.APPLICATION_JSON
        return HttpEntity(entity, headers)
    }

    internal fun createHeaderEntity(entity: Any): HttpEntity<*> {
        val headers = HttpHeaders()
        headers.add("Authorization", "bearer $token")
        headers.contentType = MediaType.APPLICATION_JSON
        return HttpEntity(entity, headers)
    }

}
