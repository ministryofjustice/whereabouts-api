package uk.gov.justice.digital.hmpps.whereabouts.services.locationfinder

import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.NotNull

data class LocationAndInterval(
  @field:NotNull
  val locationId: Long,

  @ApiModelProperty(value = "If present find the locations that can be used for the pre interval.", required = false)
  @field:ValidInterval
  val interval: Interval
)
