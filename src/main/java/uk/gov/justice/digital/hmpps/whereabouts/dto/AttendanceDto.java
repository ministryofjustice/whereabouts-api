package uk.gov.justice.digital.hmpps.whereabouts.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class AttendanceDto {
    private long id;

    @NotNull
    private long bookingId;

    @NotNull
    private String offenderNo;

    @NotNull
    private long eventId;

    @NotNull
    private long eventLocationId;

    @NotNull
    private String period;

    @NotNull
    private String prisonId;

    @NotNull
    private boolean attended;

    private AbsentReason absentReason;

    @NotNull
    private boolean paid;

    @JsonFormat(pattern="yyyy-MM-dd")
    @NotNull
    private LocalDate eventDate;
}
