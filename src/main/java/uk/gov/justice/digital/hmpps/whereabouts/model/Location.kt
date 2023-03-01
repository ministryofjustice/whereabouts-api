package uk.gov.justice.digital.hmpps.whereabouts.model

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * Location Details
 */
@ApiModel(description = "Location Details")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class Location(
  @ApiModelProperty(required = true, value = "Location identifier.", example = "721705")
  val locationId: Long,

  @ApiModelProperty(required = true, value = "Location type.", example = "ROOM")
  val locationType: String,

  @ApiModelProperty(required = true, value = "Location description.", example = "MDI-RES-HB1-ALE")
  val description: String,

  @ApiModelProperty(value = "What events this room can be used for.", example = "APP")
  val locationUsage: String? = null,

  @ApiModelProperty(required = true, value = "Identifier of Agency this location is associated with.", example = "MDI")
  val agencyId: String,

  @ApiModelProperty(value = "Identifier of this location's parent location.", example = "26960")
  val parentLocationId: Long? = null,

  @ApiModelProperty(value = "Current occupancy of location.", example = "10")
  val currentOccupancy: Int,

  @ApiModelProperty(
    value = "Location prefix. Defines search prefix that will constrain search to this location and its subordinate locations.",
    example = "RES-HB1-ALE",
  )
  val locationPrefix: String,

  @ApiModelProperty(value = "Operational capacity of the location.", example = "20")
  val operationalCapacity: Int,

  @ApiModelProperty(value = "User-friendly location description.", example = "RES-HB1-ALE")
  val userDescription: String? = null,

  val internalLocationCode: String = "",
)
