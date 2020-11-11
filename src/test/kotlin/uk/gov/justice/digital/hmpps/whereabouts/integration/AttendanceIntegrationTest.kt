package uk.gov.justice.digital.hmpps.whereabouts.integration

import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.whereabouts.dto.attendance.AttendanceDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.attendance.CreateAttendanceDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.attendance.UpdateAttendanceDto
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason
import uk.gov.justice.digital.hmpps.whereabouts.model.Attendance
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod
import uk.gov.justice.digital.hmpps.whereabouts.repository.AttendanceRepository
import java.time.LocalDate
import java.time.LocalDateTime

class AttendanceIntegrationTest : IntegrationTest() {

  @Autowired
  lateinit var attendanceRepository: AttendanceRepository

  @Test
  fun `should return all changes made to an attendance record`() {
    val bookingId = getNextBookingId()

    prisonApiMockServer.stubUpdateAttendance(bookingId)

    val createdAttendance = webTestClient.post()
      .uri("/attendance")
      .headers(setHeaders())
      .bodyValue(
        CreateAttendanceDto(
          prisonId = "LEI",
          bookingId = bookingId,
          eventId = 2L,
          eventLocationId = 2,
          eventDate = LocalDate.of(2010, 10, 10),
          period = TimePeriod.AM,
          attended = false,
          paid = false,
          absentReason = AbsentReason.Refused
        )
      )
      .exchange()
      .returnResult(AttendanceDto::class.java)
      .responseBody
      .blockFirst()

    val updateAttendance =
      UpdateAttendanceDto(attended = false, paid = true, absentReason = AbsentReason.AcceptableAbsence)

    webTestClient.put()
      .uri("/attendance/${createdAttendance.id}")
      .headers(setHeaders())
      .bodyValue(updateAttendance)
      .exchange()
      .expectStatus()
      .isNoContent

    webTestClient.get()
      .uri {
        it.path("/attendances/changes")
          .queryParam("fromDateTime", LocalDateTime.now().minusHours(1).toString())
          .queryParam("toDateTime", LocalDateTime.now().plusHours(1).toString())
          .build()
      }
      .headers(setHeaders())
      .exchange()
      .expectStatus().isOk()
      .expectBody()
      .jsonPath("$.changes[0].attendanceId").isNumber
      .jsonPath("$.changes[0].eventId").isEqualTo(2L)
      .jsonPath("$.changes[0].eventLocationId").isEqualTo(2L)
      .jsonPath("$.changes[0].bookingId").isEqualTo(bookingId)
      .jsonPath("$.changes[0].changedFrom").isEqualTo(AbsentReason.Refused.toString())
      .jsonPath("$.changes[0].changedTo").isEqualTo(AbsentReason.AcceptableAbsence.toString())
      .jsonPath("$.changes[0].changedBy").isEqualTo("ITAG_USER")
  }

  @Test
  fun `should make an elite api request to update an offenders attendance`() {

    val bookingId = getNextBookingId()
    val activityId = 2L
    val updateAttendanceUrl = "/api/bookings/$bookingId/activities/$activityId/attendance"

    prisonApiMockServer.stubUpdateAttendance(bookingId)

    val attendance = CreateAttendanceDto(
      prisonId = "LEI",
      bookingId = bookingId,
      eventId = activityId,
      eventLocationId = 2,
      eventDate = LocalDate.of(2010, 10, 10),
      period = TimePeriod.AM,
      attended = true,
      paid = true
    )

    webTestClient.post()
      .uri("/attendance")
      .bodyValue(attendance)
      .headers(setHeaders())
      .exchange()
      .expectStatus()
      .isCreated()

    prisonApiMockServer.verify(
      putRequestedFor(urlEqualTo(updateAttendanceUrl))
        .withRequestBody(
          equalToJson(
            gson.toJson(
              mapOf(
                "eventOutcome" to "ATT",
                "performance" to "STANDARD"
              )
            )
          )
        )
    )
  }

  @Test
  fun `should make a case note service request to create a IEP warning case note`() {
    val activityId = 2L
    val bookingId = getNextBookingId()
    val offenderNo = "AB1234G"
    val comments = "Test comment"
    val updateAttendanceUrl = "/api/bookings/$bookingId/activities/$activityId/attendance"

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
      comments = comments
    )

    webTestClient.post()
      .uri("/attendance")
      .bodyValue(attendance)
      .headers(setHeaders())
      .exchange()
      .expectStatus()
      .isCreated()

    prisonApiMockServer.verify(
      putRequestedFor(urlEqualTo(updateAttendanceUrl)).withRequestBody(
        equalToJson(
          gson.toJson(
            mapOf(
              "eventOutcome" to "UNACAB",
              "outcomeComment" to "Test comment"
            )
          )
        )
      )
    )

    caseNotesMockServer.verify(
      postRequestedFor(urlEqualTo("/case-notes/$offenderNo"))
        .withRequestBody(matchingJsonPath("$[?(@.type == 'NEG')]"))
        .withRequestBody(matchingJsonPath("$[?(@.subType == 'IEP_WARN')]"))
        .withRequestBody(matchingJsonPath("$[?(@.text == 'Refused - Incentive Level warning - $comments')]"))
        .withRequestBody(matchingJsonPath("$.occurrenceDateTime"))
    )
  }

  @Test
  fun `should update attendance`() {
    val bookingId = getNextBookingId()

    prisonApiMockServer.stubUpdateAttendance(bookingId)

    val persistedAttendance = attendanceRepository.save(
      Attendance.builder()
        .absentReason(AbsentReason.Refused)
        .bookingId(bookingId)
        .comments("Refused to turn up")
        .attended(false)
        .paid(false)
        .createDateTime(LocalDateTime.now())
        .eventDate(LocalDate.now())
        .eventId(2)
        .prisonId("LEI")
        .period(TimePeriod.AM)
        .eventLocationId(2)
        .build()
    )

    webTestClient.put()
      .uri("/attendance/${persistedAttendance.id}")
      .bodyValue(UpdateAttendanceDto(attended = true, paid = true))
      .headers(setHeaders())
      .exchange()
      .expectStatus()
      .isNoContent()
  }

  @Test
  fun `should make a case note amendment request`() {
    val offenderNo = "BC1234D"
    val activityId = 2L
    val caseNoteId = 3L
    val bookingId = getNextBookingId()

    prisonApiMockServer.stubUpdateAttendance(bookingId)
    caseNotesMockServer.stubCaseNoteAmendment(offenderNo)
    prisonApiMockServer.stubGetBooking(offenderNo)

    val savedAttendance = attendanceRepository.save(
      Attendance.builder()
        .bookingId(bookingId)
        .paid(false)
        .attended(false)
        .absentReason(AbsentReason.Refused)
        .comments("Refused")
        .caseNoteId(caseNoteId)
        .eventDate(LocalDate.now())
        .eventId(activityId)
        .eventLocationId(1)
        .prisonId("LEI")
        .period(TimePeriod.AM)
        .createDateTime(LocalDateTime.now())
        .createUserId("user")
        .build()
    )

    val attendance = CreateAttendanceDto(
      prisonId = "LEI",
      bookingId = bookingId,
      eventId = activityId,
      eventLocationId = 1,
      eventDate = LocalDate.of(2010, 10, 10),
      period = TimePeriod.AM,
      attended = true,
      paid = true
    )

    webTestClient.put()
      .uri("/attendance/${savedAttendance.id}")
      .bodyValue(attendance)
      .headers(setHeaders())
      .exchange()
      .expectStatus()
      .isNoContent()

    caseNotesMockServer.verify(
      putRequestedFor(urlEqualTo("/case-notes/$offenderNo/$caseNoteId"))
        .withRequestBody(matchingJsonPath("$[?(@.text == 'Incentive Level warning rescinded: attended')]"))

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
        eventDate = (LocalDate.now())
      )

    webTestClient.post()
      .uri("/attendance")
      .headers(setHeaders())
      .bodyValue(attendanceDto)
      .exchange()
      .expectStatus()
      .isCreated()
  }
}
