package uk.gov.justice.digital.hmpps.whereabouts.services.locationfinder

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel(description = "The sets of Locations that are available for the main, and optionally pre/post Intervals of a VideoLinkBookingLocationsSpecification.")
data class AvailableVideoLinkBookingLocations(
  @ApiModelProperty(
    value = "If present the locations that may be booked for the duration of the pre appointment interval.",
    required = false
  )
  val pre: List<LocationIdAndDescription>?,

  @ApiModelProperty(value = "The locations that may be booked for the duration of the main appointment interval.")
  val main: List<LocationIdAndDescription>,

  @ApiModelProperty(
    value = "If present the locations that may be booked for the duration of the post appointment interval.",
    required = false
  )
  val post: List<LocationIdAndDescription>?
) {
  companion object {
    fun fromAvailableLocations(list: List<AvailableLocations>, hasPreInterval: Boolean) =
      list
        .map { it.locations }
        .let {
          when (it.size) {
            1 -> AvailableVideoLinkBookingLocations(null, it[0], null)

            2 ->
              if (hasPreInterval) {
                AvailableVideoLinkBookingLocations(it[0], it[1], null)
              } else {
                AvailableVideoLinkBookingLocations(null, it[0], it[1])
              }

            3 -> AvailableVideoLinkBookingLocations(it[0], it[1], it[2])

            else -> throw IllegalArgumentException("Should never happen.")
          }
        }
  }
}
