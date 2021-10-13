package uk.gov.justice.digital.hmpps.whereabouts.controllers

import com.nhaarman.mockitokotlin2.whenever
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
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
        MockMvcRequestBuilders.get("/attendances/offender/$OFFENDER_NO/unacceptableAbsenceCount")
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
  fun `getAttendances invalid call no fromDate`() {

    mockMvc
      .perform(
        MockMvcRequestBuilders.get("/attendances/offender/$OFFENDER_NO/unacceptableAbsenceCount")
          .param("toDate", END.format(DateTimeFormatter.ISO_LOCAL_DATE))
      )
      .andExpect(MockMvcResultMatchers.status().isBadRequest)
      .andExpect(MockMvcResultMatchers.jsonPath("developerMessage").value("Required request parameter 'fromDate' for method parameter type LocalDate is not present"))
  }

  @Test
  fun `getAttendances unauthorised`() {
    mockMvc
      .perform(
        MockMvcRequestBuilders.get("/attendances/offender/$OFFENDER_NO/unacceptableAbsenceCount")
          .param("fromDate", START.format(DateTimeFormatter.ISO_LOCAL_DATE))
          .param("toDate", END.format(DateTimeFormatter.ISO_LOCAL_DATE))
      )
      .andExpect(MockMvcResultMatchers.status().isUnauthorized)
  }
}
