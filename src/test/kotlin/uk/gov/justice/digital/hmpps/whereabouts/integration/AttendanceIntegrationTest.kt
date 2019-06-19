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
import uk.gov.justice.digital.hmpps.whereabouts.dto.UpdateAttendanceDto
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason
import uk.gov.justice.digital.hmpps.whereabouts.model.Attendance
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod
import uk.gov.justice.digital.hmpps.whereabouts.repository.AttendanceRepository
import java.time.LocalDate
import java.time.LocalDateTime


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

        val bookingId = 1
        val activityId = 2L
        val updateAttendanceUrl = "/api/bookings/$bookingId/activities/$activityId/attendance"

        elite2MockServer.stubFor(
                WireMock.put(urlPathEqualTo(updateAttendanceUrl))
                        .willReturn(WireMock.aResponse()
                                .withStatus(200))
        )

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


        verify(putRequestedFor(urlEqualTo(updateAttendanceUrl))
                .withRequestBody(equalToJson(gson.toJson(mapOf(
                        "eventOutcome" to "ATT",
                        "performance" to "STANDARD"
                )))))
    }

    @Test
    fun `should make an elite api request to create a IEP warning case note`() {
        val activityId = 2L
        val bookingId = 1
        val comments = "Test comment"
        val updateAttendanceUrl = "/api/bookings/$bookingId/activities/$activityId/attendance"
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
                        "eventOutcome" to "UNACAB",
                        "outcomeComment" to "Test comment"
                )))
        ))

        verify(postRequestedFor(urlEqualTo(createCaseNote))
                .withRequestBody(matchingJsonPath("$[?(@.type == 'NEG')]"))
                .withRequestBody(matchingJsonPath("$[?(@.subType == 'IEP_WARN')]"))
                .withRequestBody(matchingJsonPath("$[?(@.text == 'Refused - $comments')]"))
                .withRequestBody(matchingJsonPath("$.occurrence"))
        )
    }

    @Test
    fun `receive a list of attendance for a prison, location, date and period`() {
        val activityId = 2L
        val bookingId = 1
        val updateAttendanceUrl = "/api/bookings/$bookingId/activities/$activityId/attendance"
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
                .period(TimePeriod.PM)
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
                        TimePeriod.PM)

        val result = response.body!!

        assertThat(response.statusCodeValue).isEqualTo(200)

        assertThat(result)
                .usingElementComparatorIgnoringFields("createDateTime", "modifyDateTime")
                .containsExactlyInAnyOrder(
                        AttendanceDto
                                .builder()
                                .id(attendanceId)
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
                                .createUserId("user")
                                .modifyUserId("user")
                                .locked(false)
                                .build())

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
                                .eventDate(LocalDate.of(2019,10,10))
                                .attended(true)
                                .paid(true)
                                .build()))

        assertThat(response.statusCodeValue).isEqualTo(400)

    }

    @Test
    fun `should update attendance`() {
        elite2MockServer.stubFor(
                WireMock.put(urlPathEqualTo("/api/bookings/1/activities/1/attendance"))
                        .willReturn(WireMock.aResponse()
                                .withStatus(200)))

        val persistedAttendance = attendanceRepository.save(
                Attendance.builder()
                        .absentReason(AbsentReason.Refused)
                        .offenderBookingId(1)
                        .comments("Refused to turn up")
                        .attended(false)
                        .paid(false)
                        .createDateTime(LocalDateTime.now())
                        .eventDate(LocalDate.now())
                        .eventId(1)
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
    fun `should return the attendance dto on creation` () {
        val bookingId = 1
        val activityId = 2L

        elite2MockServer.stubFor(
                WireMock.put(urlPathEqualTo("/api/bookings/$bookingId/activities/$activityId/attendance"))
                        .willReturn(WireMock.aResponse()
                                .withStatus(200))
        )

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

        val response: ResponseEntity<AttendanceDto> =
                restTemplate.exchange("/attendance", HttpMethod.POST, createHeaderEntity(attendance))

        val savedAttendance = response.body!!

        assertThat(savedAttendance.id).isGreaterThan(0)
        assertThat(savedAttendance.createUserId).isEqualTo("ITAG_USER")
        assertThat(savedAttendance.locked).isEqualTo(true)
        assertThat(response.statusCodeValue).isEqualTo(201)
    }

    @Test
    fun `should return a 400 bad request when 'locked' is true for paid`() {
        val yesterdayDateTime = LocalDateTime.now().minusDays(1)

        elite2MockServer.stubFor(
                WireMock.put(urlPathEqualTo("/api/bookings/1/activities/1/attendance"))
                        .willReturn(WireMock.aResponse()
                                .withStatus(200)))

        val persistedAttendance = attendanceRepository.save(
                Attendance.builder()
                        .absentReason(AbsentReason.Refused)
                        .offenderBookingId(1)
                        .comments("Refused to turn up")
                        .attended(false)
                        .paid(true)
                        .createDateTime(yesterdayDateTime)
                        .eventDate(yesterdayDateTime.toLocalDate())
                        .eventId(1)
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

        assertThat(response.statusCodeValue).isEqualTo(400)
    }

    @Test
    fun `should return a 400 bad request when 'locked' is true for unpaid`() {
        val sevenDaysAgoDateTime = LocalDateTime.now().minusDays(7)

        elite2MockServer.stubFor(
                WireMock.put(urlPathEqualTo("/api/bookings/1/activities/1/attendance"))
                        .willReturn(WireMock.aResponse()
                                .withStatus(200)))

        val persistedAttendance = attendanceRepository.save(
                Attendance.builder()
                        .absentReason(AbsentReason.Refused)
                        .offenderBookingId(1)
                        .comments("Refused to turn up")
                        .attended(false)
                        .paid(false)
                        .createDateTime(sevenDaysAgoDateTime)
                        .eventDate(sevenDaysAgoDateTime.toLocalDate())
                        .eventId(1)
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

        assertThat(response.statusCodeValue).isEqualTo(400)
    }

    @Test
    fun `should make a case note amendment request`() {
        val bookingId = 1
        val activityId = 1L
        val caseNoteId = 3L

        elite2MockServer.stubFor(
                WireMock.put(urlPathEqualTo("/api/bookings/$bookingId/activities/$activityId/attendance"))
                        .willReturn(WireMock.aResponse()
                                .withStatus(200))
        )

        elite2MockServer.stubFor(
                WireMock.put(urlPathEqualTo("/api/bookings/$bookingId/caseNotes/$caseNoteId"))
                        .willReturn(WireMock.aResponse()
                                .withStatus(200))
        )


        val savedAttendance = attendanceRepository.save(
                Attendance.builder()
                        .offenderBookingId(1)
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

        verify(putRequestedFor(urlEqualTo("/api/bookings/$bookingId/caseNotes/$caseNoteId"))
                .withRequestBody(matchingJsonPath("$[?(@.text == 'IEP rescinded: attended')]"))

        )
    }

    @Test
    fun `should return modified by and modified on`() {

        val bookingId = 1
        val activityId = 1L

        elite2MockServer.stubFor(
                WireMock.put(urlPathEqualTo("/api/bookings/$bookingId/activities/$activityId/attendance"))
                        .willReturn(WireMock.aResponse()
                                .withStatus(200))
        )


        attendanceRepository.save(
                Attendance.builder()
                        .offenderBookingId(1)
                        .paid(false)
                        .attended(false)
                        .absentReason(AbsentReason.Refused)
                        .comments("Refused")
                        .eventDate(LocalDate.of(2019, 10, 10))
                        .eventId(1)
                        .eventLocationId(2)
                        .prisonId("LEI")
                        .period(TimePeriod.AM)
                        .createDateTime(LocalDateTime.now())
                        .createUserId("user")
                        .build())

        val response =
                restTemplate.exchange(
                        "/attendance/LEI/2?date={0}&period={1}",
                        HttpMethod.GET,
                        createHeaderEntity(""),
                        AttendanceDtoReferenceType(),
                        LocalDate.of(2019, 10, 10),
                        TimePeriod.AM)


        val savedAttendance = response?.body!!.first()

        assertThat(savedAttendance.id).isGreaterThan(0)
        assertThat(savedAttendance.modifyUserId).isEqualTo("user")
        assertThat(savedAttendance.modifyDateTime).isNotNull()
        assertThat(response.statusCodeValue).isEqualTo(200)


    }
}
