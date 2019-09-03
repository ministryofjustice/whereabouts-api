package uk.gov.justice.digital.hmpps.whereabouts.services;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.hmpps.whereabouts.dto.elite.CaseNoteDto;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class CaseNotesService {
    private final OAuth2RestTemplate restTemplate;

    public CaseNotesService(@Qualifier("caseNotesApiRestTemplate") final OAuth2RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public CaseNoteDto postCaseNote(final String offenderNo, final String type, final String subType, final String text, final LocalDateTime occurrence) {
        final var url = "/case-notes/{offenderNo}";

        final var response = restTemplate.postForEntity(
                url,
                Map.of(
                        "type", type,
                        "subType", subType,
                        "text", text,
                        "occurrence", occurrence.toString()),
                CaseNoteDto.class, offenderNo);

        return response.getBody();
    }

    public void putCaseNoteAmendment(final String offenderNo, final long caseNoteId, final String text) {
        final var url = "/case-notes/{offenderNo}/{caseNoteId}";

        restTemplate.put(url, Map.of("text", text), offenderNo, caseNoteId);
    }
}

