package uk.gov.justice.digital.hmpps.whereabouts.controllers

import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyLong
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import uk.gov.justice.digital.hmpps.whereabouts.services.AppointmentService
import javax.persistence.EntityNotFoundException

@WebMvcTest(AppointmentController::class)
class AppointmentControllerTest : TestController() {

  @MockBean
  lateinit var appointmentService: AppointmentService

  @Nested
  inner class RequestAppointmentDetails {

    @Test
    @WithMockUser(username = "ITAG_USER")
    fun `should make a call to the appointments service to retrieve the appointment`() {
      mockMvc.perform(MockMvcRequestBuilders.get("/appointment/$APPOINTMENT_ID"))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(MockMvcResultMatchers.status().isOk)

      verify(appointmentService).getAppointment(APPOINTMENT_ID)
    }

    @Test
    @WithMockUser(username = "ITAG_USER")
    fun `should return a HTTP 404`() {
      whenever(appointmentService.getAppointment(anyLong())).thenThrow(EntityNotFoundException())

      mockMvc.perform(MockMvcRequestBuilders.get("/appointment/$APPOINTMENT_ID"))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    @WithMockUser(username = "ITAG_USER")
    fun `should return a HTTP 500`() {
      whenever(appointmentService.getAppointment(anyLong())).thenThrow(NullPointerException())

      mockMvc.perform(MockMvcRequestBuilders.get("/appointment/$APPOINTMENT_ID"))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(MockMvcResultMatchers.status().is5xxServerError)
    }
  }

  companion object {
    private const val APPOINTMENT_ID = 1L
  }
}
