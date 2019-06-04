package uk.gov.justice.digital.hmpps.whereabouts.integration

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.junit.WireMockRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.ClassRule
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.exchange
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import uk.gov.justice.digital.hmpps.whereabouts.dto.AttendanceDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.CaseNoteDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.CreateAttendanceDto
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason
import uk.gov.justice.digital.hmpps.whereabouts.model.Attendance
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod
import uk.gov.justice.digital.hmpps.whereabouts.repository.AttendanceRepository
import java.time.LocalDate


class AttendanceDtoReferenceType : ParameterizedTypeReference<List<AttendanceDto>>()

class AttendanceIntegrationTest : IntegrationTest () {

    companion object {
        @get:ClassRule
        @JvmStatic
        val elite2MockServer = WireMockRule(8999)
    }

    @Autowired
    lateinit var attendanceRepository: AttendanceRepository

    @Test
    fun `should make an elite api request to update an offenders attendance`() {

        val offenderNo = "A2345"
        val activityId = 2L
        val updateAttendanceUrl = "/api/bookings/offenderNo/$offenderNo/activities/$activityId/attendance"

        elite2MockServer.stubFor(
                WireMock.put(urlPathEqualTo(updateAttendanceUrl))
                        .willReturn(WireMock.aResponse()
                                .withStatus(200))
        )

        val attendance = CreateAttendanceDto
                .builder()
                .offenderNo(offenderNo)
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


        verify(putRequestedFor(urlEqualTo(updateAttendanceUrl))
                .withRequestBody(equalToJson(gson.toJson(mapOf(
                        "eventOutcome" to "ATT",
                        "performance" to "STANDARD"
                )))))
    }

    @Test
    fun `should make an elite api request to create a IEP warning case note`() {
        val offenderNo = "A2345"
        val activityId = 2L
        val bookingId = 1
        val comments = "Test comment"
        val updateAttendanceUrl = "/api/bookings/offenderNo/$offenderNo/activities/$activityId/attendance"
        val createCaseNote = "/api/bookings/$bookingId/caseNotes"

        elite2MockServer.stubFor(
                WireMock.put(urlPathEqualTo(updateAttendanceUrl))
                        .willReturn(WireMock.aResponse()
                                .withStatus(200)))

        elite2MockServer.stubFor(
                WireMock.post(urlPathEqualTo(createCaseNote))
                           .willReturn(WireMock.aResponse()
                                   .withHeader("Content-Type", "application/json")
                                   .withBody(gson.toJson(CaseNoteDto.builder().caseNoteId(100).build()))
                                   .withStatus(201))
        )

        val attendance = CreateAttendanceDto
                .builder()
                .offenderNo(offenderNo)
                .prisonId("LEI")
                .bookingId(1)
                .eventId(activityId)
                .eventLocationId(2)
                .eventDate(LocalDate.of(2010, 10, 10))
                .period(TimePeriod.AM)
                .attended(false)
                .paid(false)
                .absentReason(AbsentReason.Refused)
                .comments(comments)
                .build()

        val response: ResponseEntity<String> =
                restTemplate.exchange("/attendance",   HttpMethod.POST, createHeaderEntity(attendance))

        assertThat(response.statusCodeValue).isEqualTo(201)

        verify(putRequestedFor(urlEqualTo(updateAttendanceUrl)).withRequestBody(
                equalToJson(gson.toJson(mapOf(
                        "eventOutcome" to "UNACAB"
                )))
        ))

        verify(postRequestedFor(urlEqualTo(createCaseNote))
                .withRequestBody(matchingJsonPath("$[?(@.type == 'NEG')]"))
                .withRequestBody(matchingJsonPath("$[?(@.subType == 'IEP_WARN')]"))
                .withRequestBody(matchingJsonPath("$[?(@.text == '$comments')]"))
                .withRequestBody(matchingJsonPath("$.occurrence"))
        )
    }

    @Test
    fun `receive a list of attendance for a prison, location, date and period`() {
        val offenderNo = "A2345"
        val activityId = 2L
        val bookingId = 1
        val updateAttendanceUrl = "/api/bookings/offenderNo/$offenderNo/activities/$activityId/attendance"
        val createCaseNote = "/api/bookings/$bookingId/caseNotes"

        elite2MockServer.stubFor(
                WireMock.put((updateAttendanceUrl))
                        .willReturn(WireMock.aResponse()
                                .withStatus(200)))

        elite2MockServer.stubFor(
                WireMock.post(urlPathEqualTo(createCaseNote))
                        .willReturn(WireMock.aResponse()
                                .withStatus(200)))

        val attendanceId = attendanceRepository.save(Attendance
                .builder()
                .absentReason(AbsentReason.Refused)
                .period(TimePeriod.AM)
                .prisonId("LEI")
                .eventLocationId(2)
                .eventId(1)
                .eventDate(LocalDate.of(2019, 10, 10))
                .comments("hello world")
                .attended(false)
                .paid(false)
                .offenderBookingId(1)
                .build()
        ).id

        val response =
                restTemplate.exchange(
                        "/attendance/LEI/2?date={0}&period={1}",
                        HttpMethod.GET,
                        createHeaderEntity(""),
                        AttendanceDtoReferenceType(),
                        LocalDate.of(2019, 10, 10),
                        TimePeriod.AM)

        val result = response.body!!

        assertThat(response.statusCodeValue).isEqualTo(200)

        val attendance = result.firstOrNull()!!

        assertThat(attendance.id).isEqualTo(attendanceId)
        assertThat(attendance.absentReason).isEqualTo(AbsentReason.Refused)
        assertThat(attendance.period).isEqualTo(TimePeriod.AM)
        assertThat(attendance.prisonId).isEqualTo("LEI")
        assertThat(attendance.eventLocationId).isEqualTo(2)
        assertThat(attendance.eventId).isEqualTo(1)
        assertThat(attendance.eventDate).isEqualTo(LocalDate.of(2019, 10, 10))
        assertThat(attendance.comments).isEqualTo("hello world")
        assertThat(attendance.isAttended).isFalse()
        assertThat(attendance.isPaid).isFalse()
        assertThat(attendance.bookingId).isEqualTo(1)
        assertThat(attendance.createUserId).isEqualTo("user")
        assertThat(attendance.creationDateTime.toLocalDate()).isEqualTo(LocalDate.now())

    }

    @Test
    fun `should return a bad request when the 'bookingId' is missing`() {

        val response: ResponseEntity<String> =
            restTemplate.exchange("/attendance", HttpMethod.POST,
                  createHeaderEntity(CreateAttendanceDto
                    .builder()
                    .offenderNo("A12345")
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
    fun `should return a bad request when the 'offenderNo' is missing`() {

        val response: ResponseEntity<String> =
                restTemplate.exchange("/attendance", HttpMethod.POST,
                        createHeaderEntity(CreateAttendanceDto
                                .builder()
                                .bookingId(1)
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
                                .offenderNo("A12345")
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
                                .offenderNo("A12345")
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
                                .offenderNo("A12345")
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
                                .offenderNo("A12345")
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
                                .offenderNo("A12345")
                                .prisonId("LEI")
                                .eventId(1)
                                .eventLocationId(1)
                                .eventDate(LocalDate.of(2019,10,10))
                                .attended(true)
                                .paid(true)
                                .build()))

        assertThat(response.statusCodeValue).isEqualTo(400)

    }
}
