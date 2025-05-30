package uk.gov.justice.digital.hmpps.whereabouts.config

import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import io.opentelemetry.api.trace.Span
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.text.ParseException

@Configuration
class ClientTrackingConfiguration(private val clientTrackingInterceptor: ClientTrackingInterceptor) : WebMvcConfigurer {
  override fun addInterceptors(registry: InterceptorRegistry) {
    registry.addInterceptor(clientTrackingInterceptor).addPathPatterns("/**")
  }
}

@Configuration
class ClientTrackingInterceptor : HandlerInterceptor {
  override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
    val token = request.getHeader(HttpHeaders.AUTHORIZATION)
    val bearer = "Bearer "
    if (StringUtils.startsWithIgnoreCase(token, bearer)) {
      try {
        val jwtBody = getClaimsFromJWT(token)
        val user = jwtBody?.getClaim("user_name")?.toString()
        val currentSpan = Span.current()
        user?.run {
          currentSpan.setAttribute("username", this) // username in customDimensions
          currentSpan.setAttribute("enduser.id", this) // user_Id at the top level of the request
        }
        currentSpan.setAttribute("clientId", jwtBody?.getClaim("client_id").toString())
      } catch (e: ParseException) {
        log.warn("problem decoding jwt public key for application insights", e)
      }
    }
    return true
  }

  private fun getClaimsFromJWT(token: String): JWTClaimsSet? = try {
    SignedJWT.parse(token.replace("Bearer ", ""))
  } catch (e: ParseException) {
    log.warn("problem decoding jwt public key for application insights", e)
    null
  }?.jwtClaimsSet

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
