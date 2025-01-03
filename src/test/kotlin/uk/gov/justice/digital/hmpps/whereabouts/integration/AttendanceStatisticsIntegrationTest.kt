package uk.gov.justice.digital.hmpps.whereabouts.integration

import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.matching.EqualToPattern
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.boot.test.mock.mockito.MockBean
import uk.gov.justice.digital.hmpps.whereabouts.model.Attendance
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod
import uk.gov.justice.digital.hmpps.whereabouts.repository.AttendanceRepository
import java.time.LocalDate

class AttendanceStatisticsIntegrationTest : IntegrationTest() {
  @MockBean
  private lateinit var attendanceRepository: AttendanceRepository

  private val prisonId = "MDI"
  private val fromDate = LocalDate.of(2019, 10, 10)
  private val toDate = LocalDate.of(2019, 10, 11)
  private val period = TimePeriod.AM

  @Test
  fun `should request schedules by date range`() {
    prisonApiMockServer.stubScheduleActivityCount(prisonId)

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
          .build(),
      ),
    )

    webTestClient.get()
      .uri("/attendance-statistics/$prisonId/over-date-range?fromDate=$fromDate&toDate=$toDate&period=$period")
      .headers(setHeaders())
      .exchange()
      .expectStatus().isOk

    prisonApiMockServer.verify(
      postRequestedFor(
        urlEqualTo(
          "/api/schedules/$prisonId/count-activities?fromDate=$fromDate&toDate=$toDate&timeSlots=$period",
        ),
      )
        .withRequestBody(equalToJson("""{"1":1}""")),
    )
  }

  @Test
  fun `should request schedules by date range no period specified`() {
    prisonApiMockServer.stubScheduleActivityCount(prisonId)

    webTestClient.get()
      .uri("/attendance-statistics/$prisonId/over-date-range?fromDate=$fromDate&toDate=$toDate")
      .headers(setHeaders())
      .exchange()
      .expectStatus().isOk

    prisonApiMockServer.verify(
      postRequestedFor(
        urlPathEqualTo(
          "/api/schedules/$prisonId/count-activities",
        ),
      )
        .withQueryParam("fromDate", EqualToPattern(fromDate.toString()))
        .withQueryParam("toDate", EqualToPattern(toDate.toString()))
        .withQueryParam("timeSlots", EqualToPattern("AM"))
        .withQueryParam("timeSlots", EqualToPattern("PM")),
    )
  }

  @Test
  fun `should populate stats with data`() {
    prisonApiMockServer.stubScheduleActivityCount(prisonId)

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
          .build(),
      ),
    )

    webTestClient.get()
      .uri("/attendance-statistics/$prisonId/over-date-range?fromDate=$fromDate&toDate=$toDate&period=$period")
      .headers(setHeaders())
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.attended").isEqualTo(1)
      .jsonPath("$.suspended").isEqualTo(2)
      .jsonPath("$.notRecorded").isEqualTo(5)
      .jsonPath("$.scheduleActivities").isEqualTo(13)
      .jsonPath("$.paidReasons.acceptableAbsenceDescription").isEqualTo("Acceptable absence")
      .jsonPath("$.unpaidReasons.refusedIncentiveLevelWarningDescription").isEqualTo("Refused to attend - incentive level warning added")
  }
}
