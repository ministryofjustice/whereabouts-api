package uk.gov.justice.digital.hmpps.whereabouts.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.whereabouts.dto.attendance.AttendanceChangesResponse
import uk.gov.justice.digital.hmpps.whereabouts.dto.attendance.AttendanceDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.attendance.CreateAttendanceDto
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

class AttendancesChangesIntegrationTest : IntegrationTest() {
  var nextEventId = 0L

  @Test
  fun `should return all changes`() {
    val startTime = LocalDateTime.now()

    saveNewAttendance("LEI")

    val updatedAttendanceLEI = saveNewAttendance("LEI")

    updateAttendance(updatedAttendanceLEI)

    val updatedAttendanceRSI = saveNewAttendance("RSI")

    updateAttendance(updatedAttendanceRSI)

    val result = getChanges(startTime, LocalDateTime.now())

    assertThat(result.changes).extracting<Long> { change -> change.attendanceId }.containsOnly(updatedAttendanceLEI.id, updatedAttendanceRSI.id)
  }

  @Test
  fun `should return only changes for specific agency id`() {
    val startTime = LocalDateTime.now()

    saveNewAttendance("LEI")

    val updatedAttendanceLEI1 = saveNewAttendance("LEI")

    updateAttendance(updatedAttendanceLEI1)

    updateAttendance(saveNewAttendance("RSI"))

    val updatedAttendanceLEI2 = saveNewAttendance("LEI")

    updateAttendance(updatedAttendanceLEI2)

    val result = getChanges(startTime, LocalDateTime.now(), "LEI")

    assertThat(result.changes).extracting<Long> { change -> change.attendanceId }.containsOnly(updatedAttendanceLEI1.id, updatedAttendanceLEI2.id)
  }

  @Test
  fun `should return only changes for specific agency id and within time range`() {
    updateAttendance(saveNewAttendance("LEI"))

    val startTime = LocalDateTime.now()

    val updatedAttendanceLE2 = saveNewAttendance("LEI")

    updateAttendance(updatedAttendanceLE2)

    val result = getChanges(startTime, LocalDateTime.now(), "LEI")

    assertThat(result.changes).extracting<Long> { change -> change.attendanceId }.containsOnly(updatedAttendanceLE2.id)
  }

  @Test
  fun `should return only changes for a specific time`() {
    updateAttendance(saveNewAttendance("LEI"))

    val updatedAttendanceLE2 = saveNewAttendance("LEI")

    updateAttendance(updatedAttendanceLE2)

    val allChangesResult = getChanges(LocalDateTime.now().minusMinutes(2), LocalDateTime.now())

    updateAttendance(saveNewAttendance("RSI"))

    val startTime = allChangesResult.changes!!.first { change -> change.attendanceId == updatedAttendanceLE2.id }.changedOn

    val result = getChanges(startTime!!)

    assertThat(result.changes).extracting<Long> { change -> change.attendanceId }.containsOnly(updatedAttendanceLE2.id)
  }

  @Test
  fun `should return only changes for specific agency id and for a specific time`() {
    updateAttendance(saveNewAttendance("LEI"))

    val updatedAttendanceLE2 = saveNewAttendance("LEI")

    updateAttendance(updatedAttendanceLE2)

    updateAttendance(saveNewAttendance("RSI"))

    val allChangesResult = getChanges(LocalDateTime.now().minusMinutes(2), LocalDateTime.now())

    val startTime = allChangesResult.changes!!.first { change -> change.attendanceId == updatedAttendanceLE2.id }.changedOn

    val result = getChanges(startTime!!, null, "LEI")

    assertThat(result.changes).extracting<Long> { change -> change.attendanceId }.containsOnly(updatedAttendanceLE2.id)
  }

  fun getNexEventId(): Long {
    nextEventId += 1
    return nextEventId
  }

  fun saveNewAttendance(prisonId: String): AttendanceDto {
    val bookingId = getNextBookingId()

    val eventId = getNexEventId()

    prisonApiMockServer.stubUpdateAttendance(bookingId, eventId)

    val attendance = CreateAttendanceDto(
      prisonId = prisonId,
      bookingId = bookingId,
      eventId = eventId,
      eventLocationId = 2,
      eventDate = LocalDate.of(2010, 10, 10),
      period = TimePeriod.AM,
      attended = true,
      paid = true,
    )

    return webTestClient.post()
      .uri("/attendance")
      .bodyValue(attendance)
      .headers(setHeaders())
      .exchange()
      .expectStatus()
      .isCreated
      .expectBody(AttendanceDto::class.java)
      .returnResult()
      .responseBody
  }

  fun updateAttendance(attendance: AttendanceDto) {
    Thread.sleep(Duration.ofMillis(40))

    webTestClient.put()
      .uri("/attendance/${attendance.id}")
      .bodyValue(attendance)
      .headers(setHeaders())
      .exchange()
      .expectStatus()
      .isNoContent
  }

  fun getChanges(fromDateTime: LocalDateTime, toDateTime: LocalDateTime? = null, agencyId: String? = null): AttendanceChangesResponse {
    var uri = "/attendances/changes?fromDateTime=$fromDateTime"
    if (toDateTime != null) {
      uri += "&toDateTime=$toDateTime"
    }
    if (agencyId != null) {
      uri += "&agencyId=$agencyId"
    }
    return webTestClient.get()
      .uri(uri)
      .headers(setHeaders())
      .exchange()
      .expectStatus()
      .isOk
      .expectBody(AttendanceChangesResponse::class.java)
      .returnResult()
      .responseBody
  }
}
