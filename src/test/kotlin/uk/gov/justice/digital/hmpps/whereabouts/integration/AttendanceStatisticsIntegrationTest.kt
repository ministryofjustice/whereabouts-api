package uk.gov.justice.digital.hmpps.whereabouts.integration

import com.github.tomakehurst.wiremock.client.WireMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import uk.gov.justice.digital.hmpps.whereabouts.model.Attendance
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod
import uk.gov.justice.digital.hmpps.whereabouts.repository.AttendanceRepository
import uk.gov.justice.digital.hmpps.whereabouts.services.Stats
import java.time.LocalDate

class AttendanceStatisticsIntegrationTest : IntegrationTest() {

  @Autowired
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
            "/attendance-statistics/counts-over-date-range/$prisonId/?fromDate={0}&toDate={1}&period={2}",
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
    val attendance = attendanceRepository.save(
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

    elite2MockServer.stubGetScheduledActivitiesForDateRange(prisonId, fromDate, toDate, period)

    val response =
        restTemplate.exchange(
            "/attendance-statistics/counts-over-date-range/$prisonId/?fromDate={0}&toDate={1}&period={2}",
            HttpMethod.GET,
            createHeaderEntity(""),
            Stats::class.java,
            fromDate,
            toDate,
            period
        )

    assertThat(response.body?.paidReasons?.attended).isEqualTo(1)
    assertThat(response.statusCodeValue).isEqualTo(200)

    attendanceRepository.delete(attendance)
  }
}