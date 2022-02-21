package uk.gov.justice.digital.hmpps.whereabouts.integration

import com.github.tomakehurst.wiremock.client.WireMock.anyUrl
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.util.UriComponentsBuilder

class EventsIntegrationTest : IntegrationTest() {
  @BeforeEach
  fun beforeEach() {
    prisonApiMockServer.resetAll()
  }

  @Test
  fun `should pass through json from Prison API`() {
    val eventsJson = loadJsonFile("events.json")

    prisonApiMockServer.stubGetEvents(eventsJson)

    webTestClient.get()
      .uri("/events/ABC123")
      .headers(setHeaders())
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .json(eventsJson)

    prisonApiMockServer.verify(
      getRequestedFor(anyUrl())
        .withUrl("/api/offenders/ABC123/events?fromDate=&toDate=")
    )
  }

  @Test
  fun `should pass through date parameters`() {
    prisonApiMockServer.stubGetEvents("[]")

    webTestClient.get()
      .uri(
        UriComponentsBuilder.fromUriString("/events/ABC123")
          .queryParam("fromDate", "2022-02-20")
          .queryParam("toDate", "2022-03-20")
          .build()
          .toString()
      )
      .headers(setHeaders())
      .exchange()
      .expectStatus().isOk

    prisonApiMockServer.verify(
      getRequestedFor(anyUrl())
        .withUrl("/api/offenders/ABC123/events?fromDate=2022-02-20&toDate=2022-03-20")
    )
  }
}
