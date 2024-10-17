package uk.gov.justice.digital.hmpps.whereabouts.model

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * Location Details
 */
@ApiModel(description = "Location Details")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class LocationDetails(
  @ApiModelProperty(required = true, value = "Location identifier.", example = "1b7c251b-177f-40bc-a75f-1a458284fe14")
  val id: String,

  @ApiModelProperty(required = true, value = "Name", example = "Pcvl - Harrow Conf - Room 4")
  val localName: String?,

  @ApiModelProperty(required = true, value = "Path Hierarchy", example = "A")
  val pathHierarchy: String,

  @ApiModelProperty(required = true, value = "leaf Level", example = "true")
  val leafLevel: Boolean,

  @ApiModelProperty(required = true, value = "Active location", example = "true")
  val active: Boolean,

  @ApiModelProperty(required = true, value = "Location Type.", example = "MDI-RES-HB1-ALE")
  val locationType: String,
)
