package uk.gov.justice.digital.hmpps.whereabouts.controllers

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyLong
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.whereabouts.dto.CreateAppointmentSpecification
import uk.gov.justice.digital.hmpps.whereabouts.dto.Repeat
import uk.gov.justice.digital.hmpps.whereabouts.model.RepeatPeriod
import uk.gov.justice.digital.hmpps.whereabouts.services.AppointmentService
import java.time.LocalDateTime
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
      mockMvc.perform(get("/appointment/$APPOINTMENT_ID"))
        .andDo(print())
        .andExpect(status().isOk)

      verify(appointmentService).getAppointment(APPOINTMENT_ID)
    }

    @Test
    @WithMockUser(username = "ITAG_USER")
    fun `should return a HTTP 404`() {
      whenever(appointmentService.getAppointment(anyLong())).thenThrow(EntityNotFoundException())

      mockMvc.perform(get("/appointment/$APPOINTMENT_ID"))
        .andDo(print())
        .andExpect(status().isNotFound)
    }

    @Test
    @WithMockUser(username = "ITAG_USER")
    fun `should return a HTTP 500`() {
      whenever(appointmentService.getAppointment(anyLong())).thenThrow(NullPointerException())

      mockMvc.perform(get("/appointment/$APPOINTMENT_ID"))
        .andDo(print())
        .andExpect(status().is5xxServerError)
    }
  }

  @Nested
  inner class CreatingAnAppointment {

    @Test
    @WithMockUser(username = "ITAG_USER")
    fun `should create an appointment`() {
      val startTime = LocalDateTime.now()
      val endTime = LocalDateTime.now()

      mockMvc.perform(
        post("/appointment")
          .contentType(MediaType.APPLICATION_JSON)
          .content(getCreateAppointmentSpecificationAsJson(startTime = startTime, endTime = endTime))
      )
        .andDo(print())
        .andExpect(status().isCreated)

      verify(appointmentService).createAppointment(
        CreateAppointmentSpecification(
          locationId = 1,
          startTime = startTime,
          endTime = endTime,
          bookingId = 1,
          comment = "test",
          appointmentType = "ABC"
        )
      )
    }

    @Test
    @WithMockUser(username = "ITAG_USER")
    fun `should create a recurring appointment`() {
      val startTime = LocalDateTime.now()
      val endTime = LocalDateTime.now()

      mockMvc.perform(
        post("/appointment")
          .contentType(MediaType.APPLICATION_JSON)
          .content(
            getCreateAppointmentSpecificationAsJson(
              startTime = startTime,
              endTime = endTime,
              repeatPeriod = RepeatPeriod.DAILY,
              count = 1
            )
          )
      )
        .andDo(print())
        .andExpect(status().isCreated)

      verify(appointmentService).createAppointment(
        CreateAppointmentSpecification(
          locationId = 1,
          startTime = startTime,
          endTime = endTime,
          bookingId = 1,
          comment = "test",
          appointmentType = "ABC",
          repeat = Repeat(RepeatPeriod.DAILY, 1)
        )
      )
    }

    @Test
    @WithMockUser(username = "ITAG_USER")
    fun `should return a HTTP 500`() {
      whenever(appointmentService.createAppointment(any())).thenThrow(NullPointerException())

      mockMvc.perform(
        post("/appointment")
          .contentType(MediaType.APPLICATION_JSON)
          .content(getCreateAppointmentSpecificationAsJson())
      )
        .andDo(print())
        .andExpect(status().is5xxServerError)
    }

    @Test
    @WithMockUser(username = "ITAG_USER")
    fun `should return a HTTP 400`() {
      mockMvc.perform(
        post("/appointment")
          .contentType(MediaType.APPLICATION_JSON)
          .content("")
      )
        .andDo(print())
        .andExpect(status().isBadRequest)
    }

    private fun getCreateAppointmentSpecificationAsJson(
      locationId: Long = 1,
      startTime: LocalDateTime = LocalDateTime.now(),
      endTime: LocalDateTime = LocalDateTime.now(),
      bookingId: Long = 1,
      comment: String = "test",
      appointmentType: String = "ABC",
      repeatPeriod: RepeatPeriod? = null,
      count: Long? = null
    ): String {
      val fields = mutableMapOf<String, Any>(
        "locationId" to locationId,
        "startTime" to startTime,
        "endTime" to endTime,
        "bookingId" to bookingId,
        "comment" to comment,
        "appointmentType" to appointmentType,
      )

      repeatPeriod?.let { fields.set("repeat", mapOf("repeatPeriod" to repeatPeriod, "count" to count)) }

      return objectMapper.writeValueAsString(fields)
    }
  }

  @Nested
  inner class DeleteAppointment {
    @Test
    @WithMockUser(username = "ITAG_USER")
    fun `should delete an appointment`() {
      mockMvc.perform(
        delete("/appointment/$APPOINTMENT_ID")
          .contentType(MediaType.APPLICATION_JSON)
      )
        .andDo(print())
        .andExpect(status().isOk)

      verify(appointmentService).deleteAppointment(APPOINTMENT_ID)
    }

    @Test
    @WithMockUser(username = "ITAG_USER")
    fun `should delete a single appointment in a set of recurring appointments`() {
      mockMvc.perform(
        delete("/appointment/$APPOINTMENT_ID?deleteRelatedAppointments=false")
          .contentType(MediaType.APPLICATION_JSON)
      )
        .andDo(print())
        .andExpect(status().isOk)

      verify(appointmentService).deleteAppointment(APPOINTMENT_ID)
    }
  }

  @Nested
  inner class DeleteRecurringAppointmentSequence {
    @Test
    @WithMockUser(username = "ITAG_USER")
    fun `should delete the whole sequence of recurring appointments`() {
      mockMvc.perform(
        delete("/appointment/recurring/$RECURRING_APPOINTMENT_SEQUENCE_ID")
          .contentType(MediaType.APPLICATION_JSON)
      )
        .andDo(print())
        .andExpect(status().isOk)

      verify(appointmentService).deleteRecurringAppointmentSequence(RECURRING_APPOINTMENT_SEQUENCE_ID)
    }
  }

  companion object {
    private const val APPOINTMENT_ID = 1L
    private const val RECURRING_APPOINTMENT_SEQUENCE_ID = 100L
  }
}
