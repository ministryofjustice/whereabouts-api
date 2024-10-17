package uk.gov.justice.digital.hmpps.whereabouts.model

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel(description = "A minimal representation of internal location.")
data class LocationInsidePrisonIdAndDescription(
  @ApiModelProperty(value = "The NOMIS agency internal location identifier of the location", example = "c9e4d7e5-67cc-4e2c-a58e-9a252a5a460b")
  val locationId: String,

  @ApiModelProperty(value = "The description of the location", example = "VCC Room 16")
  val description: String,
)
