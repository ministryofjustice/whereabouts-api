package uk.gov.justice.digital.hmpps.whereabouts.services

import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBookingEvent
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBookingEventType
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkAppointmentRepository
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingEventRepository
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingRepository
import uk.gov.justice.digital.hmpps.whereabouts.services.court.VideoLinkMigrationService
import uk.gov.justice.digital.hmpps.whereabouts.utils.DataHelpers.Companion.makeVideoLinkBooking
import java.lang.IllegalArgumentException
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Optional

private val videoLinkAppointmentRepository: VideoLinkAppointmentRepository = mock()
private val videoLinkBookingRepository: VideoLinkBookingRepository = mock()
private val videoLinkBookingEventRepository: VideoLinkBookingEventRepository = mock()

class VideoLinkMigrationServiceTest {

  private val videoLinkMigrationService = VideoLinkMigrationService(
    videoLinkBookingRepository,
    videoLinkBookingEventRepository,
    videoLinkAppointmentRepository,
  )

  @Test
  fun `Event date and time conversion`() {
    val locationId = 123L
    val startTime = LocalDateTime.of(2023, 10, 1, 12, 20, 10)
    val endTime = LocalDateTime.of(2023, 10, 1, 12, 40, 15)

    val slot = videoLinkMigrationService.mapEventToLocationTimeSlot(locationId, startTime, endTime)

    assertThat(slot.date).isEqualTo("2023-10-01")
    assertThat(slot.startTime).hasHour(12).hasMinute(20).hasSecond(0).hasNano(0)
    assertThat(slot.endTime).hasHour(12).hasMinute(40).hasSecond(0).hasNano(0)
    assertThat(slot.startTime).isEqualTo("12:20")
    assertThat(slot.endTime).isEqualTo("12:40")
  }

  @Test
  fun `Appointment date and time conversion`() {
    val appointment = VideoLinkAppointment(
      id = 1,
      videoLinkBooking = makeVideoLinkBooking(),
      startDateTime = LocalDateTime.of(2023, 10, 1, 12, 20, 10),
      endDateTime = LocalDateTime.of(2023, 10, 1, 12, 40, 15),
      appointmentId = 1L,
      locationId = 1L,
      hearingType = HearingType.PRE,
    )

    val slot = videoLinkMigrationService.mapAppointment(appointment)

    assertThat(slot.date).isEqualTo("2023-10-01")
    assertThat(slot.startTime).hasHour(12).hasMinute(20).hasSecond(0).hasNano(0)
    assertThat(slot.endTime).hasHour(12).hasMinute(40).hasSecond(0).hasNano(0)
  }

  @Test
  fun `Migrate a court booking`() {
    val videoBookingId = 3L
    val booking = makeVideoLinkBooking(
      id = videoBookingId,
      madeByTheCourt = true,
      courtName = "York magistrates",
      courtId = "YORKMAGS",
      prisonId = "MDI",
      comment = "hello",
    )

    val appointments = booking.appointments.values.toList()
    val events = listOf(makeEvent(3L, 1L))

    whenever(videoLinkBookingRepository.findById(videoBookingId)).thenReturn(Optional.of(booking))
    whenever(videoLinkAppointmentRepository.findAllByVideoLinkBooking(booking)).thenReturn(appointments)
    whenever(videoLinkBookingEventRepository.findEventsByVideoLinkBookingId(videoBookingId)).thenReturn(events)

    val response = videoLinkMigrationService.getVideoLinkBookingToMigrate(3)

    assertThat(response.madeByTheCourt).isTrue()
    assertThat(response.probation).isFalse()

    assertThat(response.events).hasSize(1)
    assertThat(response.events).extracting("eventType").containsExactly(VideoLinkBookingEventType.CREATE)

    assertThat(response.pre?.date).isEqualTo("2022-01-01")
    assertThat(response.pre?.startTime).isEqualTo("10:00")
    assertThat(response.pre?.endTime).isEqualTo("11:00")

    assertThat(response.main.date).isEqualTo("2022-01-01")
    assertThat(response.main.startTime).isEqualTo("10:00")
    assertThat(response.main.endTime).isEqualTo("11:00")

    assertThat(response.post?.date).isEqualTo("2022-01-01")
    assertThat(response.post?.startTime).isEqualTo("10:00")
    assertThat(response.post?.endTime).isEqualTo("11:00")
  }

  @Test
  fun `Recognise a probation booking`() {
    val videoBookingId = 3L
    val booking = makeVideoLinkBooking(
      id = videoBookingId,
      madeByTheCourt = true,
      courtName = "Probation - York (PPOC)",
      courtId = "PROBYRK",
      prisonId = "MDI",
      comment = "hello",
    )

    val appointments = booking.appointments.values.toList()
    val events = listOf(makeEvent(3L, 1L))

    whenever(videoLinkBookingRepository.findById(videoBookingId)).thenReturn(Optional.of(booking))
    whenever(videoLinkAppointmentRepository.findAllByVideoLinkBooking(booking)).thenReturn(appointments)
    whenever(videoLinkBookingEventRepository.findEventsByVideoLinkBookingId(videoBookingId)).thenReturn(events)

    val response = videoLinkMigrationService.getVideoLinkBookingToMigrate(3)

    assertThat(response.madeByTheCourt).isTrue()
    assertThat(response.probation).isTrue()
    assertThat(response.courtCode).isEqualTo("PROBYRK")
    assertThat(response.prisonCode).isEqualTo("MDI")
  }

  @Test
  fun `Recognise a prison booking`() {
    val videoBookingId = 3L
    val booking = makeVideoLinkBooking(
      id = videoBookingId,
      madeByTheCourt = false,
      courtName = "York Justice Centre",
      courtId = null,
      prisonId = "MDI",
      comment = "hello",
    )

    val appointments = booking.appointments.values.toList()
    val events = listOf(makeEvent(3L, 1L))

    whenever(videoLinkBookingRepository.findById(videoBookingId)).thenReturn(Optional.of(booking))
    whenever(videoLinkAppointmentRepository.findAllByVideoLinkBooking(booking)).thenReturn(appointments)
    whenever(videoLinkBookingEventRepository.findEventsByVideoLinkBookingId(videoBookingId)).thenReturn(events)

    val response = videoLinkMigrationService.getVideoLinkBookingToMigrate(3)

    assertThat(response.madeByTheCourt).isFalse()
    assertThat(response.probation).isFalse()
    assertThat(response.courtCode).isEqualTo("UNKNOWN")
    assertThat(response.courtName).isEqualTo("York Justice Centre")
  }

  @Test
  fun `Fail if no appointments`() {
    val videoBookingId = 3L
    val booking = makeVideoLinkBooking(
      id = videoBookingId,
      madeByTheCourt = false,
      courtName = "York Justice Centre",
      courtId = null,
      prisonId = "MDI",
      comment = "hello",
    )

    val appointments: List<VideoLinkAppointment> = emptyList()
    val events = listOf(makeEvent(3L, 1L))

    whenever(videoLinkBookingRepository.findById(videoBookingId)).thenReturn(Optional.of(booking))
    whenever(videoLinkAppointmentRepository.findAllByVideoLinkBooking(booking)).thenReturn(appointments)
    whenever(videoLinkBookingEventRepository.findEventsByVideoLinkBookingId(videoBookingId)).thenReturn(events)

    assertThrows(IllegalArgumentException::class.java) {
      videoLinkMigrationService.getVideoLinkBookingToMigrate(3)
    }
  }

  @Test
  fun `Handle a cancelled booking`() {
    val videoBookingId = 3L
    val booking = makeVideoLinkBooking(
      id = videoBookingId,
      madeByTheCourt = true,
      courtName = "York Justice Centre",
      courtId = "YORKMAGS",
      prisonId = "MDI",
      comment = "hello",
    )

    // No appointments
    val appointments: List<VideoLinkAppointment> = emptyList()

    // Events include create, update and delete
    val events = listOf(
      makeEvent(3L, 1L, VideoLinkBookingEventType.CREATE),
      makeEvent(3L, 1L, VideoLinkBookingEventType.UPDATE),
      makeEvent(3L, 1L, VideoLinkBookingEventType.DELETE),
    )

    whenever(videoLinkBookingRepository.findById(videoBookingId)).thenReturn(Optional.of(booking))
    whenever(videoLinkAppointmentRepository.findAllByVideoLinkBooking(booking)).thenReturn(appointments)
    whenever(videoLinkBookingEventRepository.findEventsByVideoLinkBookingId(videoBookingId)).thenReturn(events)

    val response = videoLinkMigrationService.getVideoLinkBookingToMigrate(3)

    assertThat(response.cancelled).isTrue()

    assertThat(response.events).hasSize(3)
    assertThat(response.events).extracting("eventType").containsExactly(
      VideoLinkBookingEventType.CREATE,
      VideoLinkBookingEventType.UPDATE,
      VideoLinkBookingEventType.DELETE,
    )

    assertThat(response.pre).isNull()

    // Only the main appointment has been populated from the latest CREATE or UPDATE event
    assertThat(response.main.locationId).isEqualTo(123L)
    assertThat(response.main.date).isEqualTo(LocalDate.now().plusDays(1))
    assertThat(response.main.startTime).isNotNull()
    assertThat(response.main.endTime).isNotNull()

    assertThat(response.post).isNull()
  }

  @Test
  fun `Fail if there is no main hearing`() {
    val videoBookingId = 3L
    val booking = makeVideoLinkBooking(
      id = videoBookingId,
      madeByTheCourt = false,
      courtName = "York Justice Centre",
      courtId = null,
      prisonId = "MDI",
      comment = "hello",
    )

    val appointments = booking.appointments.values.toList().filter { it.hearingType != HearingType.MAIN }
    val events = listOf(makeEvent(3L, 1L))

    whenever(videoLinkBookingRepository.findById(videoBookingId)).thenReturn(Optional.of(booking))
    whenever(videoLinkAppointmentRepository.findAllByVideoLinkBooking(booking)).thenReturn(appointments)
    whenever(videoLinkBookingEventRepository.findEventsByVideoLinkBookingId(videoBookingId)).thenReturn(events)

    assertThrows(IllegalArgumentException::class.java) {
      videoLinkMigrationService.getVideoLinkBookingToMigrate(3)
    }
  }

  @Test
  fun `Fail if more than 3 appointments`() {
    val videoBookingId = 3L
    val booking = makeVideoLinkBooking(
      id = videoBookingId,
      madeByTheCourt = false,
      courtName = "York Justice Centre",
      courtId = null,
      prisonId = "MDI",
      comment = "hello",
    )

    // Add an extra MAIN appointment
    val mainHearing = booking.appointments.values.toList().find { it.hearingType == HearingType.MAIN }
    val appointments = booking.appointments.values.toMutableList()
    appointments.addLast(mainHearing!!)

    val events = listOf(makeEvent(3L, 1L))

    whenever(videoLinkBookingRepository.findById(videoBookingId)).thenReturn(Optional.of(booking))
    whenever(videoLinkAppointmentRepository.findAllByVideoLinkBooking(booking)).thenReturn(appointments)
    whenever(videoLinkBookingEventRepository.findEventsByVideoLinkBookingId(videoBookingId)).thenReturn(events)

    assertThrows(IllegalArgumentException::class.java) {
      videoLinkMigrationService.getVideoLinkBookingToMigrate(3)
    }
  }

  @Test
  fun `No events`() {
    val videoBookingId = 3L
    val booking = makeVideoLinkBooking(
      id = videoBookingId,
      madeByTheCourt = false,
      courtName = "York Justice Centre",
      courtId = null,
      prisonId = "MDI",
      comment = "hello",
    )

    val appointments = booking.appointments.values.toList()
    val events: List<VideoLinkBookingEvent> = emptyList()

    whenever(videoLinkBookingRepository.findById(videoBookingId)).thenReturn(Optional.of(booking))
    whenever(videoLinkAppointmentRepository.findAllByVideoLinkBooking(booking)).thenReturn(appointments)
    whenever(videoLinkBookingEventRepository.findEventsByVideoLinkBookingId(videoBookingId)).thenReturn(events)

    assertThrows(IllegalArgumentException::class.java) {
      videoLinkMigrationService.getVideoLinkBookingToMigrate(3)
    }
  }

  private fun makeEvent(
    videoLinkBookingId: Long,
    eventId: Long,
    eventType: VideoLinkBookingEventType = VideoLinkBookingEventType.CREATE,
    courtCode: String = "YORK",
    courtName: String = "YORKMAGS",
  ): VideoLinkBookingEvent {
    return VideoLinkBookingEvent(
      eventId = eventId,
      eventType = eventType,
      timestamp = LocalDateTime.now().minusDays(1),
      userId = "TIM",
      videoLinkBookingId = videoLinkBookingId,
      agencyId = "MDI",
      offenderBookingId = 123L,
      court = courtName,
      courtId = courtCode,
      madeByTheCourt = true,
      comment = "comments",
      mainNomisAppointmentId = 123L,
      mainLocationId = 123L,
      mainStartTime = LocalDateTime.now().plusDays(1),
      mainEndTime = LocalDateTime.now().plusDays(1).plusHours(1),
    )
  }
}
