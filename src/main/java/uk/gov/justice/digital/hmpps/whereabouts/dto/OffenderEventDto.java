package uk.gov.justice.digital.hmpps.whereabouts.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class OffenderEventDto {

    @NotNull
    @Length(max = 10)
    private String offenderNo;
    @NotNull
    private Long eventId;  //identifier returned for ACT/APP/VISIT  -  needs adding to elite2api (only exists for activity)
    @NotNull
    private String eventType;
    @NotNull
    private LocalDate eventDate;
    @NotNull
    private String period;
    @NotNull
    private String prisonId;
    private Boolean currentLocation;
}
