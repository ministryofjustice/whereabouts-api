package uk.gov.justice.digital.hmpps.whereabouts.integration

import com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.junit.jupiter.api.Test

class CellIntegrationTest : IntegrationTest() {
  @Test
  fun `make a request to change an offenders cell`() {
    val bookingId = 10L
    val agencyId = "MDI"
    val assignedLivingUnitId = 25700L
    val internalLocationDescription = "MDI-2-2-006"
    val reasonCode = "ADM"

    prisonApiMockServer.stubMakeCellMove(
      bookingId = bookingId,
      agencyId = agencyId,
      assignedLivingUnitId = assignedLivingUnitId,
      internalLocationDescription = internalLocationDescription
    )

    webTestClient.post()
      .uri("/cell/make-cell-move")
      .bodyValue(
        mapOf(
          "bookingId" to bookingId,
          "internalLocationDescriptionDestination" to internalLocationDescription,
          "cellMoveReasonCode" to reasonCode
        )
      )
      .headers(setHeaders())
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .json(loadJsonFile("cell-move-details.json"))

    prisonApiMockServer.verify(
      putRequestedFor(
        urlEqualTo("/api/bookings/$bookingId/living-unit/$internalLocationDescription?reasonCode=$reasonCode")
      )
    )
  }
}
