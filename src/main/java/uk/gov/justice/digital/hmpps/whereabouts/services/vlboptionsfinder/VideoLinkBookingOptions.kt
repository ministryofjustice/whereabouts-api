package uk.gov.justice.digital.hmpps.whereabouts.services.vlboptionsfinder

import uk.gov.justice.digital.hmpps.whereabouts.services.locationfinder.LocationAndInterval
import java.time.Duration
import java.time.LocalTime

data class VideoLinkBookingOption(
  val pre: LocationAndInterval? = null,
  val main: LocationAndInterval,
  val post: LocationAndInterval? = null
) {
  fun toLocationsAndIntervals() = listOfNotNull(pre, main, post)

  fun copyStartingAt(startTime: LocalTime): VideoLinkBookingOption {
    val offset = Duration.between(earliestStartTime(), startTime)
    return VideoLinkBookingOption(
      pre = this.pre?.shift(offset),
      main = this.main.shift(offset),
      post = this.post?.shift(offset)
    )
  }

  fun endsOnOrBefore(endTime: LocalTime): Boolean = latestEndTime().isBefore(endTime) || latestEndTime() == endTime

  fun earliestStartTime(): LocalTime = pre?.interval?.start ?: main.interval.start

  fun latestEndTime(): LocalTime = post?.interval?.end ?: main.interval.end

  companion object {
    fun from(specification: VideoLinkBookingSearchSpecification) =
      VideoLinkBookingOption(
        pre = specification.preAppointment?.let {
          LocationAndInterval(
            locationId = it.locationId,
            interval = it.interval
          )
        },
        main = specification.mainAppointment.let {
          LocationAndInterval(
            locationId = it.locationId,
            interval = it.interval
          )
        },
        post = specification.postAppointment?.let {
          LocationAndInterval(
            locationId = it.locationId,
            interval = it.interval
          )
        }
      )
  }
}

data class VideoLinkBookingOptions(
  val matched: Boolean,
  val alternatives: List<VideoLinkBookingOption>
)
