package uk.gov.justice.digital.hmpps.whereabouts.model

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * Cell With Attributes Details
 */
@ApiModel(description = "Cell with Attributes Details")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class CellWithAttributes(
  @ApiModelProperty(required = true, value = "Location identifier.", example = "721705")
  val id: Long,
  @ApiModelProperty(required = true, value = "Location description.", example = "MDI-RES-HB1-ALE")
  val description: String,
  @ApiModelProperty(required = true, value = "Current occupancy of location.", example = "10")
  val noOfOccupants: Int,
  @ApiModelProperty(required = true, value = "Capacity of the location.", example = "20")
  val capacity: Int,
  @ApiModelProperty(value = "User-friendly location description.", example = "RES-HB1-ALE")
  val userDescription: String? = null,
  @ApiModelProperty(value = "List of attributes for the cell.", example = "Listener cell")
  val attributes: List<CellAttribute> = listOf()
)
