package uk.gov.justice.digital.hmpps.whereabouts.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason;
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ApiModel(description = "Attendance details to create")
public class CreateAttendanceDto {

    @ApiModelProperty(required = true)
    @NotNull
    private Long bookingId;

    @ApiModelProperty(required = true)
    @NotEmpty
    private String offenderNo;

    @ApiModelProperty(required = true)
    @NotNull
    private Long eventId;

    @ApiModelProperty(required = true)
    @NotNull
    private Long eventLocationId;

    @ApiModelProperty(required = true)
    @NotNull
    private TimePeriod period;

    @ApiModelProperty(required = true)
    @NotNull
    private String prisonId;

    @ApiModelProperty(required = true)
    @NotNull
    private Boolean attended;

    @ApiModelProperty(required = true)
    @NotNull
    private Boolean paid;

    private AbsentReason absentReason;

    @JsonFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(required = true)
    @NotNull
    private LocalDate eventDate;

    @Length(max = 500)
    private String comments;
}
