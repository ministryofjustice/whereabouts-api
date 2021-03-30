package uk.gov.justice.digital.hmpps.whereabouts.services.locationfinder

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * The set of Locations (as a list) which satisfies some part of an AppointmentLocationSpecification.
 * Each AvailableLocations object corresponds to an appointmentInterval in the specification.
 */
@ApiModel(description = "The set of Locations (as an array) which satisfies some part of an AppointmentLocationSpecification.")
data class AvailableLocations(
  @ApiModelProperty(value = "The appointmentInterval where the locations are available.")
  val appointmentInterval: Interval,

  @ApiModelProperty(value = "The locations that may be booked for the duration of the appointment interval.")
  val locations: List<LocationIdAndDescription>
)
