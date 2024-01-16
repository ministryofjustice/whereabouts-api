package uk.gov.justice.digital.hmpps.whereabouts.services.vlboptionsfinder

import org.springframework.beans.factory.annotation.Value
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalTime

@Component
class OptionsGenerator(
  @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
  @Value("\${video-link-booking.booking-options-finder.day-start}")
  val dayStart: LocalTime,

  @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
  @Value("\${video-link-booking.booking-options-finder.day-end}")
  val dayEnd: LocalTime,

  @Value("\${video-link-booking.booking-options-finder.step}")
  val step: Duration,
) {
  fun getOptionsInPreferredOrder(preferredOption: VideoLinkBookingOption): Sequence<VideoLinkBookingOption> =
    durations(step)
      .map { dayStart.plus(it) }
      .map { preferredOption.copyStartingAt(it) }
      .takeWhile { it.endsOnOrBefore(dayEnd) }
      .sortedBy { Duration.between(preferredOption.main.interval.start, it.main.interval.start).abs() }

  companion object {
    fun durations(delta: Duration) = generateSequence(Duration.ZERO) { it.plus(delta) }
  }
}
