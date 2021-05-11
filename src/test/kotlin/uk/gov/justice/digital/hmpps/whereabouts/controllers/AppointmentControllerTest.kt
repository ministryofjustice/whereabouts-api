package uk.gov.justice.digital.hmpps.whereabouts.controllers

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.whereabouts.dto.AppointmentDto
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod
import uk.gov.justice.digital.hmpps.whereabouts.services.AppointmentService
import uk.gov.justice.digital.hmpps.whereabouts.utils.UserMdcFilter
import java.time.LocalDate
import java.time.LocalDateTime

@WebMvcTest(AppointmentController::class)
@Import(UserMdcFilter::class, StubUserSecurityUtilsConfig::class)
class AppointmentControllerTest : TestController() {
  @MockBean
  lateinit var appointmentService: AppointmentService

  @Test
  @WithMockUser(username = "ITAG_USER")
  fun `getAppointments valid call returns expected data`() {
    val agencyId = "MDI"
    val locationId = 123L
    whenever(appointmentService.getAppointments(anyString(), any(), anyOrNull(), anyOrNull(), anyOrNull()))
      .thenReturn(listOf(AppointmentDto(
        id = 1L,
        agencyId = agencyId,
        locationId = locationId,
        appointmentTypeCode = "VLB",
        startTime = LocalDateTime.of(2020, 1, 2, 12, 0, 0),
        endTime = LocalDateTime.of(2020, 1, 2, 13, 0, 0),
        offenderNo = "A1234AA"
    )))

    mockMvc
      .perform(
        get("/appointments/MDI")
          .param("date", "2021-03-01")
          .param("timeSlot", "PM")
          .param("offenderLocationPrefix", "MDI-1")
          .param("locationId", locationId.toString())
      )
      .andExpect(status().isOk)
      .andExpect(MockMvcResultMatchers.jsonPath("$", hasSize<Array<Any>>(1)))
      .andExpect(MockMvcResultMatchers.jsonPath("[0].id").value(1))
      .andExpect(MockMvcResultMatchers.jsonPath("[0].offenderNo").value("A1234AA"))

    verify(appointmentService).getAppointments("MDI", LocalDate.of(2021, 3, 1), TimePeriod.PM, "MDI-1", locationId)
  }

  @Test
  @WithMockUser(username = "ITAG_USER")
  fun `getAppointments bad date returns bad request`() {
    mockMvc
      .perform(
        get("/appointments/MDI")
          .param("date", "xxxxx")
      )
      .andExpect(status().isBadRequest)
  }

  @Test
  @WithMockUser(username = "ITAG_USER")
  fun `getAppointments missing date returns bad request`() {
    mockMvc
      .perform(
        get("/appointments/MDI")
      )
      .andExpect(status().isBadRequest)
  }
}
