package uk.gov.justice.digital.hmpps.whereabouts.config

import com.microsoft.applicationinsights.web.internal.RequestTelemetryContext
import com.microsoft.applicationinsights.web.internal.ThreadContext
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import uk.gov.justice.digital.hmpps.whereabouts.utils.JwtAuthenticationHelper
import uk.gov.justice.digital.hmpps.whereabouts.utils.JwtParameters
import java.time.Duration

@RunWith(SpringRunner::class)
@Import(JwtAuthenticationHelper::class, ClientTrackingTelemetryModule::class, JwkClient::class)
@ContextConfiguration(initializers = [ConfigFileApplicationContextInitializer::class])
@ActiveProfiles("test")
class ClientTrackingTelemetryModuleTest {

    @Autowired
    lateinit var clientTrackingTelemetryModule: ClientTrackingTelemetryModule

    @Autowired
    lateinit var jwtAuthenticationHelper: JwtAuthenticationHelper

    @MockBean
    lateinit var jwkClient: JwkClient

    @ExperimentalStdlibApi
    @Before
    fun setup() {
        ThreadContext.setRequestTelemetryContext(RequestTelemetryContext(1L))
        val publicKey = "LS0tLS1CRUdJTiBQVUJMSUMgS0VZLS0tLS0NCk1JSUJJakFOQmdrcWhraUc5dzBCQVFFRkFBT0NBUThBTUlJQkNnS0NBUUVBc09QQXRzUUFEZGJSdS9FSDZMUDUNCkJNMS9tRjQwVkRCbjEyaEpTWFBQZDVXWUswSExZMjBWTTdBeHhSOW1uWUNGNlNvMVd0N2ZHTnFVeC9XeWVtQnANCklKTnJzLzdEendnM3V3aVF1Tmg0ektSK0VHeFdiTHdpM3l3N2xYUFV6eFV5QzV4dDg4ZS83dk8rbHoxb0NuaXoNCmpoNG14TkFtczZaWUY3cWZuaEpFOVd2V1B3TExrb2prWnUxSmR1c0xhVm93TjdHVEdOcE1FOGR6ZUprYW0wZ3ANCjRveEhRR2hNTjg3SzZqcVgzY0V3TzZEdmhlbWc4d2hzOTZuelFsOG4yTEZ2QUsydXA5UHJyOUdpMkxGZ1R0N0sNCnFYQTA2a0M0S2d3MklSMWVGZ3pjQmxUT0V3bXpqcmU2NUhvTmFKQnI5dU5aelY1c0lMUE1jenpoUWovZk1oejMNCi9RSURBUUFCDQotLS0tLUVORCBQVUJMSUMgS0VZLS0tLS0="
        whenever(jwkClient.findJwkSet()).thenReturn(publicKey)
    }

    @After
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
