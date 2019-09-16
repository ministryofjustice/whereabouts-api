package uk.gov.justice.digital.hmpps.whereabouts.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ApiModel(description = "Attendances response")
public class AttendancesResponse {

    @ApiModelProperty(required = true, value = "Set of attendances")
    private Set<AttendanceDto> attendances;
}

