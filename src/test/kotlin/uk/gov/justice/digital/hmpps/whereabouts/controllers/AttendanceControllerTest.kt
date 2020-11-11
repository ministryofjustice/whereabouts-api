package uk.gov.justice.digital.hmpps.whereabouts.controllers

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.whenever
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyLong
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod
import uk.gov.justice.digital.hmpps.whereabouts.services.AttendanceExists
import uk.gov.justice.digital.hmpps.whereabouts.services.AttendanceLocked
import uk.gov.justice.digital.hmpps.whereabouts.services.AttendanceNotFound
import uk.gov.justice.digital.hmpps.whereabouts.services.AttendanceService
import java.time.LocalDate

@WebMvcTest(AttendanceController::class)
class AttendanceControllerTest : TestController() {
  @MockBean
  lateinit var attendanceService: AttendanceService

  @Test
  @WithMockUser(username = "ITAG_USER")
  fun `should return a bad request when the 'eventId' is missing`() {
    mockMvc.perform(
      post("/attendance")
        .contentType(MediaType.APPLICATION_JSON)
        .content(
          gson.toJson(
            mapOf(
              "bookingId" to 1,
              "prisonId" to "LEI",
              "eventLocationId" to 2,
              "eventDate" to LocalDate.of(2010, 10, 10),
              "period" to TimePeriod.AM,
              "attended" to true,
              "paid" to true
            )
          )
        )
    )
      .andDo(print())
      .andExpect(
        status()
          .isBadRequest
      )
  }

  @Test
  @WithMockUser(username = "ITAG_USER")
  fun `should return a bad request when the 'eventLocationId' is missing`() {
    mockMvc.perform(
      post("/attendance")
        .contentType(MediaType.APPLICATION_JSON)
        .content(
          gson.toJson(
            mapOf(
              "bookingId" to 1,
              "prisonId" to "LEI",
              "eventId" to 1,
              "eventDate" to LocalDate.of(2010, 10, 10),
              "period" to TimePeriod.AM,
              "attended" to true,
              "paid" to true
            )
          )
        )
    )
      .andDo(print())
      .andExpect(
        status()
          .isBadRequest
      )
  }

  @Test
  @WithMockUser(username = "ITAG_USER")
  fun `should return a bad request when the 'eventDate' is missing`() {
    mockMvc.perform(
      post("/attendance")
        .contentType(MediaType.APPLICATION_JSON)
        .content(
          gson.toJson(
            mapOf(
              "bookingId" to 1,
              "prisonId" to "LEI",
              "eventId" to 1,
              "eventLocationId" to 1,
              "period" to TimePeriod.AM,
              "attended" to true,
              "paid" to true
            )
          )
        )
    )
      .andDo(print())
      .andExpect(
        status()
          .isBadRequest
      )
  }

  @Test
  @WithMockUser(username = "ITAG_USER")
  fun `should return a bad request when the 'period' is missing`() {
    mockMvc.perform(
      post("/attendance")
        .contentType(MediaType.APPLICATION_JSON)
        .content(
          gson.toJson(
            mapOf(
              "bookingId" to 1,
              "prisonId" to "LEI",
              "eventId" to 1,
              "eventLocationId" to 1,
              "eventDate" to LocalDate.of(2019, 10, 10),
              "attended" to true,
              "paid" to true
            )
          )
        )
    )
      .andDo(print())
      .andExpect(
        status()
          .isBadRequest
      )
  }

  @Test
  @WithMockUser(username = "ITAG_USER")
  fun `should return a bad request when the 'bookingId' is missing`() {
    mockMvc.perform(
      post("/attendance")
        .contentType(MediaType.APPLICATION_JSON)
        .content(
          gson.toJson(
            mapOf(
              "prisonId" to "LEI",
              "eventId" to 2,
              "eventLocationId" to 2,
              "eventDate" to LocalDate.of(2010, 10, 10),
              "period" to TimePeriod.AM,
              "attended" to true,
              "paid" to true
            )
          )
        )
    )
      .andDo(print())
      .andExpect(
        status()
          .isBadRequest
      )
  }

  @Test
  @WithMockUser(username = "ITAG_USER")
  fun `should return a bad request when the 'prisonId' is missing`() {
    mockMvc.perform(
      post("/attendance")
        .contentType(MediaType.APPLICATION_JSON)
        .content(
          gson.toJson(
            mapOf(
              "bookingId" to 1,
              "eventId" to 2,
              "eventLocationId" to 2,
              "eventDate" to LocalDate.of(2010, 10, 10),
              "period" to TimePeriod.AM,
              "attended" to true,
              "paid" to true
            )
          )
        )
    )
      .andDo(print())
      .andExpect(
        status()
          .isBadRequest
      )
  }

  @Test
  @WithMockUser(username = "ITAG_USER")
  fun `should return a 404 when attempting to update non existent attendance`() {
    whenever(attendanceService.updateAttendance(anyLong(), any()))
      .thenThrow(AttendanceNotFound())

    mockMvc.perform(
      put("/attendance/1")
        .contentType(MediaType.APPLICATION_JSON)
        .content(
          gson.toJson(
            mapOf(
              "attended" to true,
              "paid" to true
            )
          )
        )
    )
      .andDo(print())
      .andExpect(status().isNotFound)
  }

  @Test
  @WithMockUser(username = "ITAG_USER")
  fun `should return a http conflict when attendance already exists`() {
    whenever(attendanceService.createAttendance(any())).thenThrow(AttendanceExists())

    mockMvc.perform(
      post("/attendance")
        .contentType(MediaType.APPLICATION_JSON)
        .content(
          gson.toJson(
            mapOf(
              "prisonId" to "LEI",
              "attended" to true,
              "paid" to true,
              "bookingId" to 1,
              "eventId" to 2,
              "eventLocationId" to 1,
              "period" to TimePeriod.AM,
              "eventDate" to LocalDate.now().toString()
            )
          )
        )
    )
      .andDo(print())
      .andExpect(status().isConflict)
      .andExpect(jsonPath(".developerMessage").value("Attendance already exists"))
  }

  @Test
  @WithMockUser(username = "ITAG_USER")
  fun `should return a http bad request when attempting to update a locked attendance record`() {
    whenever(attendanceService.updateAttendance(anyLong(), any())).thenThrow(AttendanceLocked())

    val lockedMessage = "Attendance record is locked"
    mockMvc.perform(
      put("/attendance/1")
        .contentType(MediaType.APPLICATION_JSON)
        .content(
          gson.toJson(
            mapOf(
              "attended" to true,
              "paid" to true,
            )
          )
        )
    )
      .andDo(print())
      .andExpect(status().isBadRequest)
      .andExpect(jsonPath(".developerMessage").value(lockedMessage))
      .andExpect(jsonPath(".userMessage").value(lockedMessage))
  }
}
