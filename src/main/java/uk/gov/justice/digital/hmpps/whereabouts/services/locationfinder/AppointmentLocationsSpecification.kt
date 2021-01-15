package uk.gov.justice.digital.hmpps.whereabouts.services.locationfinder

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.time.LocalDate
import javax.validation.constraints.NotBlank

@ApiModel(description = "A specification that returned locations should satisfy.")
data class AppointmentLocationsSpecification(
  @ApiModelProperty(
    value = "The appointment intervals are all on this date. ISO-8601 date format (YYYY-MM-DD)",
    example = "2021-01-01"
  )
  val date: LocalDate,

  @ApiModelProperty(value = "The locations must be within the agency (prison) having this identifier.", example = "WWI")
  @field:NotBlank
  val agencyId: String,

  @ApiModelProperty(
    value = "When searching for locations include locations that are currently part of these Video Link Bookings.",
    example = "[1,3,5]"
  )
  val vlbIdsToExclude: List<Long>,

  @ApiModelProperty(value = "Find the sets of locations that are available for these intervals.")
  val appointmentIntervals: List<@ValidInterval Interval>
)
