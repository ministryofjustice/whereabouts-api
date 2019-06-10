package uk.gov.justice.digital.hmpps.whereabouts.services;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
class EventOutcome {
    private final String eventOutcome;
    private final String performance;
}
