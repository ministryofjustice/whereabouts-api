package uk.gov.justice.digital.hmpps.whereabouts.services.vlboptionsfinder

import io.swagger.annotations.ApiModelProperty
import java.time.Duration
import java.time.LocalTime

data class VideoLinkBookingOption(
  @ApiModelProperty(value = "The location and Interval for the pre-hearing appointment.")
  val pre: LocationAndInterval? = null,

  @ApiModelProperty(
    value = "The location (by location id) and Interval for the main appointment (the court appearance).",
    required = true,
  )
  val main: LocationAndInterval,

  @ApiModelProperty(value = "The location and Interval for the post-hearing appointment.")
  val post: LocationAndInterval? = null,
) {
  fun toLocationsAndIntervals() = listOfNotNull(pre, main, post)

  fun copyStartingAt(startTime: LocalTime): VideoLinkBookingOption {
    val offset = Duration.between(earliestStartTime(), startTime)
    return VideoLinkBookingOption(
      pre = this.pre?.shift(offset),
      main = this.main.shift(offset),
      post = this.post?.shift(offset),
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
            interval = it.interval,
          )
        },
        main = specification.mainAppointment.let {
          LocationAndInterval(
            locationId = it.locationId,
            interval = it.interval,
          )
        },
        post = specification.postAppointment?.let {
          LocationAndInterval(
            locationId = it.locationId,
            interval = it.interval,
          )
        },
      )
  }
}

data class VideoLinkBookingOptions(
  @ApiModelProperty(value = "True if the specified rooms are available at the specified times.")
  val matched: Boolean,

  @ApiModelProperty(value = "If the specification could not be met then up to three alternative booking times are offered.")
  val alternatives: List<VideoLinkBookingOption>,
)
