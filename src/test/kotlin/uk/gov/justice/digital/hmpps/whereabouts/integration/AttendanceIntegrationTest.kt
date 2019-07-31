package uk.gov.justice.digital.hmpps.whereabouts.integration

import com.github.tomakehurst.wiremock.client.WireMock.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.ClassRule
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import uk.gov.justice.digital.hmpps.whereabouts.dto.*
import uk.gov.justice.digital.hmpps.whereabouts.integration.wiremock.Elite2MockServer
import uk.gov.justice.digital.hmpps.whereabouts.integration.wiremock.OAuthMockServer
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason
import uk.gov.justice.digital.hmpps.whereabouts.model.Attendance
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod
import uk.gov.justice.digital.hmpps.whereabouts.repository.AttendanceRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.stream.Collectors

class AttendanceIntegrationTest : IntegrationTest () {

    companion object {
        @get:ClassRule
        @JvmStatic
        val elite2MockServer = Elite2MockServer()

        @get:ClassRule
        @JvmStatic
        val oauthMockServer = OAuthMockServer()
    }

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
    fun `should make an elite api request to create a IEP warning case note`() {
        val activityId = 2L
        val bookingId = 1
        val comments = "Test comment"
        val updateAttendanceUrl = "/api/bookings/$bookingId/activities/$activityId/attendance"
        val createCaseNote = "/api/bookings/$bookingId/caseNotes"

        elite2MockServer.stubUpdateAttendance()
        elite2MockServer.stubCreateCaseNote()

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

        elite2MockServer.verify(putRequestedFor(urlEqualTo(updateAttendanceUrl)).withRequestBody(
                equalToJson(gson.toJson(mapOf(
                        "eventOutcome" to "UNACAB",
                        "outcomeComment" to "Test comment"
                )))
        ))

       elite2MockServer.verify(postRequestedFor(urlEqualTo(createCaseNote))
                .withRequestBody(matchingJsonPath("$[?(@.type == 'NEG')]"))
                .withRequestBody(matchingJsonPath("$[?(@.subType == 'IEP_WARN')]"))
                .withRequestBody(matchingJsonPath("$[?(@.text == 'Refused - $comments')]"))
                .withRequestBody(matchingJsonPath("$.occurrence"))
        )
    }

    @Test
    fun `receive a list of attendance for a prison, location, date and period`() {
        elite2MockServer.stubUpdateAttendance()
        elite2MockServer.stubCreateCaseNote()

        attendanceRepository.deleteAll()

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
                .bookingId(1)
                .build()
        ).id

        val response =
                restTemplate.exchange(
                        "/attendance/LEI/2?date={0}&period={1}",
                        HttpMethod.GET,
                        createHeaderEntity(""),
                        ListOfAttendanceDtoReferenceType(),
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
    fun `should return the attendance dto on creation` () {
        val activityId = 2L

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

        elite2MockServer.stubUpdateAttendance()

        val persistedAttendance = attendanceRepository.save(
                Attendance.builder()
                        .absentReason(AbsentReason.Refused)
                        .bookingId(1)
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

        elite2MockServer.stubUpdateAttendance()

        val persistedAttendance = attendanceRepository.save(
                Attendance.builder()
                        .absentReason(AbsentReason.Refused)
                        .bookingId(1)
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
        val activityId = 2L
        val caseNoteId = 3L

        elite2MockServer.stubUpdateAttendance()
        elite2MockServer.stubCaseNoteAmendment()

        oauthMockServer.stubGrantToken()

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

        elite2MockServer.verify(putRequestedFor(urlEqualTo("/api/bookings/$bookingId/caseNotes/$caseNoteId"))
                .withRequestBody(matchingJsonPath("$[?(@.text == 'IEP rescinded: attended')]"))

        )
    }

    @Test
    fun `should return modified by and modified on`() {
        elite2MockServer.stubUpdateAttendance()

        attendanceRepository.save(
                Attendance.builder()
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
                        .createDateTime(LocalDateTime.now())
                        .createUserId("user")
                        .build())

        val response =
                restTemplate.exchange(
                        "/attendance/LEI/2?date={0}&period={1}",
                        HttpMethod.GET,
                        createHeaderEntity(""),
                        ListOfAttendanceDtoReferenceType(),
                        LocalDate.of(2019, 10, 10),
                        TimePeriod.AM)


        val savedAttendance = response?.body!!.first()

        assertThat(savedAttendance.id).isGreaterThan(0)
        assertThat(savedAttendance.modifyUserId).isEqualTo("user")
        assertThat(savedAttendance.modifyDateTime).isNotNull()
        assertThat(response.statusCodeValue).isEqualTo(200)
    }

    @Test
    fun `should request a new auth token for each new incoming request`() {
        elite2MockServer.stubUpdateAttendance()
        elite2MockServer.stubUpdateAttendance()
        oauthMockServer.resetAll()
        oauthMockServer.stubGrantToken()

        postAttendance()
        postAttendance()

        oauthMockServer.verify(2, postRequestedFor(urlEqualTo("/auth/oauth/token")))
    }

    private fun postAttendance() {
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

        val response: ResponseEntity<String> =
                restTemplate.exchange("/attendance", HttpMethod.POST, createHeaderEntity(attendanceDto))

        assertThat(response.statusCodeValue).isEqualTo(201)
    }

    @Test
    fun `should return attendance information for set of bookings ids`() {
        elite2MockServer.stubUpdateAttendance()
        attendanceRepository.deleteAll()

        attendanceRepository.save(Attendance
                .builder()
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
                .build())

       attendanceRepository.save(Attendance
                .builder()
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
                .build())

        val response =
                restTemplate.exchange(
                        "/attendance/LEI?date={0}&period={1}&bookings=${1}&bookings=${2}",
                        HttpMethod.GET,
                        createHeaderEntity(""),
                        ListOfAttendanceDtoReferenceType(),
                        LocalDate.of(2019, 10, 10),
                        TimePeriod.PM)

        assertThat(response.statusCodeValue).isEqualTo(200)
        assertThat(response.body).extracting("bookingId").contains(1L,2L)
    }

    @Test
    fun `should attend all supplied in bookings`() {
        val bookingIds = setOf(1L, 2L)

        oauthMockServer.stubGrantToken()
        elite2MockServer.stubUpdateAttendanceForBookingIds()

        attendanceRepository.deleteAll()

        val bookingActivities = bookingIds
                .stream()
                .map { BookingActivity.builder().activityId(2L).bookingId(it).build() }
                .collect(Collectors.toSet())

        val attendAll = AttendAllDto
                .builder()
                .eventDate(LocalDate.of(2019, 10, 10))
                .eventLocationId(1L)
                .prisonId("LEI")
                .period(TimePeriod.AM)
                .bookingActivities(bookingActivities)
                .build()

        val response =
                restTemplate.exchange(
                        "/attendance/attend-all",
                        HttpMethod.POST,
                        createHeaderEntity(attendAll),
                        String::class.java,
                        LocalDate.of(2019, 10, 10),
                        TimePeriod.PM)

        assertThat(response.statusCodeValue).isEqualTo(201)

        elite2MockServer.verify(putRequestedFor(urlEqualTo("/api/bookings/activities/attendance"))
                .withRequestBody(equalToJson(gson.toJson(mapOf(
                        "eventOutcome" to "ATT",
                        "performance" to "STANDARD",
                        "outcomeComment" to "",
                        "bookingActivities" to bookingActivities
                )))))
    }
}
