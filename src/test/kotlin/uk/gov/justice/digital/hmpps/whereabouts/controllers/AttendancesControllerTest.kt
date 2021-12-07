package uk.gov.justice.digital.hmpps.whereabouts.controllers

import com.nhaarman.mockitokotlin2.whenever
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import uk.gov.justice.digital.hmpps.whereabouts.dto.BookingActivity
import uk.gov.justice.digital.hmpps.whereabouts.dto.attendance.AttendanceHistoryDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.attendance.AttendanceSummary
import uk.gov.justice.digital.hmpps.whereabouts.services.AttendanceService
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@WebMvcTest(AttendancesController::class)
class AttendancesControllerTest : TestController() {
  private val OFFENDER_NO = "A1234AB"
  private val START = LocalDate.of(2021, 3, 14)
  private val END = LocalDate.of(2021, 5, 24)
  private val MOORLAND = "HMP Moorland"
  private val BOOKING_ACTIVITIES = mutableSetOf(BookingActivity(1001, 1002))
  private val testAttendanceHistoryDto = PageImpl(
    listOf(
      AttendanceHistoryDto(
        eventDate = START,
        comments = "Test comment",
        location = MOORLAND,
        activity = "a",
        activityDescription = "d",
      )
    )
  )

  @MockBean
  lateinit var attendanceService: AttendanceService

  @Test
  @WithMockUser(username = "ITAG_USER")
  fun `getAttendances valid call returns expected data`() {
    whenever(attendanceService.getAttendanceAbsenceSummaryForOffender(OFFENDER_NO, START, END))
      .thenReturn(
        AttendanceSummary(
          acceptableAbsence = 6,
          unacceptableAbsence = 4,
          total = 23
        )
      )

    mockMvc
      .perform(
        MockMvcRequestBuilders.get("/attendances/offender/$OFFENDER_NO/unacceptable-absence-count")
          .param("fromDate", START.format(DateTimeFormatter.ISO_LOCAL_DATE))
          .param("toDate", END.format(DateTimeFormatter.ISO_LOCAL_DATE))
      )
      .andExpect(MockMvcResultMatchers.status().isOk)
      .andExpect(MockMvcResultMatchers.jsonPath("$.acceptableAbsence").value(6))
      .andExpect(MockMvcResultMatchers.jsonPath("$.unacceptableAbsence").value(4))
      .andExpect(MockMvcResultMatchers.jsonPath("$.total").value(23))
  }

  val pageable = Pageable.ofSize(10)

  @Test
  @WithMockUser(username = "ITAG_USER")
  fun `getAttendances details valid call returns expected data`() {
    whenever(attendanceService.getAttendanceDetailsForOffender(OFFENDER_NO, START, END, pageable))
      .thenReturn(testAttendanceHistoryDto)

    val result = mockMvc
      .perform(
        MockMvcRequestBuilders.get("/attendances/offender/$OFFENDER_NO/unacceptable-absences")
          .param("offenderNo", OFFENDER_NO)
          .param("fromDate", START.format(DateTimeFormatter.ISO_LOCAL_DATE))
          .param("toDate", END.format(DateTimeFormatter.ISO_LOCAL_DATE))
          .param("page", "0")
          .param("size", "10")
      )
      .andExpect(MockMvcResultMatchers.status().isOk)
      .andExpect(MockMvcResultMatchers.jsonPath("content", Matchers.hasSize<Array<Any>>(1)))
      .andExpect(MockMvcResultMatchers.jsonPath("content[0].eventDate").value("2021-03-14"))
      .andExpect(MockMvcResultMatchers.jsonPath("content[0].comments").value("Test comment"))
  }

  @Test
  fun `getAttendances details - unauthorised`() {
    whenever(attendanceService.getAttendanceDetailsForOffender(OFFENDER_NO, START, END, pageable))
      .thenReturn(testAttendanceHistoryDto)

    mockMvc
      .perform(
        MockMvcRequestBuilders.get("/attendances/offender/$OFFENDER_NO/unacceptable-absences")
          .param("offenderNo", OFFENDER_NO)
          .param("fromDate", START.format(DateTimeFormatter.ISO_LOCAL_DATE))
          .param("toDate", END.format((DateTimeFormatter.ISO_LOCAL_DATE)))
      )
      .andExpect(MockMvcResultMatchers.status().isUnauthorized)
  }

  @Test
  @WithMockUser(username = "ITAG_USER")
  fun `getAttendances details - missing parameter`() {

    whenever(attendanceService.getAttendanceDetailsForOffender(OFFENDER_NO, START, END, pageable))
      .thenReturn(testAttendanceHistoryDto)

    mockMvc
      .perform(
        MockMvcRequestBuilders.get("/attendances/offender/$OFFENDER_NO/unacceptable-absences")
          .param("offenderNo", OFFENDER_NO)
          .param("toDate", END.format((DateTimeFormatter.ISO_LOCAL_DATE)))
      )
      .andExpect(MockMvcResultMatchers.status().isBadRequest)
      .andExpect(
        MockMvcResultMatchers.jsonPath("developerMessage")
          .value("Required request parameter 'fromDate' for method parameter type LocalDate is not present")
      )
  }

  @Test
  @WithMockUser(username = "ITAG_USER")
  fun `getAttendances invalid call no fromDate`() {

    mockMvc
      .perform(
        MockMvcRequestBuilders.get("/attendances/offender/$OFFENDER_NO/unacceptable-absence-count")
          .param("toDate", END.format(DateTimeFormatter.ISO_LOCAL_DATE))
      )
      .andExpect(MockMvcResultMatchers.status().isBadRequest)
      .andExpect(
        MockMvcResultMatchers.jsonPath("developerMessage")
          .value("Required request parameter 'fromDate' for method parameter type LocalDate is not present")
      )
  }

  @Test
  fun `getAttendances unauthorised`() {
    mockMvc
      .perform(
        MockMvcRequestBuilders.get("/attendances/offender/$OFFENDER_NO/unacceptable-absence-count")
          .param("fromDate", START.format(DateTimeFormatter.ISO_LOCAL_DATE))
          .param("toDate", END.format(DateTimeFormatter.ISO_LOCAL_DATE))
      )
      .andExpect(MockMvcResultMatchers.status().isUnauthorized)
  }
}
