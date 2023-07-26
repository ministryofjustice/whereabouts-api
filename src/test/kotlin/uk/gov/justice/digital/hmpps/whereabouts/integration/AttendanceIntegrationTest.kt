package uk.gov.justice.digital.hmpps.whereabouts.integration

import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.whereabouts.dto.attendance.CreateAttendanceDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.attendance.UpdateAttendanceDto
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentSubReason
import uk.gov.justice.digital.hmpps.whereabouts.model.Attendance
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod
import uk.gov.justice.digital.hmpps.whereabouts.repository.AttendanceChangesRepository
import uk.gov.justice.digital.hmpps.whereabouts.repository.AttendanceRepository
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

class AttendanceIntegrationTest : IntegrationTest() {

  @Autowired
  lateinit var attendanceRepository: AttendanceRepository

  @Autowired
  lateinit var attendanceChangesRepository: AttendanceChangesRepository

  @AfterEach
  fun cleanUp() {
    attendanceChangesRepository.deleteAll()
    attendanceRepository.deleteAll()
  }

  @Test
  fun `unacceptable absences details is unauthorised when no auth token provided`() {
    val updateAttendanceUrl = "/attendances/offender/A1234AB/unacceptable-absences?offenderNo=A1234AB&fromDate=2021-03-14&toDate=2021-05-24"

    webTestClient.get()
      .uri(updateAttendanceUrl)
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `unacceptable absence count details is unauthorised when no auth token provided`() {
    val updateAttendanceUrl = "/attendances/offender/A1234AB/unacceptable-absence-count?fromDate=2021-03-14&toDate=2021-05-24"

    webTestClient.get()
      .uri(updateAttendanceUrl)
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should make an elite api request to update an offenders attendance`() {
    val bookingId = getNextBookingId()
    val activityId = 2L
    val updateAttendanceUrl = "/api/bookings/$bookingId/activities/$activityId/attendance?lockTimeout=true"

    prisonApiMockServer.stubUpdateAttendance(bookingId)

    val attendance = CreateAttendanceDto(
      prisonId = "LEI",
      bookingId = bookingId,
      eventId = activityId,
      eventLocationId = 2,
      eventDate = LocalDate.of(2010, 10, 10),
      period = TimePeriod.AM,
      attended = true,
      paid = true,
    )

    webTestClient.post()
      .uri("/attendance")
      .bodyValue(attendance)
      .headers(setHeaders())
      .exchange()
      .expectStatus()
      .isCreated

    prisonApiMockServer.verify(
      putRequestedFor(urlEqualTo(updateAttendanceUrl))
        .withRequestBody(
          equalToJson(
            objectMapper.writeValueAsString(
              mapOf(
                "eventOutcome" to "ATT",
                "performance" to "STANDARD",
              ),
            ),
          ),
        ),
    )
  }

  @Test
  fun `should make a case note service request to create a IEP warning case note`() {
    val activityId = 2L
    val bookingId = getNextBookingId()
    val offenderNo = "AB1234G"
    val comments = "Test comment"
    val updateAttendanceUrl = "/api/bookings/$bookingId/activities/$activityId/attendance?lockTimeout=true"

    prisonApiMockServer.stubUpdateAttendance(bookingId)
    caseNotesMockServer.stubCreateCaseNote(offenderNo)
    prisonApiMockServer.stubGetBooking(offenderNo, bookingId)

    val attendance = CreateAttendanceDto(
      prisonId = "LEI",
      bookingId = bookingId,
      eventId = activityId,
      eventLocationId = 2,
      eventDate = LocalDate.of(2010, 10, 10),
      period = TimePeriod.AM,
      attended = false,
      paid = false,
      absentReason = AbsentReason.RefusedIncentiveLevelWarning,
      absentSubReason = AbsentSubReason.ExternalMoves,
      comments = comments,
    )

    webTestClient.post()
      .uri("/attendance")
      .bodyValue(attendance)
      .headers(setHeaders())
      .exchange()
      .expectStatus()
      .isCreated

    prisonApiMockServer.verify(
      putRequestedFor(urlEqualTo(updateAttendanceUrl)).withRequestBody(
        equalToJson(
          objectMapper.writeValueAsString(
            mapOf(
              "eventOutcome" to "UNACAB",
              "outcomeComment" to "External moves. Test comment",
            ),
          ),
        ),
      ),
    )

    caseNotesMockServer.verify(
      postRequestedFor(urlEqualTo("/case-notes/$offenderNo"))
        .withRequestBody(matchingJsonPath("$[?(@.type == 'NEG')]"))
        .withRequestBody(matchingJsonPath("$[?(@.subType == 'IEP_WARN')]"))
        .withRequestBody(matchingJsonPath("$[?(@.text == 'Refused to attend - incentive level warning - External moves. Test comment')]"))
        .withRequestBody(matchingJsonPath("$.occurrenceDateTime")),
    )
  }

  @Test
  fun `should update attendance`() {
    val bookingId = getNextBookingId()

    prisonApiMockServer.stubUpdateAttendance(bookingId)

    val persistedAttendance = createAttendance(bookingId)

    webTestClient
      .mutate().responseTimeout(Duration.ofMinutes(20)).build()
      .put()
      .uri("/attendance/${persistedAttendance.id}")
      .bodyValue(UpdateAttendanceDto(attended = true, paid = true))
      .headers(setHeaders())
      .exchange()
      .expectStatus()
      .isNoContent
  }

  @Test
  fun `update attendance with unexpected error`() {
    val bookingId = getNextBookingId()

    prisonApiMockServer.stubUpdateAttendance(bookingId, 2, 400)

    val persistedAttendance = createAttendance(bookingId)

    webTestClient
      .mutate().responseTimeout(Duration.ofMinutes(20)).build()
      .put()
      .uri("/attendance/${persistedAttendance.id}")
      .bodyValue(UpdateAttendanceDto(attended = true, paid = true))
      .headers(setHeaders())
      .exchange()
      .expectStatus()
      .is5xxServerError
      .expectBody()
      .jsonPath("$.developerMessage").isEqualTo("400 Bad Request from PUT http://localhost:8999/api/bookings/6/activities/2/attendance?lockTimeout=true")
  }

  @Test
  fun `update attendance should return correct lock error`() {
    val bookingId = getNextBookingId()

    prisonApiMockServer.stubUpdateAttendance(bookingId, 2, 423)

    val persistedAttendance = createAttendance(bookingId)

    webTestClient
      .mutate().responseTimeout(Duration.ofMinutes(20)).build()
      .put()
      .uri("/attendance/${persistedAttendance.id}")
      .bodyValue(UpdateAttendanceDto(attended = true, paid = true))
      .headers(setHeaders())
      .exchange()
      .expectStatus()
      .isEqualTo(423)
  }

  @Test
  fun `should make a case note amendment request`() {
    val offenderNo = "BC1234D"
    val activityId = 2L
    val caseNoteId = 3L
    val bookingId = getNextBookingId()

    prisonApiMockServer.stubUpdateAttendance(bookingId)
    caseNotesMockServer.stubCaseNoteAmendment(offenderNo)
    prisonApiMockServer.stubGetBooking(offenderNo, bookingId)

    val savedAttendance = attendanceRepository.save(
      Attendance.builder()
        .bookingId(bookingId)
        .paid(false)
        .attended(false)
        .absentReason(AbsentReason.Refused)
        .absentSubReason(AbsentSubReason.ExternalMoves)
        .comments("Refused")
        .caseNoteId(caseNoteId)
        .eventDate(LocalDate.now())
        .eventId(activityId)
        .eventLocationId(1)
        .prisonId("LEI")
        .period(TimePeriod.AM)
        .createDateTime(LocalDateTime.now())
        .createUserId("user")
        .build(),
    )

    val attendance = CreateAttendanceDto(
      prisonId = "LEI",
      bookingId = bookingId,
      eventId = activityId,
      eventLocationId = 1,
      eventDate = LocalDate.of(2010, 10, 10),
      period = TimePeriod.AM,
      attended = true,
      paid = true,
    )

    webTestClient
      .mutate().responseTimeout(Duration.ofMinutes(20)).build()
      .put()
      .uri("/attendance/${savedAttendance.id}")
      .bodyValue(attendance)
      .headers(setHeaders())
      .exchange()
      .expectStatus()
      .isNoContent

    caseNotesMockServer.verify(
      putRequestedFor(urlEqualTo("/case-notes/$offenderNo/$caseNoteId"))
        .withRequestBody(matchingJsonPath("$[?(@.text == 'Incentive level warning removed: attended')]")),

    )
  }

  @Test
  fun `should request a new auth token for each new incoming request`() {
    val firstBooking = 507L
    val secondBooking = 508L

    prisonApiMockServer.stubUpdateAttendance(firstBooking)
    prisonApiMockServer.stubUpdateAttendance(secondBooking)
    oauthMockServer.resetAll()
    oauthMockServer.stubGrantToken()

    postAttendance(firstBooking)
    postAttendance(secondBooking)

    oauthMockServer.verify(2, postRequestedFor(urlEqualTo("/auth/oauth/token")))
  }

  private fun postAttendance(bookingId: Long) {
    val attendanceDto =
      CreateAttendanceDto(
        prisonId = "LEI",
        attended = true,
        paid = true,
        bookingId = bookingId,
        eventId = 2,
        eventLocationId = 1,
        period = TimePeriod.AM,
        eventDate = (LocalDate.now()),
      )

    webTestClient.post()
      .uri("/attendance")
      .headers(setHeaders())
      .bodyValue(attendanceDto)
      .exchange()
      .expectStatus()
      .isCreated
  }

  private fun createAttendance(bookingId: Long) = attendanceRepository.save(
    Attendance.builder()
      .bookingId(bookingId)
      .paid(false)
      .attended(false)
      .absentReason(AbsentReason.Refused)
      .absentSubReason(AbsentSubReason.ExternalMoves)
      .comments("Refused to turn up")
      .eventDate(LocalDate.now())
      .eventId(2)
      .eventLocationId(2)
      .prisonId("LEI")
      .period(TimePeriod.AM)
      .createDateTime(LocalDateTime.now())
      .createUserId("user")
      .build(),
  )
}
