package uk.gov.justice.digital.hmpps.whereabouts.integration

import com.github.tomakehurst.wiremock.client.WireMock
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpMethod
import uk.gov.justice.digital.hmpps.whereabouts.model.Attendance
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod
import uk.gov.justice.digital.hmpps.whereabouts.repository.AttendanceRepository
import uk.gov.justice.digital.hmpps.whereabouts.services.Stats
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
    elite2MockServer.stubGetScheduledActivitiesForDateRange(prisonId, fromDate, toDate, period)

    val response =
        restTemplate.exchange(
            "/attendance-statistics/$prisonId/over-date-range?fromDate={0}&toDate={1}&period={2}",
            HttpMethod.GET,
            createHeaderEntity(""),
            Any::class.java,
            fromDate,
            toDate,
            period
        )

    assertThat(response.statusCodeValue).isEqualTo(200)

    elite2MockServer.verify(WireMock.getRequestedFor(
        WireMock.urlEqualTo(
            "/api/schedules/$prisonId/activities-by-date-range?fromDate=$fromDate&toDate=$toDate&timeSlot=$period"
        )
    ))
  }

  @Test
  fun `should populate stats with data`() {
    elite2MockServer.stubGetScheduledActivitiesForDateRange(prisonId, fromDate, toDate, period)

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
        ))

    val response =
        restTemplate.exchange(
            "/attendance-statistics/$prisonId/over-date-range?fromDate={0}&toDate={1}&period={2}",
            HttpMethod.GET,
            createHeaderEntity(""),
            Stats::class.java,
            fromDate,
            toDate,
            period
        )

    assertThat(response.body?.paidReasons?.attended).isEqualTo(1)
    assertThat(response.statusCodeValue).isEqualTo(200)
  }
}
