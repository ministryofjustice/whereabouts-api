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

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ApiModel(description = "Attendance details to create")
public class CreateAttendanceDto {
    @ApiModelProperty(required = true, value = "Id of active booking", example = "1")
    @NotNull
    private Long bookingId;

    @ApiModelProperty(required = true, value = "Id of event", example = "2")
    @NotNull
    private Long eventId;

    @ApiModelProperty(required = true, value = "Id of the location the event is taking place", example = "4")
    @NotNull
    private Long eventLocationId;

    @ApiModelProperty(required = true, value = "Time period for the event", example = "AM")
    @NotNull
    private TimePeriod period;

    @ApiModelProperty(required = true, value = "Id of prison the event is taking place", example = "LEI")
    @NotNull
    private String prisonId;

    @ApiModelProperty(required = true, value = "Flag to indicate the offender attended the event", example = "true")
    @NotNull
    private Boolean attended;

    @ApiModelProperty(required = true, value = "Flag to indicate the offender should be paid", example = "true")
    @NotNull
    private Boolean paid;

    @ApiModelProperty(value = "Reason the offender did not attendance the event", example = "Refused")
    private AbsentReason absentReason;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(required = true, value = "Date the event is scheduled", example = "2019-10-01")
    @NotNull
    private LocalDate eventDate;

    @Length(max = 240)
    @ApiModelProperty(value = "Comments about non attendance. This also gets used for the IEP warning text ")
    private String comments;
}
