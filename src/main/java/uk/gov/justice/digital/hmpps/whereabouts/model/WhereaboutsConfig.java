package uk.gov.justice.digital.hmpps.whereabouts.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * Whereabouts Details
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Whereabouts Details")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WhereaboutsConfig {

    /**
     * Whether this prison is enabled for whereabouts
     */
    @ApiModelProperty(required = true, value = "Whether this prison is enabled for whereabouts")
    @NotNull
    private boolean enabled;
}
