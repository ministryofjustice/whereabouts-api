package uk.gov.justice.digital.hmpps.whereabouts.controllers

import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import uk.gov.justice.digital.hmpps.whereabouts.dto.attendance.AttendanceHistoryDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.attendance.AttendanceSummary
import uk.gov.justice.digital.hmpps.whereabouts.services.AttendanceService
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@WebMvcTest(
  AttendancesController::class,
  excludeAutoConfiguration = [SecurityAutoConfiguration::class, OAuth2ClientAutoConfiguration::class, OAuth2ResourceServerAutoConfiguration::class],
)
class AttendancesControllerTest : TestController() {
  private val offenderNo = "A1234AB"
  private val startDate = LocalDate.of(2021, 3, 14)
  private val endDate = LocalDate.of(2021, 5, 24)
  private val prisonName = "HMP Moorland"

  private val testAttendanceHistoryDto = PageImpl(
    listOf(
      AttendanceHistoryDto(
        eventDate = startDate,
        comments = "Test comment",
        location = prisonName,
        activity = "a",
        activityDescription = "d",
      ),
    ),
    PageRequest.of(0, 10),
    1,
  )

  @MockBean
  lateinit var attendanceService: AttendanceService

  @Test
  @WithMockUser(username = "ITAG_USER")
  fun `getAttendances valid call returns expected data`() {
    whenever(attendanceService.getAttendanceAbsenceSummaryForOffender(offenderNo, startDate, endDate))
      .thenReturn(
        AttendanceSummary(
          acceptableAbsence = 6,
          unacceptableAbsence = 4,
          total = 23,
        ),
      )

    mockMvc
      .perform(
        MockMvcRequestBuilders.get("/attendances/offender/$offenderNo/unacceptable-absence-count")
          .param("fromDate", startDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
          .param("toDate", endDate.format(DateTimeFormatter.ISO_LOCAL_DATE)),
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
    whenever(attendanceService.getAttendanceDetailsForOffender(offenderNo, startDate, endDate, pageable))
      .thenReturn(testAttendanceHistoryDto)

    mockMvc
      .perform(
        MockMvcRequestBuilders.get("/attendances/offender/$offenderNo/unacceptable-absences")
          .param("offenderNo", offenderNo)
          .param("fromDate", startDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
          .param("toDate", endDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
          .param("page", "0")
          .param("size", "10"),
      )
      .andExpect(MockMvcResultMatchers.status().isOk)
      .andExpect(MockMvcResultMatchers.jsonPath("content", Matchers.hasSize<Array<Any>>(1)))
      .andExpect(MockMvcResultMatchers.jsonPath("content[0].eventDate").value("2021-03-14"))
      .andExpect(MockMvcResultMatchers.jsonPath("content[0].comments").value("Test comment"))
  }

  @Test
  @WithMockUser(username = "ITAG_USER")
  fun `getAttendances details - missing parameter`() {
    mockMvc
      .perform(
        MockMvcRequestBuilders.get("/attendances/offender/$offenderNo/unacceptable-absences")
          .param("offenderNo", offenderNo)
          .param("toDate", endDate.format((DateTimeFormatter.ISO_LOCAL_DATE))),
      )
      .andExpect(MockMvcResultMatchers.status().isBadRequest)
      .andExpect(
        MockMvcResultMatchers.jsonPath("developerMessage")
          .value("Required request parameter 'fromDate' for method parameter type LocalDate is not present"),
      )
  }

  @Test
  @WithMockUser(username = "ITAG_USER")
  fun `getAttendances invalid call no fromDate`() {
    mockMvc
      .perform(
        MockMvcRequestBuilders.get("/attendances/offender/$offenderNo/unacceptable-absence-count")
          .param("toDate", endDate.format(DateTimeFormatter.ISO_LOCAL_DATE)),
      )
      .andExpect(MockMvcResultMatchers.status().isBadRequest)
      .andExpect(
        MockMvcResultMatchers.jsonPath("developerMessage")
          .value("Required request parameter 'fromDate' for method parameter type LocalDate is not present"),
      )
  }
}
