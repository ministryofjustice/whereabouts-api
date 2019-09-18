package uk.gov.justice.digital.hmpps.whereabouts.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason;
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class AttendanceDto {
    private Long id;
    private Long bookingId;
    private Long eventId;
    private Long eventLocationId;
    private TimePeriod period;
    private String prisonId;
    private Boolean attended;
    private AbsentReason absentReason;
    private Boolean paid;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate eventDate;
    private String comments;
    private LocalDateTime createDateTime;
    private String createUserId;
    private LocalDateTime modifyDateTime;
    private String modifyUserId;
    private Long caseNoteId;
    private Boolean locked;
}
