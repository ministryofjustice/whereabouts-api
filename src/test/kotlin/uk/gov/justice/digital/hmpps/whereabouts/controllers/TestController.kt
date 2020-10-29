package uk.gov.justice.digital.hmpps.whereabouts.controllers

import com.google.gson.Gson
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import uk.gov.justice.digital.hmpps.whereabouts.common.getGson

@ActiveProfiles("test")
open class TestController {
  @Autowired
  internal lateinit var mockMvc: MockMvc
  internal val gson: Gson = getGson()
}
