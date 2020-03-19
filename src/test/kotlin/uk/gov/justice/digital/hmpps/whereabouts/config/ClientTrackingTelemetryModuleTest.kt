package uk.gov.justice.digital.hmpps.whereabouts.config

import com.microsoft.applicationinsights.web.internal.RequestTelemetryContext
import com.microsoft.applicationinsights.web.internal.ThreadContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.whereabouts.utils.JwtAuthenticationHelper
import uk.gov.justice.digital.hmpps.whereabouts.utils.JwtParameters
import java.time.Duration

@ExtendWith(SpringExtension::class)
@Import(JwtAuthenticationHelper::class, ClientTrackingTelemetryModule::class)
@ContextConfiguration(initializers = [ConfigFileApplicationContextInitializer::class])
@ActiveProfiles("test")
class ClientTrackingTelemetryModuleTest {

    @Autowired
    lateinit var clientTrackingTelemetryModule: ClientTrackingTelemetryModule

    @Autowired
    lateinit var jwtAuthenticationHelper: JwtAuthenticationHelper

    @BeforeEach
    fun setup() {
        ThreadContext.setRequestTelemetryContext(RequestTelemetryContext(1L))
    }

    @AfterEach
    fun tearDown() {
        ThreadContext.remove()
    }

    @Test
    fun shouldAddClientIdAndUserNameToInsightTelemetry() {

        val token = createJwt(user = "bob", duration = 1L)

        val req = MockHttpServletRequest()
        req.addHeader(HttpHeaders.AUTHORIZATION, "Bearer $token")
        val res = MockHttpServletResponse()

        clientTrackingTelemetryModule.onBeginRequest(req, res)

        val insightTelemetry = ThreadContext.getRequestTelemetryContext().httpRequestTelemetry.properties

        assertThat(insightTelemetry).hasSize(2)
        assertThat(insightTelemetry["username"]).isEqualTo("bob")
        assertThat(insightTelemetry["clientId"]).isEqualTo("elite2apiclient")

    }

    @Test
    fun shouldAddOnlyClientIdIfUsernameNullToInsightTelemetry() {

        val token = createJwt(duration = 1L)

        val req = MockHttpServletRequest()
        req.addHeader(HttpHeaders.AUTHORIZATION, "Bearer $token")
        val res = MockHttpServletResponse()

        clientTrackingTelemetryModule.onBeginRequest(req, res)

        val insightTelemetry = ThreadContext.getRequestTelemetryContext().httpRequestTelemetry.properties

        assertThat(insightTelemetry).hasSize(1)
        assertThat(insightTelemetry["clientId"]).isEqualTo("elite2apiclient")

    }

    @Test
    fun shouldNotAddClientIdAndUserNameToInsightTelemetryAsTokenExpired() {

        val token = createJwt(user = "Fred", duration =  -1L)

        val req = MockHttpServletRequest()
        req.addHeader(HttpHeaders.AUTHORIZATION, "Bearer $token")
        val res = MockHttpServletResponse()

        clientTrackingTelemetryModule.onBeginRequest(req, res)

        val insightTelemetry = ThreadContext.getRequestTelemetryContext().httpRequestTelemetry.properties

        assertThat(insightTelemetry).isEmpty()
    }

    private fun createJwt(user: String? = null, roles: List<String> = listOf(), duration: Long): String =
            jwtAuthenticationHelper.createJwt(JwtParameters(
                username = user,
                roles = roles,
                scope = listOf("read", "write"),
                expiryTime = Duration.ofDays(duration)))

}
