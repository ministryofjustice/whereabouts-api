package uk.gov.justice.digital.hmpps.whereabouts.integration

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anySet
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.mock.mockito.MockBean
import uk.gov.justice.digital.hmpps.whereabouts.dto.BookingActivity
import uk.gov.justice.digital.hmpps.whereabouts.dto.attendance.AttendancesDto
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentSubReason
import uk.gov.justice.digital.hmpps.whereabouts.model.Attendance
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod
import uk.gov.justice.digital.hmpps.whereabouts.repository.AttendanceRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.stream.Collectors

class AttendancesIntegrationTest : IntegrationTest() {

  @MockBean
  lateinit var attendanceRepository: AttendanceRepository

  @Test
  fun `receive a list of attendance for a prison, location, date and period`() {
    prisonApiMockServer.stubUpdateAttendance()
    prisonApiMockServer.stubGetBooking()
    caseNotesMockServer.stubCreateCaseNote()

    whenever(attendanceRepository.findByPrisonIdAndEventLocationIdAndEventDateAndPeriod(any(), any(), any(), any()))
      .thenReturn(
        setOf(
          Attendance
            .builder()
            .id(1)
            .absentReason(AbsentReason.Refused)
            .absentSubReason(AbsentSubReason.ExternalMoves)
            .period(TimePeriod.PM)
            .prisonId("LEI")
            .eventLocationId(2)
            .eventId(1)
            .eventDate(LocalDate.of(2019, 10, 10))
            .comments("hello world")
            .attended(false)
            .paid(false)
            .bookingId(1)
            .build()
        )
      )

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
      .expectStatus().isOk
      .expectBody()
      .jsonPath(".attendances[0].id").isEqualTo(1)
      .jsonPath(".attendances[0].absentReason").isEqualTo("Refused")
      .jsonPath(".attendances[0].absentReasonDescription").isEqualTo("Refused to attend")
      .jsonPath(".attendances[0].absentSubReason").isEqualTo("ExternalMoves")
      .jsonPath(".attendances[0].absentSubReasonDescription").isEqualTo("External moves")
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
    prisonApiMockServer.stubUpdateAttendance()

    val date = LocalDateTime.now()

    whenever(attendanceRepository.findByPrisonIdAndEventLocationIdAndEventDateAndPeriod(any(), any(), any(), any()))
      .thenReturn(
        setOf(
          Attendance.builder()
            .id(1)
            .bookingId(1)
            .paid(false)
            .attended(false)
            .absentReason(AbsentReason.Refused)
            .absentSubReason(AbsentSubReason.ExternalMoves)
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
            .build()
        )
      )

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
      .expectStatus().isOk
      .expectBody()
      .jsonPath(".attendances[0].id").isEqualTo(1)
      .jsonPath(".attendances[0].modifyDateTime").isNotEmpty
      .jsonPath(".attendances[0].modifyUserId").isEqualTo("user")
  }

  @Test
  fun `should return attendance information for set of bookings ids`() {
    prisonApiMockServer.stubUpdateAttendance()

    whenever(attendanceRepository.findByPrisonIdAndBookingIdInAndEventDateAndPeriod(any(), any(), any(), any()))
      .thenReturn(
        setOf(
          Attendance
            .builder()
            .id(1)
            .absentReason(AbsentReason.Refused)
            .absentSubReason(AbsentSubReason.ExternalMoves)
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
            .absentSubReason(AbsentSubReason.ExternalMoves)
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
        )
      )

    webTestClient
      .get()
      .uri {
        it.path("/attendances/LEI")
          .queryParam("date", LocalDate.of(2019, 10, 10))
          .queryParam("period", TimePeriod.PM)
          .queryParam("bookings", setOf(1, 2))
          .build()
      }
      .headers(setHeaders())
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath(".attendances[0].id").isEqualTo(1)
      .jsonPath(".attendances[1].id").isEqualTo(2)
      .jsonPath(".attendances[0].bookingId").isEqualTo(1)
      .jsonPath(".attendances[1].bookingId").isEqualTo(2)
  }

  @Test
  fun `should return attendance information for set of bookings ids by post`() {
    prisonApiMockServer.stubUpdateAttendance()

    whenever(attendanceRepository.findByPrisonIdAndBookingIdInAndEventDateAndPeriod(any(), any(), any(), any()))
      .thenReturn(
        setOf(
          Attendance
            .builder()
            .id(1)
            .absentReason(AbsentReason.Refused)
            .absentSubReason(AbsentSubReason.ExternalMoves)
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
            .absentReason(AbsentReason.UnacceptableAbsenceIncentiveLevelWarning)
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
        )
      )

    val bookings = setOf(1, 2)

    webTestClient
      .post()
      .uri {
        it.path("/attendances/LEI")
          .queryParam("date", LocalDate.of(2019, 10, 10))
          .queryParam("period", TimePeriod.PM)
          .build()
      }
      .headers(setHeaders())
      .bodyValue(bookings)
      .exchange()
      .expectBody()
      .jsonPath(".attendances[0].id").isEqualTo(1)
      .jsonPath(".attendances[0].bookingId").isEqualTo(1)
      .jsonPath(".attendances[1].id").isEqualTo(2)
      .jsonPath(".attendances[1].bookingId").isEqualTo(2)
      .jsonPath(".attendances[0].absentReasonDescription").isEqualTo("Refused to attend")
      .jsonPath(".attendances[1].absentReasonDescription").isEqualTo("Unacceptable absence - incentive level warning")
      .jsonPath(".attendances[0].absentSubReasonDescription").isEqualTo("External moves")
      .jsonPath(".attendances[1].absentSubReasonDescription").isEmpty
  }

  @Test
  fun `should return attendance information for set of bookings ids over date range by post`() {
    prisonApiMockServer.stubUpdateAttendance()

    whenever(
      attendanceRepository.findByPrisonIdAndBookingIdInAndEventDateBetweenAndPeriodIn(
        any(),
        any(),
        any(),
        any(),
        any()
      )
    )
      .thenReturn(
        setOf(
          Attendance
            .builder()
            .id(1)
            .absentReason(AbsentReason.Refused)
            .absentSubReason(AbsentSubReason.ExternalMoves)
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
            .absentSubReason(AbsentSubReason.ExternalMoves)
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
        )
      )

    val bookings = setOf(1, 2)

    webTestClient
      .post()
      .uri {
        it.path("/attendances/LEI/attendance-over-date-range")
          .queryParam("fromDate", LocalDate.of(2019, 10, 10))
          .queryParam("toDate", LocalDate.of(2019, 10, 11))
          .queryParam("period", TimePeriod.PM)
          .build()
      }
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

    prisonApiMockServer.stubUpdateAttendanceForBookingIds()

    val bookingActivities = bookingIds
      .stream()
      .map { BookingActivity(activityId = 2L, bookingId = it) }
      .collect(Collectors.toSet())

    val attendAll = AttendancesDto(
      eventDate = LocalDate.of(2019, 10, 10),
      eventLocationId = 1L,
      prisonId = "LEI",
      period = TimePeriod.AM,
      bookingActivities = bookingActivities,
      attended = true,
      paid = true
    )

    webTestClient
      .post()
      .uri("/attendances")
      .headers(setHeaders())
      .bodyValue(attendAll)
      .exchange()
      .expectStatus().isCreated

    prisonApiMockServer.verify(
      WireMock.putRequestedFor(urlEqualTo("/api/bookings/activities/attendance"))
        .withRequestBody(
          WireMock.equalToJson(
            objectMapper.writeValueAsString(
              mapOf(
                "performance" to "STANDARD",
                "bookingActivities" to bookingActivities,
                "eventOutcome" to "ATT"
              )
            )
          )
        )
    )

    verify(attendanceRepository).saveAll(anySet())
  }

  @Test
  fun `should return attendance information for offenders that have scheduled activity`() {
    prisonApiMockServer.stubGetScheduledActivities()

    val prisonId = "MDI"
    val date = LocalDate.now()
    val period = TimePeriod.AM

    whenever(attendanceRepository.findByPrisonIdAndBookingIdInAndEventDateAndPeriod(any(), anySet(), any(), any()))
      .thenReturn(
        setOf(
          Attendance
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
            .build()
        )
      )

    webTestClient
      .get()
      .uri {
        it.path("/attendances/$prisonId/attendance-for-scheduled-activities")
          .queryParam("date", date)
          .queryParam("period", period)
          .build()
      }
      .headers(setHeaders())
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath(".attendances[0].id").isEqualTo(1)

    prisonApiMockServer.verify(getRequestedFor(urlEqualTo("/api/schedules/$prisonId/activities?date=$date&timeSlot=$period")))
  }

  @Test
  fun `should return prisoners that haven't attended a scheduled activity`() {
    prisonApiMockServer.stubGetScheduledActivities()

    val prisonId = "MDI"
    val date = LocalDate.now()
    val period = TimePeriod.AM

    whenever(attendanceRepository.findByPrisonIdAndPeriodAndEventDateBetween(any(), any(), any(), any()))
      .thenReturn(
        setOf(
          attendance.toBuilder().bookingId(1).eventId(2).build(),
        )
      )

    webTestClient
      .get()
      .uri {
        it.path("/attendances/$prisonId/unaccounted-for")
          .queryParam("date", date)
          .queryParam("period", period)
          .build()
      }
      .headers(setHeaders())
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .consumeWith(System.out::println)
      .jsonPath("\$.scheduled[*].offenderNo").isEqualTo("B123C")

    prisonApiMockServer.verify(getRequestedFor(urlEqualTo("/api/schedules/$prisonId/activities?date=$date&timeSlot=$period")))
  }

  @Test
  fun `should return absences for scheduled activity`() {
    val prisonId = "MDI"
    val date = LocalDate.now()
    val period = TimePeriod.AM
    val reason = AbsentReason.RefusedIncentiveLevelWarning

    prisonApiMockServer.stubGetScheduledActivitiesForDateRange(prisonId, date, date, period, true)

    whenever(
      attendanceRepository.findByPrisonIdAndEventDateBetweenAndPeriodInAndAbsentReason(
        any(),
        any(),
        any(),
        anySet(),
        any()
      )
    )
      .thenReturn(
        setOf(
          Attendance
            .builder()
            .id(1)
            .absentReason(reason)
            .absentSubReason(AbsentSubReason.Courses)
            .attended(false)
            .paid(false)
            .eventId(1)
            .eventDate(date)
            .eventLocationId(3)
            .period(period)
            .prisonId(prisonId)
            .bookingId(1L)
            .caseNoteId(1)
            .build()
        )
      )

    webTestClient
      .get()
      .uri {
        it.path("/attendances/$prisonId/absences-for-scheduled-activities/$reason")
          .queryParam("fromDate", date)
          .queryParam("period", period)
          .build()
      }
      .headers(setHeaders())
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath(".description").isEqualTo("Refused to attend - incentive level warning added")
      .jsonPath(".absences[0].attendanceId").isEqualTo(1)
      .jsonPath(".absences[0].subReasonDescription").isEqualTo("Courses, programmes and interventions")
  }

  @Test
  fun `should return attendance summary for offender`() {
    val offenderNo = "A1234AX"
    val START = LocalDate.of(2021, 3, 14)
    val END = LocalDate.of(2021, 5, 24)

    prisonApiMockServer.stubGetAttendanceForOffender(offenderNo, START, END)

    webTestClient
      .get()
      .uri {
        it.path("/attendances/offender/$offenderNo/unacceptable-absence-count")
          .queryParam("fromDate", START.format(DateTimeFormatter.ISO_LOCAL_DATE))
          .queryParam("toDate", END.format(DateTimeFormatter.ISO_LOCAL_DATE))
          .build()
      }
      .headers(setHeaders())
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.total").isEqualTo(5)
      .jsonPath("$.acceptableAbsence").isEqualTo(1)
      .jsonPath("$.unacceptableAbsence").isEqualTo(2)
  }

  @Test
  fun `should handle empty attendance summary data for offender`() {
    val offenderNo = "A1234AX"
    val START = LocalDate.of(2021, 3, 14)
    val END = LocalDate.of(2021, 5, 24)

    prisonApiMockServer.stubGetAttendanceForOffenderEmpty(offenderNo, START, END)

    webTestClient
      .get()
      .uri {
        it.path("/attendances/offender/$offenderNo/unacceptable-absence-count")
          .queryParam("fromDate", START.format(DateTimeFormatter.ISO_LOCAL_DATE))
          .queryParam("toDate", END.format(DateTimeFormatter.ISO_LOCAL_DATE))
          .build()
      }
      .headers(setHeaders())
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.total").isEqualTo(0)
      .jsonPath("$.acceptableAbsence").isEqualTo(0)
      .jsonPath("$.unacceptableAbsence").isEqualTo(0)
  }

  @Test
  fun `should return unacceptable absence details for offender`() {
    val offenderNo = "A1234AX"
    val START = LocalDate.of(2021, 3, 14)
    val END = LocalDate.of(2021, 5, 24)

    prisonApiMockServer.stubGetAttendanceForOffender(offenderNo, START, END, "UNACAB", "0", "20")

    webTestClient
      .get()
      .uri {
        it.path("/attendances/offender/$offenderNo/unacceptable-absences")
          .queryParam("fromDate", START.format(DateTimeFormatter.ISO_LOCAL_DATE))
          .queryParam("toDate", END.format(DateTimeFormatter.ISO_LOCAL_DATE))
          .build()
      }
      .headers(setHeaders())
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.totalPages").isEqualTo(1)
      .jsonPath("$.content[0].activity").isEqualTo("act 1")
      .jsonPath("$.content[2].comments").isEqualTo("comment 3")
      .jsonPath("$.content[3].comments").isEmpty
      .jsonPath("$.content[5].activity").isEqualTo("act 6")
  }

  private val attendance = Attendance.builder()
    .id(1)
    .absentReason(AbsentReason.Refused)
    .attended(false)
    .paid(false)
    .eventId(2)
    .eventLocationId(3)
    .period(TimePeriod.AM)
    .prisonId("LEI")
    .bookingId(100)
    .createUserId("user")
    .caseNoteId(1)
    .build()
}
