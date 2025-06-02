package uk.gov.justice.digital.hmpps.whereabouts.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import uk.gov.justice.digital.hmpps.whereabouts.config.ResourceServerConfiguration
import uk.gov.justice.digital.hmpps.whereabouts.utils.JwtAuthHelper
import uk.gov.justice.digital.hmpps.whereabouts.utils.UserMdcFilter

@ActiveProfiles("test")
@Import(UserMdcFilter::class, StubUserSecurityUtilsConfig::class, ResourceServerConfiguration::class, JwtAuthHelper::class)
open class TestController {
  @Autowired
  lateinit var mockMvc: MockMvc

  @Autowired
  lateinit var objectMapper: ObjectMapper
}
