package uk.gov.justice.digital.hmpps.whereabouts.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason;
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ApiModel(description = "Attendance details")
public class CreateAttendanceDto {

    @ApiModelProperty(required = true)
    @Min(1)
    private long bookingId;

    @ApiModelProperty(required = true)
    @NotEmpty
    private String offenderNo;

    @ApiModelProperty(required = true)
    @Min(1)
    private long eventId;

    @ApiModelProperty(required = true)
    @NotNull
    @Min(1)
    private long eventLocationId;

    @ApiModelProperty(required = true)
    @NotNull
    private TimePeriod period;

    @ApiModelProperty(required = true)
    @NotNull
    private String prisonId;

    @ApiModelProperty(required = true)
    @NotNull
    private boolean attended;

    @ApiModelProperty(required = true)
    @NotNull
    private boolean paid;

    private AbsentReason absentReason;

    @JsonFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(required = true)
    @NotNull
    private LocalDate eventDate;

    private String comments;
}
