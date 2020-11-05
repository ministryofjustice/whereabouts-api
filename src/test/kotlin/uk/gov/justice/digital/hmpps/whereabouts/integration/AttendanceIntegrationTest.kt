package uk.gov.justice.digital.hmpps.whereabouts.integration

import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
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

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class AttendanceIntegrationTest : IntegrationTest() {

  @Autowired
  lateinit var attendanceRepository: AttendanceRepository

  @Test
  @Order(0)
  fun `should return all changes made to an attendance record`() {
    val activityId = 2L

    prisonApiMockServer.stubUpdateAttendance(5)

    val attendance = CreateAttendanceDto(
      prisonId = "LEI",
      bookingId = 5,
      eventId = activityId,
      eventLocationId = 2,
      eventDate = LocalDate.of(2010, 10, 10),
      period = TimePeriod.AM,
      attended = false,
      paid = false,
      absentReason = AbsentReason.Refused
    )

    val response = webTestClient.post()
      .uri("/attendance")
      .headers(setHeaders())
      .bodyValue(attendance)
      .exchange()
      .returnResult(AttendanceDto::class.java)

    val createdAttendance = response.responseBody.blockFirst()
    val updateAttendance =
      UpdateAttendanceDto(attended = false, paid = true, absentReason = AbsentReason.AcceptableAbsence)

    webTestClient.put()
      .uri("/attendance/${createdAttendance.id}")
      .headers(setHeaders())
      .bodyValue(updateAttendance)
      .exchange()
      .expectStatus()
      .isNoContent()

    val from = LocalDateTime.now().minusHours(1)
    val to = LocalDateTime.now().plusHours(1)

    webTestClient.get()
      .uri {
        it.path("/attendances/changes")
          .queryParam("fromDateTime", from)
          .queryParam("toDateTime", to)
          .build()
      }
      .headers(setHeaders())
      .exchange()
      .expectStatus().isOk()
      .expectBody()
      .jsonPath("$.changes[0].attendanceId").isEqualTo(createdAttendance.id)
      .jsonPath("$.changes[0].eventId").isEqualTo(activityId)
      .jsonPath("$.changes[0].eventLocationId").isEqualTo(2L)
      .jsonPath("$.changes[0].bookingId").isEqualTo(5L)
      .jsonPath("$.changes[0].changedFrom").isEqualTo(AbsentReason.Refused.toString())
      .jsonPath("$.changes[0].changedTo").isEqualTo(AbsentReason.AcceptableAbsence.toString())
      .jsonPath("$.changes[0].changedBy").isEqualTo("ITAG_USER")
  }

  @Test
  fun `should make an elite api request to update an offenders attendance`() {

    val bookingId = 1
    val activityId = 2L
    val updateAttendanceUrl = "/api/bookings/$bookingId/activities/$activityId/attendance"

    prisonApiMockServer.stubUpdateAttendance()

    val attendance = CreateAttendanceDto(
      prisonId = "LEI",
      bookingId = 1,
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
    val bookingId = 3L
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
  fun `should return a bad request when the 'bookingId' is missing`() {
    webTestClient.post()
      .uri("/attendance")
      .bodyValue(
        mapOf(
          "prisonId" to "LEI",
          "eventId" to 2,
          "eventLocationId" to 2,
          "eventDate" to LocalDate.of(2010, 10, 10),
          "period" to TimePeriod.AM,
          "attended" to true,
          "paid" to true
        )
      )
      .headers(setHeaders())
      .exchange()
      .expectStatus()
      .isBadRequest()
  }

  @Test
  fun `should return a bad request when the 'prisonId' is missing`() {
    webTestClient.post()
      .uri("/attendance")
      .bodyValue(
        mapOf(
          "bookingId" to 1,
          "eventId" to 2,
          "eventLocationId" to 2,
          "eventDate" to LocalDate.of(2010, 10, 10),
          "period" to TimePeriod.AM,
          "attended" to true,
          "paid" to true
        )
      )
      .headers(setHeaders())
      .exchange()
      .expectStatus()
      .isBadRequest()
  }

  @Test
  fun `should return a bad request when the 'eventId' is missing`() {
    webTestClient.post()
      .uri("/attendance")
      .bodyValue(
        mapOf(
          "bookingId" to 1,
          "prisonId" to "LEI",
          "eventLocationId" to 2,
          "eventDate" to LocalDate.of(2010, 10, 10),
          "period" to TimePeriod.AM,
          "attended" to true,
          "paid" to true
        )
      )
      .headers(setHeaders())
      .exchange()
      .expectStatus()
      .isBadRequest()
  }

  @Test
  fun `should return a bad request when the 'eventLocationId' is missing`() {
    webTestClient.post()
      .uri("/attendance")
      .bodyValue(
        mapOf(
          "bookingId" to 1,
          "prisonId" to "LEI",
          "eventId" to 1,
          "eventDate" to LocalDate.of(2010, 10, 10),
          "period" to TimePeriod.AM,
          "attended" to true,
          "paid" to true
        )
      )
      .headers(setHeaders())
      .exchange()
      .expectStatus()
      .isBadRequest()
  }

  @Test
  fun `should return a bad request when the 'eventDate' is missing`() {
    webTestClient.post()
      .uri("/attendance")
      .bodyValue(
        mapOf(
          "bookingId" to 1,
          "prisonId" to "LEI",
          "eventId" to 1,
          "eventLocationId" to 1,
          "period" to TimePeriod.AM,
          "attended" to true,
          "paid" to true
        )
      )
      .headers(setHeaders())
      .exchange()
      .expectStatus()
      .isBadRequest()
  }

  @Test
  fun `should return a bad request when the 'period' is missing`() {
    webTestClient.post()
      .uri("/attendance")
      .bodyValue(
        mapOf(
          "bookingId" to 1,
          "prisonId" to "LEI",
          "eventId" to 1,
          "eventLocationId" to 1,
          "eventDate" to LocalDate.of(2019, 10, 10),
          "attended" to true,
          "paid" to true
        )
      )
      .headers(setHeaders())
      .exchange()
      .expectStatus()
      .isBadRequest()
  }

  @Test
  fun `should update attendance`() {
    prisonApiMockServer.stubUpdateAttendance()

    val persistedAttendance = attendanceRepository.save(
      Attendance.builder()
        .absentReason(AbsentReason.Refused)
        .bookingId(1)
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
  fun `should return a 404 when attempting to update non existent attendance`() {
    webTestClient.put()
      .uri("/attendance/100")
      .bodyValue(UpdateAttendanceDto(attended = true, paid = true))
      .headers(setHeaders())
      .exchange()
      .expectStatus()
      .isNotFound()
  }

  @Test
  fun `should return a 409 bad request when attendance already exists`() {
    prisonApiMockServer.stubUpdateAttendance()

    attendanceRepository.save(
      Attendance.builder()
        .absentReason(AbsentReason.Refused)
        .bookingId(1)
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

    val attendanceDto =
      CreateAttendanceDto(
        prisonId = "LEI",
        attended = true,
        paid = true,
        bookingId = 1,
        eventId = 2,
        eventLocationId = 1,
        period = TimePeriod.AM,
        eventDate = LocalDate.now()
      )

    val response = webTestClient.post()
      .uri("/attendance")
      .bodyValue(attendanceDto)
      .headers(setHeaders())
      .exchange()
      .expectStatus()
      .isEqualTo(409)
      .returnResult(String::class.java)

    val body = response.responseBody.blockFirst()

    assertThat(body).isEqualTo("Attendance already exists")
  }

  @Test
  fun `should return the attendance dto on creation`() {
    val activityId = 2L

    prisonApiMockServer.stubUpdateAttendance(5)

    val attendance = CreateAttendanceDto(
      prisonId = "LEI",
      bookingId = 5,
      eventId = activityId,
      eventLocationId = 2,
      eventDate = LocalDate.of(2010, 11, 11),
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
      .expectBody()
      .jsonPath("$.id").isNotEmpty()
      .jsonPath("$.createUserId").isEqualTo("ITAG_USER")
      .jsonPath("$.locked").isEqualTo(false)
  }

  @Test
  fun `should make a case note amendment request`() {
    val offenderNo = "BC1234D"
    val activityId = 2L
    val caseNoteId = 3L

    prisonApiMockServer.stubUpdateAttendance()
    caseNotesMockServer.stubCaseNoteAmendment(offenderNo)
    prisonApiMockServer.stubGetBooking(offenderNo)

    val savedAttendance = attendanceRepository.save(
      Attendance.builder()
        .bookingId(1)
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
      bookingId = 1,
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
    prisonApiMockServer.stubUpdateAttendance()
    prisonApiMockServer.stubUpdateAttendance(2)
    oauthMockServer.resetAll()
    oauthMockServer.stubGrantToken()

    postAttendance()
    postAttendance(2)

    oauthMockServer.verify(2, postRequestedFor(urlEqualTo("/auth/oauth/token")))
  }

  private fun postAttendance(bookingId: Long = 1) {
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
