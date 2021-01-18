package uk.gov.justice.digital.hmpps.whereabouts.services.locationfinder

import io.swagger.annotations.ApiModelProperty
import java.time.LocalDate
import javax.validation.constraints.NotBlank

data class VideoLinkBookingLocationsSpecification(
  @ApiModelProperty(value = "The appointment intervals are all on this date.", example = "2021-01-01")
  val date: LocalDate,

  @ApiModelProperty(value = "The locations must be within the agency (prison) having this identifier.", example = "WWI")
  @field:NotBlank
  val agencyId: String,

  @ApiModelProperty(
    value = "When searching for locations include locations that are currently part of these Video Link Bookings.",
    example = "[1,3,5]"
  )
  val vlbIdsToExclude: List<Long>,

  @ApiModelProperty(value = "If present find the locations that can be used for the pre interval.", required = false)
  @field:ValidInterval
  val preInterval: Interval?,

  @ApiModelProperty(value = "Find the locations that can be used for the main interval.")
  @field:ValidInterval
  val mainInterval: Interval,

  @ApiModelProperty(value = "If present find the locations that can be used for the post interval", required = false)
  @field:ValidInterval
  val postInterval: Interval?
) {
  fun toAppointmentLocationsSpecification() =
    AppointmentLocationsSpecification(
      date = date,
      agencyId = agencyId,
      vlbIdsToExclude = vlbIdsToExclude,
      appointmentIntervals = listOfNotNull(preInterval, mainInterval, postInterval)
    )
}
