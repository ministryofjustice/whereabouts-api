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
import java.util.ArrayList;
import java.util.List;

/**
 * Cell Locations are grouped for unlock lists as a 2 level tree. The two levels are referred to as Location and Sub-Location in the prisonstaffhub UI. Each (location/sub-location) group has a name that is understood by prison officers and also serves as a key to retrieve the corresponding Cell Locations and information about their occupants.
 **/
@ApiModel(description = "Cell Locations are grouped for unlock lists as a 2 level tree. The two levels are referred to as Location and Sub-Location in the prisonstaffhub UI. Each (location/sub-location) group has a name that is understood by prison officers and also serves as a key to retrieve the corresponding Cell Locations and information about their occupants.")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class LocationGroup {
    @ApiModelProperty(required = true, value = "The name of the group", example = "Block A")
    @NotBlank
    private String name;

    @ApiModelProperty(required = true, value = "A key for the group", example = "A")
    @NotBlank
    private String key;

    @ApiModelProperty(required = true, value = "The child groups of this group", example = "[{\"name\": \"Landing A/1\", \"key\":\"1\"}, {\"name\": \"Landing A/2\", \"key\": \"2\"}]")
    @NotNull
    @Builder.Default
    private List<LocationGroup> children = new ArrayList<>();
}

