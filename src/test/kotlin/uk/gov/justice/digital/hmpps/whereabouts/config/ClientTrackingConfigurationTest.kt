package uk.gov.justice.digital.hmpps.whereabouts.config

import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.sdk.testing.junit5.OpenTelemetryExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.whereabouts.utils.JwtAuthHelper

@Import(JwtAuthHelper::class, ClientTrackingInterceptor::class, ClientTrackingConfiguration::class)
@ContextConfiguration(initializers = [ConfigDataApplicationContextInitializer::class])
@ActiveProfiles("test")
@ExtendWith(SpringExtension::class)
class ClientTrackingConfigurationTest {

  @Suppress("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  private lateinit var clientTrackingInterceptor: ClientTrackingInterceptor

  @Suppress("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  private lateinit var jwtAuthHelper: JwtAuthHelper

  private val tracer: Tracer = otelTesting.openTelemetry.getTracer("test")

  @Test
  fun shouldAddClientIdAndUserNameToInsightTelemetry() {
    val token = jwtAuthHelper.createJwt("bob")
    val req = MockHttpServletRequest()
    req.addHeader(HttpHeaders.AUTHORIZATION, "Bearer $token")
    val res = MockHttpServletResponse()
    tracer.spanBuilder("span").startSpan().run {
      makeCurrent().use { clientTrackingInterceptor.preHandle(req, res, "null") }
      end()
    }
    otelTesting.assertTraces().hasTracesSatisfyingExactly({ t ->
      t.hasSpansSatisfyingExactly({
        it.hasAttribute(AttributeKey.stringKey("username"), "bob")
        it.hasAttribute(AttributeKey.stringKey("clientId"), "whereabouts-api")
      })
    })
  }

  @Test
  fun shouldAddOnlyClientIdIfUsernameNullToInsightTelemetry() {
    val token = jwtAuthHelper.createJwt(null)
    val req = MockHttpServletRequest()
    req.addHeader(HttpHeaders.AUTHORIZATION, "Bearer $token")
    val res = MockHttpServletResponse()
    tracer.spanBuilder("span").startSpan().run {
      makeCurrent().use { clientTrackingInterceptor.preHandle(req, res, "null") }
      end()
    }
    otelTesting.assertTraces().hasTracesSatisfyingExactly({ t ->
      t.hasSpansSatisfyingExactly({
        it.hasAttribute(AttributeKey.stringKey("clientId"), "whereabouts-api")
      })
    })
  }

  private companion object {
    @JvmStatic
    @RegisterExtension
    private val otelTesting: OpenTelemetryExtension = OpenTelemetryExtension.create()
  }
}
