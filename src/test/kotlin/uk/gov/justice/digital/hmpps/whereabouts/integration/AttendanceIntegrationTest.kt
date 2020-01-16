package uk.gov.justice.digital.hmpps.whereabouts.integration

import com.github.tomakehurst.wiremock.client.WireMock.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import uk.gov.justice.digital.hmpps.whereabouts.dto.AttendanceDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.CreateAttendanceDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.UpdateAttendanceDto
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
  fun `should make an elite api request to update an offenders attendance`() {

    val bookingId = 1
    val activityId = 2L
    val updateAttendanceUrl = "/api/bookings/$bookingId/activities/$activityId/attendance"

    elite2MockServer.stubUpdateAttendance()

    val attendance = CreateAttendanceDto
        .builder()
        .prisonId("LEI")
        .bookingId(1)
        .eventId(activityId)
        .eventLocationId(2)
        .eventDate(LocalDate.of(2010, 10, 10))
        .period(TimePeriod.AM)
        .attended(true)
        .paid(true)
        .build()

    val response: ResponseEntity<String> =
        restTemplate.exchange("/attendance", HttpMethod.POST, createHeaderEntity(attendance))

    assertThat(response.statusCodeValue).isEqualTo(201)


    elite2MockServer.verify(putRequestedFor(urlEqualTo(updateAttendanceUrl))
        .withRequestBody(equalToJson(gson.toJson(mapOf(
            "eventOutcome" to "ATT",
            "performance" to "STANDARD"
        )))))
  }

  @Test
  fun `should make a case note service request to create a IEP warning case note`() {
    val activityId = 2L
    val bookingId = 3L
    val offenderNo = "AB1234G"
    val comments = "Test comment"
    val updateAttendanceUrl = "/api/bookings/$bookingId/activities/$activityId/attendance"

    elite2MockServer.stubUpdateAttendance(bookingId)
    caseNotesMockServer.stubCreateCaseNote(offenderNo)
    elite2MockServer.stubGetBooking(offenderNo, bookingId)

    val attendance = CreateAttendanceDto
        .builder()
        .prisonId("LEI")
        .bookingId(bookingId)
        .eventId(activityId)
        .eventLocationId(2)
        .eventDate(LocalDate.of(2010, 10, 10))
        .period(TimePeriod.AM)
        .attended(false)
        .paid(false)
        .absentReason(AbsentReason.RefusedIncentiveLevelWarning)
        .comments(comments)
        .build()

    val response: ResponseEntity<String> =
        restTemplate.exchange("/attendance", HttpMethod.POST, createHeaderEntity(attendance))

    assertThat(response.statusCodeValue).isEqualTo(201)

    elite2MockServer.verify(putRequestedFor(urlEqualTo(updateAttendanceUrl)).withRequestBody(
        equalToJson(gson.toJson(mapOf(
            "eventOutcome" to "UNACAB",
            "outcomeComment" to "Test comment"
        )))
    ))

    caseNotesMockServer.verify(postRequestedFor(urlEqualTo("/case-notes/$offenderNo"))
        .withRequestBody(matchingJsonPath("$[?(@.type == 'NEG')]"))
        .withRequestBody(matchingJsonPath("$[?(@.subType == 'IEP_WARN')]"))
        .withRequestBody(matchingJsonPath("$[?(@.text == 'Refused - Incentive Level warning - $comments')]"))
        .withRequestBody(matchingJsonPath("$.occurrenceDateTime"))
    )
  }

  @Test
  fun `should return a bad request when the 'bookingId' is missing`() {

    val response: ResponseEntity<String> =
        restTemplate.exchange("/attendance", HttpMethod.POST,
            createHeaderEntity(CreateAttendanceDto
                .builder()
                .prisonId("LEI")
                .eventId(2)
                .eventLocationId(2)
                .eventDate(LocalDate.of(2010, 10, 10))
                .period(TimePeriod.AM)
                .attended(true)
                .paid(true)
                .build()))

    assertThat(response.statusCodeValue).isEqualTo(400)
  }

  @Test
  fun `should return a bad request when the 'prisonId' is missing`() {

    val response: ResponseEntity<String> =
        restTemplate.exchange("/attendance", HttpMethod.POST,
            createHeaderEntity(CreateAttendanceDto
                .builder()
                .bookingId(1)
                .eventId(2)
                .eventLocationId(2)
                .eventDate(LocalDate.of(2010, 10, 10))
                .period(TimePeriod.AM)
                .attended(true)
                .paid(true)
                .build()))

    assertThat(response.statusCodeValue).isEqualTo(400)

  }

  @Test
  fun `should return a bad request when the 'eventId' is missing`() {

    val response: ResponseEntity<String> =
        restTemplate.exchange("/attendance", HttpMethod.POST,
            createHeaderEntity(CreateAttendanceDto
                .builder()
                .bookingId(1)
                .prisonId("LEI")
                .eventLocationId(2)
                .eventDate(LocalDate.of(2010, 10, 10))
                .period(TimePeriod.AM)
                .attended(true)
                .paid(true)
                .build()))

    assertThat(response.statusCodeValue).isEqualTo(400)

  }

  @Test
  fun `should return a bad request when the 'eventLocationId' is missing`() {

    val response: ResponseEntity<String> =
        restTemplate.exchange("/attendance", HttpMethod.POST,
            createHeaderEntity(CreateAttendanceDto
                .builder()
                .bookingId(1)
                .prisonId("LEI")
                .eventId(1)
                .eventDate(LocalDate.of(2010, 10, 10))
                .period(TimePeriod.AM)
                .attended(true)
                .paid(true)
                .build()))

    assertThat(response.statusCodeValue).isEqualTo(400)

  }

  @Test
  fun `should return a bad request when the 'eventDate' is missing`() {

    val response: ResponseEntity<String> =
        restTemplate.exchange("/attendance", HttpMethod.POST,
            createHeaderEntity(CreateAttendanceDto
                .builder()
                .bookingId(1)
                .prisonId("LEI")
                .eventId(1)
                .eventLocationId(1)
                .period(TimePeriod.AM)
                .attended(true)
                .paid(true)
                .build()))

    assertThat(response.statusCodeValue).isEqualTo(400)

  }

  @Test
  fun `should return a bad request when the 'period' is missing`() {

    val response: ResponseEntity<String> =
        restTemplate.exchange("/attendance", HttpMethod.POST,
            createHeaderEntity(CreateAttendanceDto
                .builder()
                .bookingId(1)
                .prisonId("LEI")
                .eventId(1)
                .eventLocationId(1)
                .eventDate(LocalDate.of(2019, 10, 10))
                .attended(true)
                .paid(true)
                .build()))

    assertThat(response.statusCodeValue).isEqualTo(400)

  }

  @Test
  fun `should update attendance`() {
    elite2MockServer.stubUpdateAttendance()

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
            .build())

    val response =
        restTemplate.exchange(
            "/attendance/${persistedAttendance.id}",
            HttpMethod.PUT,
            createHeaderEntity(UpdateAttendanceDto.builder().attended(true).paid(true).build()),
            String::class.java,
            LocalDate.of(2019, 10, 10),
            TimePeriod.AM)

    assertThat(response.statusCodeValue).isEqualTo(204)
  }

  @Test
  fun `should return a 404 when attempting to update non existent attendance`() {
    val response =
        restTemplate.exchange(
            "/attendance/100",
            HttpMethod.PUT,
            createHeaderEntity(UpdateAttendanceDto.builder().attended(true).paid(true).build()),
            String::class.java,
            LocalDate.of(2019, 10, 10),
            TimePeriod.AM)

    assertThat(response.statusCodeValue).isEqualTo(404)
  }

  @Test
  fun `should return a 400 bad request when 'attended' is null`() {
    val response =
        restTemplate.exchange(
            "/attendance/100",
            HttpMethod.PUT,
            createHeaderEntity(UpdateAttendanceDto.builder().paid(true).build()),
            String::class.java,
            LocalDate.of(2019, 10, 10),
            TimePeriod.AM)

    assertThat(response.statusCodeValue).isEqualTo(400)
  }

  @Test
  fun `should return a 400 bad request when 'paid' is null`() {
    val response =
        restTemplate.exchange(
            "/attendance/100",
            HttpMethod.PUT,
            createHeaderEntity(UpdateAttendanceDto.builder().attended(true).build()),
            String::class.java,
            LocalDate.of(2019, 10, 10),
            TimePeriod.AM)

    assertThat(response.statusCodeValue).isEqualTo(400)
  }

  @Test
  fun `should return a 409 bad request when attendance already exists`() {
    elite2MockServer.stubUpdateAttendance()

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
                    .build())

    val attendanceDto =
            CreateAttendanceDto
                    .builder()
                    .prisonId("LEI")
                    .attended(true)
                    .paid(true)
                    .bookingId(1)
                    .eventId(2)
                    .eventLocationId(1)
                    .period(TimePeriod.AM)
                    .eventDate(LocalDate.now())
                    .build()

    val errorResponse: ResponseEntity<String> =
            restTemplate.exchange("/attendance", HttpMethod.POST, createHeaderEntity(attendanceDto))

    assertThat(errorResponse.statusCodeValue).isEqualTo(409)
    assertThat(errorResponse.body).contains("Attendance already exists")
  }

  @Test
  fun `should return the attendance dto on creation`() {
    val activityId = 2L

    elite2MockServer.stubUpdateAttendance(5)

    val attendance = CreateAttendanceDto
        .builder()
        .prisonId("LEI")
        .bookingId(5)
        .eventId(activityId)
        .eventLocationId(2)
        .eventDate(LocalDate.of(2010, 10, 10))
        .period(TimePeriod.AM)
        .attended(true)
        .paid(true)
        .build()

    val response: ResponseEntity<AttendanceDto> =
        restTemplate.exchange("/attendance", HttpMethod.POST, createHeaderEntity(attendance))

    val savedAttendance = response.body!!

    assertThat(response.statusCodeValue).isEqualTo(201)
    assertThat(savedAttendance.id).isGreaterThan(0)
    assertThat(savedAttendance.createUserId).isEqualTo("ITAG_USER")
    assertThat(savedAttendance.locked).isEqualTo(false)
  }

  @Test
  fun `should make a case note amendment request`() {
    val offenderNo = "BC1234D"
    val activityId = 2L
    val caseNoteId = 3L

    elite2MockServer.stubUpdateAttendance()
    caseNotesMockServer.stubCaseNoteAmendment(offenderNo)
    elite2MockServer.stubGetBooking(offenderNo)

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
            .build())

    val attendance = CreateAttendanceDto
        .builder()
        .prisonId("LEI")
        .bookingId(1)
        .eventId(activityId)
        .eventLocationId(1)
        .eventDate(LocalDate.of(2010, 10, 10))
        .period(TimePeriod.AM)
        .attended(true)
        .paid(true)
        .build()

    val response: ResponseEntity<AttendanceDto> =
        restTemplate.exchange("/attendance/${savedAttendance.id}", HttpMethod.PUT, createHeaderEntity(attendance))

    assertThat(response.statusCodeValue).isEqualTo(204)

    caseNotesMockServer.verify(putRequestedFor(urlEqualTo("/case-notes/$offenderNo/$caseNoteId"))
        .withRequestBody(matchingJsonPath("$[?(@.text == 'Incentive Level warning rescinded: attended')]"))

    )
  }

  @Test
  fun `should request a new auth token for each new incoming request`() {
    elite2MockServer.stubUpdateAttendance()
    elite2MockServer.stubUpdateAttendance(2)
    oauthMockServer.resetAll()
    oauthMockServer.stubGrantToken()

    postAttendance()
    postAttendance(2)

    oauthMockServer.verify(2, postRequestedFor(urlEqualTo("/auth/oauth/token")))
  }

  private fun postAttendance(bookingId: Long = 1) {
    val attendanceDto =
        CreateAttendanceDto
            .builder()
            .prisonId("LEI")
            .attended(true)
            .paid(true)
            .bookingId(bookingId)
            .eventId(2)
            .eventLocationId(1)
            .period(TimePeriod.AM)
            .eventDate(LocalDate.now())
            .build()

    val response: ResponseEntity<String> =
        restTemplate.exchange("/attendance", HttpMethod.POST, createHeaderEntity(attendanceDto))

    assertThat(response.statusCodeValue).isEqualTo(201)
  }
}
