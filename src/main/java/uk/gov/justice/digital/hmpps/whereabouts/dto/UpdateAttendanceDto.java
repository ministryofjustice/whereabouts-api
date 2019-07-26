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
    @ApiModelProperty(required = true, value = "Flag to indicate the offender attended the event", example = "true")
    @NotNull
    private Boolean attended;

    @ApiModelProperty(required = true, value = "Flag to indicate the offender should be paid", example = "true")
    @NotNull
    private Boolean paid;

    @ApiModelProperty(value = "Reason the offender did not attendance the event", example = "Refused")
    private AbsentReason absentReason;

    @Length(max = 240)
    @ApiModelProperty( value = "Comments about non attendance. This also gets used for the IEP warning text ")
    private String comments;
}
