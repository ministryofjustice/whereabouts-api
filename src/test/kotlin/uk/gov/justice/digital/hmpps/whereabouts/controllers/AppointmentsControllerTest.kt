package uk.gov.justice.digital.hmpps.whereabouts.controllers

import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import uk.gov.justice.digital.hmpps.whereabouts.dto.AppointmentSearchDto
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod
import uk.gov.justice.digital.hmpps.whereabouts.services.AppointmentService
import java.time.LocalDate
import java.time.LocalDateTime

@WebMvcTest(AppointmentsController::class)
class AppointmentsControllerTest : TestController() {
  @MockBean
  lateinit var appointmentService: AppointmentService

  @Test
  @WithMockUser(username = "ITAG_USER")
  fun `getAppointments valid call returns expected data`() {
    val agencyId = "MDI"
    val locationId = 123L
    whenever(
      appointmentService.getAppointments(
        ArgumentMatchers.anyString(),
        any(),
        anyOrNull(),
        anyOrNull(),
        anyOrNull()
      )
    )
      .thenReturn(
        listOf(
          AppointmentSearchDto(
            id = 1L,
            agencyId = agencyId,
            locationId = locationId,
            locationDescription = "A location",
            appointmentTypeCode = "VLB",
            appointmentTypeDescription = "Video Link Booking",
            startTime = LocalDateTime.of(2020, 1, 2, 12, 0, 0),
            endTime = LocalDateTime.of(2020, 1, 2, 13, 0, 0),
            offenderNo = "A1234AA",
            firstName = "BILL",
            lastName = "BENN",
            createUserId = "ASMITH"
          )
        )
      )

    mockMvc
      .perform(
        MockMvcRequestBuilders.get("/appointments/MDI")
          .param("date", "2021-03-01")
          .param("timeSlot", "PM")
          .param("offenderLocationPrefix", "MDI-1")
          .param("locationId", locationId.toString())
      )
      .andExpect(MockMvcResultMatchers.status().isOk)
      .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize<Array<Any>>(1)))
      .andExpect(MockMvcResultMatchers.jsonPath("[0].id").value(1))
      .andExpect(MockMvcResultMatchers.jsonPath("[0].offenderNo").value("A1234AA"))

    verify(appointmentService).getAppointments("MDI", LocalDate.of(2021, 3, 1), TimePeriod.PM, "MDI-1", locationId)
  }

  @Test
  @WithMockUser(username = "ITAG_USER")
  fun `getAppointments bad date returns bad request`() {
    mockMvc
      .perform(
        MockMvcRequestBuilders.get("/appointments/MDI")
          .param("date", "xxxxx")
      )
      .andExpect(MockMvcResultMatchers.status().isBadRequest)
  }

  @Test
  @WithMockUser(username = "ITAG_USER")
  fun `getAppointments missing date returns bad request`() {
    mockMvc
      .perform(
        MockMvcRequestBuilders.get("/appointments/MDI")
      )
      .andExpect(MockMvcResultMatchers.status().isBadRequest)
  }
}
