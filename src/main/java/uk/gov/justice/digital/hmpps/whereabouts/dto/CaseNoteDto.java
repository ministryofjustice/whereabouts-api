package uk.gov.justice.digital.hmpps.whereabouts.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class CaseNoteDto {
    private long caseNoteId;
    private long bookingId;
    private long staffId;
    private String type;
    private String typeDescription;
    private String subType;
    private String subTypeDescription;
    private String source;
    private LocalDateTime creationDateTime;
    private LocalDateTime occurrenceDateTime;
    private String authorName;
    private String text;
    private String originalNoteText;
    private String agencyId;
    private List<Amendment> amendments;
}

