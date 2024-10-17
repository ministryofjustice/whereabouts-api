package uk.gov.justice.digital.hmpps.whereabouts.services

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.whereabouts.model.LocationDetails

@Component
class LocationApiClient(@Qualifier("locationApiWebClient") private val webClient: WebClient) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun getNonResidentialLocationForPrison(prisonId: String, usageType: String): List<LocationDetails> {
    return webClient.get()
      .uri("/locations/prison/$prisonId/non-residential-usage-type/$usageType")
      .retrieve()
      .bodyToMono(object : ParameterizedTypeReference<List<LocationDetails>>() {})
      .block()
  }
}
