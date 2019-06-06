package uk.gov.justice.digital.hmpps.whereabouts.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ApiModel(description = "Attendance update details")
public class UpdateAttendanceDto {
    @ApiModelProperty(required = true)
    @NotNull
    private Boolean attended;

    @ApiModelProperty(required = true)
    @NotNull
    private Boolean paid;

    private AbsentReason absentReason;

    @Length(max = 500)
    private String comments;
}
