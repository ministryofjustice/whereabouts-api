package uk.gov.justice.digital.hmpps.whereabouts.controllers

import com.nhaarman.mockitokotlin2.whenever
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import uk.gov.justice.digital.hmpps.whereabouts.dto.BookingActivity
import uk.gov.justice.digital.hmpps.whereabouts.dto.attendance.AttendanceDetailsDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.attendance.AttendanceSummary
import uk.gov.justice.digital.hmpps.whereabouts.services.AttendanceService
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@WebMvcTest(AttendancesController::class)
class AttendancesControllerTest : TestController() {
  private val OFFENDER_NO = "A1234AB"
  private val START = LocalDate.of(2021, 3, 14)
  private val END = LocalDate.of(2021, 5, 24)
  private val MOORLAND = "HMP Moorland"
  private val BOOKING_ACTIVITIES = mutableSetOf(BookingActivity(1001, 1002))

  @MockBean
  lateinit var attendanceService: AttendanceService

  @Test
  @WithMockUser(username = "ITAG_USER")
  fun `getAttendances valid call returns expected data`() {
    whenever(attendanceService.getAttendanceAbsenceSummaryForOffender(OFFENDER_NO, START, END))
      .thenReturn(
        listOf(
          AttendanceSummary(
            month = YearMonth.of(2021, 5),
            acceptableAbsence = 6,
            unacceptableAbsence = 4,
            total = 23
          )
        )
      )

    mockMvc
      .perform(
        MockMvcRequestBuilders.get("/attendances/offender/$OFFENDER_NO/unacceptable-absence-count")
          .param("fromDate", START.format(DateTimeFormatter.ISO_LOCAL_DATE))
          .param("toDate", END.format(DateTimeFormatter.ISO_LOCAL_DATE))
      )
      .andExpect(MockMvcResultMatchers.status().isOk)
      .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize<Array<Any>>(1)))
      .andExpect(MockMvcResultMatchers.jsonPath("[0].month").value("2021-05"))
      .andExpect(MockMvcResultMatchers.jsonPath("[0].acceptableAbsence").value(6))
      .andExpect(MockMvcResultMatchers.jsonPath("[0].unacceptableAbsence").value(4))
      .andExpect(MockMvcResultMatchers.jsonPath("[0].total").value(23))
  }

  @Test
  @WithMockUser(username = "ITAG_USER")
  fun `getAttendances details valid call returns expected data`() {
    whenever(attendanceService.getAttendanceDetailsFromBookings(OFFENDER_NO, START, END))
      .thenReturn(
        listOf(
          AttendanceDetailsDto(
            eventDate = START,
            comments = "Test comment",
            bookingActivities = BOOKING_ACTIVITIES,
            locationId = 1,
            location = MOORLAND
          )
        )
      )

    mockMvc
      .perform(
        MockMvcRequestBuilders.get("/attendances/offender/$OFFENDER_NO/attendances-in-whereabouts-or-prisonapi")
          .param("offenderNo", OFFENDER_NO)
          .param("fromDate", START.format(DateTimeFormatter.ISO_LOCAL_DATE))
          .param("toDate", END.format((DateTimeFormatter.ISO_LOCAL_DATE)))
      )
      .andExpect(MockMvcResultMatchers.status().isOk)
      .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize<Array<Any>>(1)))
      .andExpect(MockMvcResultMatchers.jsonPath("[0].eventDate").value("2021-03-14"))
      .andExpect(MockMvcResultMatchers.jsonPath("[0].comments").value("Test comment"))
  }

  @Test
  fun `getAttendances details - unauthorided`() {
    whenever(attendanceService.getAttendanceDetailsFromBookings(OFFENDER_NO, START, END))
      .thenReturn(
        listOf(
          AttendanceDetailsDto(
            eventDate = START,
            comments = "Test comment",
            bookingActivities = BOOKING_ACTIVITIES,
            locationId = 1,
            location = MOORLAND
          )
        )
      )

    mockMvc
      .perform(
        MockMvcRequestBuilders.get("/attendances/offender/$OFFENDER_NO/attendances-in-whereabouts-or-prisonapi")
          .param("offenderNo", OFFENDER_NO)
          .param("fromDate", START.format(DateTimeFormatter.ISO_LOCAL_DATE))
          .param("toDate", END.format((DateTimeFormatter.ISO_LOCAL_DATE)))
      )
      .andExpect(MockMvcResultMatchers.status().isUnauthorized)
  }

  @Test
  @WithMockUser(username = "ITAG_USER")
  fun `getAttendances details - missing parameter`() {

    whenever(attendanceService.getAttendanceDetailsFromBookings(OFFENDER_NO, START, END))
      .thenReturn(
        listOf(
          AttendanceDetailsDto(
            eventDate = START,
            comments = "Test comment",
            bookingActivities = BOOKING_ACTIVITIES,
            locationId = 1,
            location = MOORLAND
          )
        )
      )

    mockMvc
      .perform(
        MockMvcRequestBuilders.get("/attendances/offender/$OFFENDER_NO/attendances-in-whereabouts-or-prisonapi")
          .param("offenderNo", OFFENDER_NO)
          .param("toDate", END.format((DateTimeFormatter.ISO_LOCAL_DATE)))
      )
      .andExpect(MockMvcResultMatchers.status().isBadRequest)
      .andExpect(MockMvcResultMatchers.jsonPath("developerMessage").value("Required request parameter 'fromDate' for method parameter type LocalDate is not present"))
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
      .andExpect(MockMvcResultMatchers.jsonPath("developerMessage").value("Required request parameter 'fromDate' for method parameter type LocalDate is not present"))
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
