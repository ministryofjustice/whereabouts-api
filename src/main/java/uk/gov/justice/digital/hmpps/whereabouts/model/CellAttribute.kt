package uk.gov.justice.digital.hmpps.whereabouts.model

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

/**
 * Cell Attribute Details
 */
@ApiModel(description = "Cell Attribute Details")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class CellAttribute (
  @ApiModelProperty(required = true, value = "Cell attribute code", example = "LC")
  val code: String,
  @ApiModelProperty(required = true, value = "Cell attribute description", example = "Listener cell")
  val description: String
)