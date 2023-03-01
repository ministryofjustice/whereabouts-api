package uk.gov.justice.digital.hmpps.whereabouts.model

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * Whereabouts Details
 */
@ApiModel(description = "Whereabouts Details")
data class WhereaboutsConfig(
  @ApiModelProperty(required = true, value = "Whether this prison is enabled for whereabouts")
  val enabled: Boolean,
)
