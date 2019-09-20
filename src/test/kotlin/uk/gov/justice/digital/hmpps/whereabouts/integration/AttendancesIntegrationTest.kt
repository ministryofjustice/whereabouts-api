package uk.gov.justice.digital.hmpps.whereabouts.integration

import com.github.tomakehurst.wiremock.client.WireMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import uk.gov.justice.digital.hmpps.whereabouts.dto.AttendanceDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.AttendancesDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.AttendancesResponse
import uk.gov.justice.digital.hmpps.whereabouts.dto.BookingActivity
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason
import uk.gov.justice.digital.hmpps.whereabouts.model.Attendance
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod
import uk.gov.justice.digital.hmpps.whereabouts.repository.AttendanceRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.stream.Collectors

class AttendancesIntegrationTest : IntegrationTest() {

  @Autowired
  lateinit var attendanceRepository: AttendanceRepository

  @Test
  fun `receive a list of attendance for a prison, location, date and period`() {
    elite2MockServer.stubUpdateAttendance()
    elite2MockServer.stubGetBooking()
    caseNotesMockServer.stubCreateCaseNote()

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
            "/attendances/LEI/2?date={0}&period={1}",
            HttpMethod.GET,
            createHeaderEntity(""),
            AttendancesResponse::class.java,
            LocalDate.of(2019, 10, 10),
            TimePeriod.PM)

    val result = response.body?.attendances

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
            "/attendances/LEI/2?date={0}&period={1}",
            HttpMethod.GET,
            createHeaderEntity(""),
            AttendancesResponse::class.java,
            LocalDate.of(2019, 10, 10),
            TimePeriod.AM)


    val savedAttendance = response?.body?.attendances?.first()

    assertThat(savedAttendance?.id).isGreaterThan(0)
    assertThat(savedAttendance?.modifyUserId).isEqualTo("user")
    assertThat(savedAttendance?.modifyDateTime).isNotNull()
    assertThat(response.statusCodeValue).isEqualTo(200)
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
            "/attendances/LEI?date={0}&period={1}&bookings=${1}&bookings=${2}",
            HttpMethod.GET,
            createHeaderEntity(""),
            AttendancesResponse::class.java,
            LocalDate.of(2019, 10, 10),
            TimePeriod.PM)

    assertThat(response.statusCodeValue).isEqualTo(200)
    assertThat(response.body?.attendances).extracting("bookingId").contains(1L, 2L)
  }

  @Test
  fun `should return attendance information for set of bookings ids by post`() {
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

    val bookings = setOf(1, 2)

    val response =
        restTemplate.exchange(
            "/attendances/LEI?date={0}&period={1}",
            HttpMethod.POST,
            createHeaderEntity(bookings),
            AttendancesResponse::class.java,
            LocalDate.of(2019, 10, 10),
            TimePeriod.PM)

    assertThat(response.statusCodeValue).isEqualTo(200)
    assertThat(response.body?.attendances).extracting("bookingId").contains(1L, 2L)
  }

  @Test
  fun `should create multiple attendances`() {
    val bookingIds = setOf(1L, 2L)

    elite2MockServer.stubUpdateAttendanceForBookingIds()
    attendanceRepository.deleteAll()

    val bookingActivities = bookingIds
        .stream()
        .map { BookingActivity(activityId = 2L,bookingId = it) }
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

    val response =
        restTemplate.exchange(
            "/attendances",
            HttpMethod.POST,
            createHeaderEntity(attendAll),
            AttendancesResponse::class.java
        )

    assertThat(response.statusCodeValue).isEqualTo(201)

    elite2MockServer.verify(WireMock.putRequestedFor(WireMock.urlEqualTo("/api/bookings/activities/attendance"))
        .withRequestBody(WireMock.equalToJson(gson.toJson(mapOf(
            "performance" to "STANDARD",
            "bookingActivities" to bookingActivities,
            "eventOutcome" to "ATT"
        )))))
  }

  @Test
  fun `should return attendance information for offenders that have scheduled activity`() {
    elite2MockServer.stubGetScheduledActivities()

    val prisonId = "MDI"
    val date = LocalDate.now()
    val period = TimePeriod.AM

    attendanceRepository.save(
        Attendance
            .builder()
            .absentReason(AbsentReason.Refused)
            .attended(false)
            .paid(false)
            .eventId(2)
            .eventLocationId(3)
            .period(TimePeriod.AM)
            .prisonId("MDI")
            .bookingId(1L)
            .eventDate(date)
            .createUserId("user")
            .caseNoteId(1)
            .build())


    val response = restTemplate.exchange(
        "/attendances/$prisonId/attendance-for-scheduled-activities?date=$date&period=$period",
        HttpMethod.GET,
        createHeaderEntity(""),
        AttendancesResponse::class.java)

    assertThat(response.statusCodeValue).isEqualTo(200)
    assertThat(response.body?.attendances).hasSize(1)

    elite2MockServer.verify(WireMock.getRequestedFor(WireMock.urlEqualTo("/api/schedules/$prisonId/activities?date=$date&timeSlot=$period")))
  }
}
