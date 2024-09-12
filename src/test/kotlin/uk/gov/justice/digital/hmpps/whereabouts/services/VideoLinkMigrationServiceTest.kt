package uk.gov.justice.digital.hmpps.whereabouts.services

import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkAppointment
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkAppointmentRepository
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingEventRepository
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingRepository
import uk.gov.justice.digital.hmpps.whereabouts.services.court.VideoLinkMigrationService
import uk.gov.justice.digital.hmpps.whereabouts.utils.DataHelpers.Companion.makeVideoLinkBooking
import java.time.LocalDateTime

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

  // TODO: Mapping tests - basic
}
