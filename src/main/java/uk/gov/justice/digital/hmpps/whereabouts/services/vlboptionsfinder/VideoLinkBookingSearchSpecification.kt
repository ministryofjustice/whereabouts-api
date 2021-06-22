package uk.gov.justice.digital.hmpps.whereabouts.services.vlboptionsfinder

import io.swagger.annotations.ApiModelProperty
import uk.gov.justice.digital.hmpps.whereabouts.services.locationfinder.LocationAndInterval
import java.time.LocalDate
import javax.validation.constraints.NotBlank

data class VideoLinkBookingSearchSpecification(
  @ApiModelProperty(value = "The locations must be within the agency (prison) having this identifier.", example = "WWI")
  @field:NotBlank
  val agencyId: String,

  @ApiModelProperty(value = "The appointment intervals are all on this date.", example = "2021-01-01")
  val date: LocalDate,

  @ApiModelProperty(
    value = "When searching for locations include locations that are currently part of these Video Link Bookings.",
    example = "[1,3,5]"
  )

  val preAppointment: LocationAndInterval? = null,
  val mainAppointment: LocationAndInterval,
  val postAppointment: LocationAndInterval? = null,
  val vlbIdToExclude: Long? = null,
)
