package uk.gov.justice.digital.hmpps.whereabouts.dto.prisonapi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class AmendmentDto {
    private LocalDateTime creationDateTime;
    private String authorName;
    private String additionalNotesText;
}
