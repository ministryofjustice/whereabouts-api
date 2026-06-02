package uk.gov.justice.digital.hmpps.whereabouts.services.health

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.hmpps.kotlin.health.HealthPingCheck

@Component
class PrisonApiHealth(prisonApiHealthWebClient: WebClient) : HealthPingCheck(prisonApiHealthWebClient)

@Component
class CaseNotesApiHealth(caseNoteHealthWebClient: WebClient) : HealthPingCheck(caseNoteHealthWebClient)

@Component
class OAuthApiHealth(oAuthHealthWebClient: WebClient) : HealthPingCheck(oAuthHealthWebClient)
