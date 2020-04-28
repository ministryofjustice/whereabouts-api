package uk.gov.justice.digital.hmpps.whereabouts.integration

import com.github.tomakehurst.wiremock.client.WireMock
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anySet
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpMethod
import uk.gov.justice.digital.hmpps.whereabouts.dto.*
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason
import uk.gov.justice.digital.hmpps.whereabouts.model.Attendance
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod
import uk.gov.justice.digital.hmpps.whereabouts.repository.AttendanceRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.stream.Collectors

class AttendancesIntegrationTest : IntegrationTest() {

  @MockBean
  lateinit var attendanceRepository: AttendanceRepository

  @Test
  fun `receive a list of attendance for a prison, location, date and period`() {
    elite2MockServer.stubUpdateAttendance()
    elite2MockServer.stubGetBooking()
    caseNotesMockServer.stubCreateCaseNote()

    whenever(attendanceRepository.findByPrisonIdAndEventLocationIdAndEventDateAndPeriod(any(), any(), any(), any()))
        .thenReturn(setOf(Attendance
            .builder()
            .id(1)
            .absentReason(AbsentReason.Refused)
            .period(TimePeriod.PM)
            .prisonId("LEI")
            .eventLocationId(2)
            .eventId(1)
            .eventDate(LocalDate.of(2019, 10, 10))
            .comments("hello world")
            .attended(false)
            .paid(false)
            .bookingId(1)
            .build()))

    webTestClient
            .get()
            .uri {
              it.path("/attendances/LEI/2")
                      .queryParam("date", LocalDate.of(2019, 10, 10))
                      .queryParam("period", TimePeriod.PM)
                      .build()
            }
            .headers(setHeaders())
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath(".attendances[0].id").isEqualTo(1)
            .jsonPath(".attendances[0].absentReason").isEqualTo("Refused")
            .jsonPath(".attendances[0].period").isEqualTo("PM")
            .jsonPath(".attendances[0].prisonId").isEqualTo("LEI")
            .jsonPath(".attendances[0].eventLocationId").isEqualTo(2)
            .jsonPath(".attendances[0].eventId").isEqualTo(1)
            .jsonPath(".attendances[0].eventDate").isEqualTo("2019-10-10")
            .jsonPath(".attendances[0].comments").isEqualTo("hello world")
            .jsonPath(".attendances[0].attended").isEqualTo(false)
            .jsonPath(".attendances[0].paid").isEqualTo(false)
            .jsonPath(".attendances[0].bookingId").isEqualTo(1)
            .jsonPath(".attendances[0].locked").isEqualTo(false)

  }


  @Test
  fun `should return modified by and modified on`() {
    elite2MockServer.stubUpdateAttendance()

    val date = LocalDateTime.now()

    whenever(attendanceRepository.findByPrisonIdAndEventLocationIdAndEventDateAndPeriod(any(), any(), any(), any()))
        .thenReturn(setOf(
            Attendance.builder()
                .id(1)
                .bookingId(1)
                .paid(false)
                .attended(false)
                .absentReason(AbsentReason.Refused)
                .comments("Refused")
                .eventDate(LocalDate.of(2019, 10, 10))
                .eventId(1)
                .eventLocationId(2)
                .prisonId("LEI")
                .period(TimePeriod.AM)
                .createDateTime(date)
                .createUserId("user")
                .modifyDateTime(date)
                .modifyUserId("user")
                .build()))

    webTestClient
        .get()
        .uri {
          it.path("/attendances/LEI/2")
          .queryParam("date", LocalDate.of(2019, 10, 10))
          .queryParam("period", TimePeriod.PM)
           .build()
        }
        .headers(setHeaders())
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath(".attendances[0].id").isEqualTo(1)
        .jsonPath(".attendances[0].modifyDateTime").isEqualTo(date.toString())
        .jsonPath(".attendances[0].modifyUserId").isEqualTo("user")
  }

  @Test
  fun `should return attendance information for set of bookings ids`() {
    elite2MockServer.stubUpdateAttendance()

    whenever(attendanceRepository.findByPrisonIdAndBookingIdInAndEventDateAndPeriod(any(), any(), any(), any()))
        .thenReturn(setOf(
            Attendance
                .builder()
                .id(1)
                .absentReason(AbsentReason.Refused)
                .period(TimePeriod.PM)
                .prisonId("LEI")
                .eventLocationId(2)
                .eventId(1)
                .eventDate(LocalDate.of(2019, 10, 10))
                .comments("hello world")
                .attended(true)
                .paid(true)
                .bookingId(1)
                .build(),
            Attendance
                .builder()
                .id(2)
                .absentReason(AbsentReason.Refused)
                .period(TimePeriod.PM)
                .prisonId("LEI")
                .eventLocationId(2)
                .eventId(1)
                .eventDate(LocalDate.of(2019, 10, 10))
                .comments("hello world")
                .attended(true)
                .paid(true)
                .bookingId(2)
                .build()))

    webTestClient
      .get()
      .uri("/attendances/LEI?date={0}&period={1}&bookings=${1}&bookings=${2}", LocalDate.of(2019, 10, 10), TimePeriod.PM)
      .headers(setHeaders())
      .exchange()
      .expectStatus().isOk()
      .expectBody()
      .jsonPath(".attendances[0].id").isEqualTo(1)
      .jsonPath(".attendances[1].id").isEqualTo(2)
      .jsonPath(".attendances[0].bookingId").isEqualTo(1)
      .jsonPath(".attendances[1].bookingId").isEqualTo(2)
  }

  @Test
  fun `should return attendance information for set of bookings ids by post`() {
    elite2MockServer.stubUpdateAttendance()

    whenever(attendanceRepository.findByPrisonIdAndBookingIdInAndEventDateAndPeriod(any(), any(), any(), any()))
        .thenReturn(setOf(
            Attendance
                .builder()
                .id(1)
                .absentReason(AbsentReason.Refused)
                .period(TimePeriod.PM)
                .prisonId("LEI")
                .eventLocationId(2)
                .eventId(1)
                .eventDate(LocalDate.of(2019, 10, 10))
                .comments("hello world")
                .attended(true)
                .paid(true)
                .bookingId(1)
                .build(),
            Attendance
                .builder()
                .id(2)
                .absentReason(AbsentReason.Refused)
                .period(TimePeriod.PM)
                .prisonId("LEI")
                .eventLocationId(2)
                .eventId(1)
                .eventDate(LocalDate.of(2019, 10, 10))
                .comments("hello world")
                .attended(true)
                .paid(true)
                .bookingId(2)
                .build()
        ))

    val bookings = setOf(1, 2)

    webTestClient
      .post()
      .uri("/attendances/LEI?date={0}&period={1}", LocalDate.of(2019, 10, 10), TimePeriod.PM)
      .headers(setHeaders())
      .bodyValue(bookings)
      .exchange()
      .expectBody()
      .jsonPath(".attendances[0].id").isEqualTo(1)
      .jsonPath(".attendances[1].id").isEqualTo(2)
      .jsonPath(".attendances[0].bookingId").isEqualTo(1)
      .jsonPath(".attendances[1].bookingId").isEqualTo(2)
  }

  @Test
  fun `should return attendance information for set of bookings ids over date range by post`() {
    elite2MockServer.stubUpdateAttendance()

    whenever(attendanceRepository.findByPrisonIdAndBookingIdInAndEventDateBetweenAndPeriodIn(any(), any(), any(), any(), any()))
            .thenReturn(setOf(
                    Attendance
                            .builder()
                            .id(1)
                            .absentReason(AbsentReason.Refused)
                            .period(TimePeriod.PM)
                            .prisonId("LEI")
                            .eventLocationId(2)
                            .eventId(1)
                            .eventDate(LocalDate.of(2019, 10, 10))
                            .comments("hello world")
                            .attended(true)
                            .paid(true)
                            .bookingId(1)
                            .build(),
                    Attendance
                            .builder()
                            .id(2)
                            .absentReason(AbsentReason.Refused)
                            .period(TimePeriod.PM)
                            .prisonId("LEI")
                            .eventLocationId(2)
                            .eventId(1)
                            .eventDate(LocalDate.of(2019, 10, 11))
                            .comments("hello world")
                            .attended(true)
                            .paid(true)
                            .bookingId(2)
                            .build()
            ))

    val bookings = setOf(1, 2)

    webTestClient
            .post()
            .uri("/attendances/LEI/attendance-over-date-range?fromDate={0}&toDate={1}&period={2}",
                    LocalDate.of(2019, 10, 10),
                    LocalDate.of(2019, 10, 11),
                    TimePeriod.PM)
            .headers(setHeaders())
            .bodyValue(bookings)
            .exchange()
            .expectBody()
            .jsonPath(".attendances[0].id").isEqualTo(1)
            .jsonPath(".attendances[1].id").isEqualTo(2)
            .jsonPath(".attendances[0].bookingId").isEqualTo(1)
            .jsonPath(".attendances[1].bookingId").isEqualTo(2)
  }

  @Test
  fun `should create multiple attendances`() {
    val bookingIds = setOf(1L, 2L)

    elite2MockServer.stubUpdateAttendanceForBookingIds()

    val bookingActivities = bookingIds
        .stream()
        .map { BookingActivity(activityId = 2L, bookingId = it) }
        .collect(Collectors.toSet())

    val attendAll = AttendancesDto
        .builder()
        .eventDate(LocalDate.of(2019, 10, 10))
        .eventLocationId(1L)
        .prisonId("LEI")
        .period(TimePeriod.AM)
        .bookingActivities(bookingActivities)
        .attended(true)
        .paid(true)
        .build()

    webTestClient
      .post()
      .uri("/attendances")
      .headers(setHeaders())
      .bodyValue(attendAll)
      .exchange()
      .expectStatus().isCreated()

    elite2MockServer.verify(WireMock.putRequestedFor(WireMock.urlEqualTo("/api/bookings/activities/attendance"))
        .withRequestBody(WireMock.equalToJson(gson.toJson(mapOf(
            "performance" to "STANDARD",
            "bookingActivities" to bookingActivities,
            "eventOutcome" to "ATT"
        )))))

    verify(attendanceRepository).saveAll(anySet())
  }

  @Test
  fun `should return attendance information for offenders that have scheduled activity`() {
    elite2MockServer.stubGetScheduledActivities()

    val prisonId = "MDI"
    val date = LocalDate.now()
    val period = TimePeriod.AM

    whenever(attendanceRepository.findByPrisonIdAndBookingIdInAndEventDateAndPeriod(any(), anySet(), any(), any()))
        .thenReturn(setOf(Attendance
            .builder()
            .id(1)
            .absentReason(AbsentReason.Refused)
            .attended(false)
            .paid(false)
            .eventId(2)
            .eventLocationId(3)
            .period(period)
            .prisonId(prisonId)
            .bookingId(1L)
            .eventDate(date)
            .caseNoteId(1)
            .build()))

    webTestClient
      .get()
      .uri("/attendances/$prisonId/attendance-for-scheduled-activities?date=$date&period=$period")
      .headers(setHeaders())
      .exchange()
      .expectStatus().isOk()
      .expectBody()
      .jsonPath(".attendances[0].id").isEqualTo(1)

    elite2MockServer.verify(WireMock.getRequestedFor(WireMock.urlEqualTo("/api/schedules/$prisonId/activities?date=$date&timeSlot=$period")))
  }

  @Test
  fun `should return absences for scheduled activity`() {
    val prisonId = "MDI"
    val date = LocalDate.now()
    val period = TimePeriod.AM
    val reason = AbsentReason.Refused

    elite2MockServer.stubGetScheduledActivitiesForDateRange(prisonId, date, date, period, true)

    whenever(attendanceRepository.findByPrisonIdAndEventDateBetweenAndPeriodInAndAbsentReason(any(), any(), any(), anySet(), any()))
        .thenReturn(setOf(
            Attendance
                .builder()
                .id(1)
                .absentReason(reason)
                .attended(false)
                .paid(false)
                .eventId(1)
                .eventDate(date)
                .eventLocationId(3)
                .period(period)
                .prisonId(prisonId)
                .bookingId(1L)
                .caseNoteId(1)
                .build()))

    webTestClient
      .get()
      .uri("/attendances/${prisonId}/absences-for-scheduled-activities/${reason}?fromDate=$date&period=$period")
      .headers(setHeaders())
      .exchange()
      .expectStatus().isOk()
      .expectBody()
      .jsonPath(".absences[0].attendanceId").isEqualTo(1)

  }
}
