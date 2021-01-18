package uk.gov.justice.digital.hmpps.whereabouts.services.locationfinder

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel(description = "A minimal representation of a NOMIS agency internal location.")
data class LocationIdAndDescription(
  @ApiModelProperty(value = "The NOMIS agency internal location identifier of the location", example = "12345")
  val locationId: Long,

  @ApiModelProperty(value = "The NOMIS description of the location", example = "VCC Room 16")
  val description: String
)
