package uk.gov.justice.digital.hmpps.whereabouts.integration

import com.github.tomakehurst.wiremock.client.WireMock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.junit.jupiter.api.Test
import org.springframework.boot.test.mock.mockito.MockBean
import uk.gov.justice.digital.hmpps.whereabouts.model.Attendance
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod
import uk.gov.justice.digital.hmpps.whereabouts.repository.AttendanceRepository
import java.time.LocalDate

class AttendanceStatisticsIntegrationTest : IntegrationTest() {

  @MockBean
  lateinit var attendanceRepository: AttendanceRepository

  private val prisonId = "MDI"
  private val fromDate = LocalDate.of(2019, 10, 10)
  private val toDate = LocalDate.of(2019, 10, 11)
  private val period = TimePeriod.AM

  @Test
  fun `should request schedules by date range`() {
    prisonApiMockServer.stubGetScheduledActivitiesForDateRange(prisonId, fromDate, toDate, period, true)

    webTestClient.get()
      .uri("/attendance-statistics/$prisonId/over-date-range?fromDate=$fromDate&toDate=$toDate&period=$period")
      .headers(setHeaders())
      .exchange()
      .expectStatus().isOk

    prisonApiMockServer.verify(
      WireMock.getRequestedFor(
        WireMock.urlEqualTo(
          "/api/schedules/$prisonId/activities-by-date-range?fromDate=$fromDate&toDate=$toDate&timeSlot=$period&includeSuspended=true"
        )
      )
    )
  }

  @Test
  fun `should populate stats with data`() {
    prisonApiMockServer.stubGetScheduledActivitiesForDateRange(prisonId, fromDate, toDate, period, true)

    whenever(attendanceRepository.findByPrisonIdAndPeriodAndEventDateBetween(any(), any(), any(), any())).thenReturn(
      setOf(
        Attendance
          .builder()
          .bookingId(1)
          .attended(true)
          .prisonId(prisonId)
          .period(period)
          .eventDate(fromDate)
          .eventId(1)
          .eventLocationId(1)
          .build()
      )
    )

    webTestClient.get()
      .uri("/attendance-statistics/$prisonId/over-date-range?fromDate=$fromDate&toDate=$toDate&period=$period")
      .headers(setHeaders())
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.paidReasons.attended").isEqualTo(1)
  }
}
