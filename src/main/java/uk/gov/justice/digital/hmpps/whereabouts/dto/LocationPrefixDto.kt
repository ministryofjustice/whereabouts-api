package uk.gov.justice.digital.hmpps.whereabouts.dto

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel(description = "Location prefix response")
data class LocationPrefixDto (
    @ApiModelProperty(value ="Location prefix translated from group name", example = "MDI-1-")
    val locationPrefix: String
)