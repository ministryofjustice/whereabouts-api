package uk.gov.justice.digital.hmpps.whereabouts.services;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import uk.gov.justice.digital.hmpps.whereabouts.dto.prisonApi.CaseNoteDto;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class CaseNotesService {
    private final WebClient webClient;

    public CaseNotesService(@Qualifier("caseNoteWebClient") final WebClient webClient) {
        this.webClient = webClient;
    }

    public CaseNoteDto postCaseNote(final String offenderNo, final String type, final String subType, final String text, final LocalDateTime occurrence) {
        return webClient.post()
                .uri("/case-notes/{offenderNo}", offenderNo)
                .bodyValue(Map.of(
                        "type", type,
                        "subType", subType,
                        "text", text,
                        "occurrenceDateTime", occurrence.toString()))
                .retrieve()
                .bodyToMono(CaseNoteDto.class)
                .block();
    }

    public void putCaseNoteAmendment(final String offenderNo, final long caseNoteId, final String text) {
        webClient.put()
                .uri("/case-notes/{offenderNo}/{caseNoteId}", offenderNo, caseNoteId)
                .bodyValue(Map.of("text", text))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}

