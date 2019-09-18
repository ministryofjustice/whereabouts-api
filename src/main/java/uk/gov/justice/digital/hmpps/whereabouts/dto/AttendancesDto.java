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

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ApiModel(description = "Attend all parameters")
public class AttendancesDto {
    @ApiModelProperty(required = true, value = "Set of active booking and activity ids")
    @NotNull
    private Set<BookingActivity> bookingActivities;

    @ApiModelProperty(required = true, value = "Id of the location the event is taking place", example = "4")
    @NotNull
    private Long eventLocationId;

    @ApiModelProperty(required = true, value = "Time period for the event", example = "AM")
    @NotNull
    private TimePeriod period;

    @ApiModelProperty(required = true, value = "Id of prison the event is taking place", example = "LEI")
    @NotNull
    private String prisonId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(required = true, value = "Date the event is scheduled", example = "2019-10-01")
    @NotNull
    private LocalDate eventDate;

    @ApiModelProperty(value = "Absent reason", example = "Refused")
    private AbsentReason reason;

    @ApiModelProperty(value = "Indication of attendance", example = "true")
    @NotNull
    private Boolean attended;

    @ApiModelProperty(value = "Indicates that the offender should be paid", example = "true")
    @NotNull
    private Boolean paid;

    @ApiModelProperty(value = "Comment describing the offenders absence", example = "They had a medical appointment scheduled")
    private String comments;
}
