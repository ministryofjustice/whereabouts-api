package uk.gov.justice.digital.hmpps.whereabouts.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Location Details
 **/
@ApiModel(description = "Location Details")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Location {
    @ApiModelProperty(required = true, value = "Location identifier.", example = "721705")
    @NotNull
    private Long locationId;

    @ApiModelProperty(required = true, value = "Location type.", example = "ROOM")
    @NotBlank
    private String locationType;

    @ApiModelProperty(required = true, value = "Location description.", example = "MDI-RES-HB1-ALE")
    @NotBlank
    private String description;

    @ApiModelProperty(value = "What events this room can be used for.", example = "APP")
    private String locationUsage;

    @ApiModelProperty(required = true, value = "Identifier of Agency this location is associated with.", example = "MDI")
    @NotBlank
    private String agencyId;

    @ApiModelProperty(value = "Identifier of this location's parent location.", example = "26960")
    private Long parentLocationId;

    @ApiModelProperty(value = "Current occupancy of location.", example = "10")
    private Integer currentOccupancy;

    @ApiModelProperty(value = "Location prefix. Defines search prefix that will constrain search to this location and its subordinate locations.", example = "RES-HB1-ALE")
    private String locationPrefix;

    @ApiModelProperty(value = "Operational capacity of the location.", example = "20")
    private Integer operationalCapacity;

    @ApiModelProperty(value = "User-friendly location description.", example = "RES-HB1-ALE")
    private String userDescription;

    private String internalLocationCode;
}
