package uk.gov.justice.digital.hmpps.whereabouts.integration

import org.junit.jupiter.api.Test
import java.time.LocalDate

class AppointmentsIntegrationTest : IntegrationTest() {

  @Test
  fun `getAppointments valid call with offenderLocationPrefix returns expected data`() {
    prisonApiMockServer.stubGetScheduledAppointmentsByAgencyAndDate("MDI", "A1234AA")
    prisonApiMockServer.stubGetOffenderBookings("A1234AA", "MDI-1-2", true)

    webTestClient.get()
      .uri {
        it.path("/appointments/MDI")
          .queryParam("date", LocalDate.of(2020, 12, 25))
          .queryParam("offenderLocationPrefix", "MDI-1")
          .build()
      }
      .headers(setHeaders())
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.size()").isEqualTo(1)
      .jsonPath("[0].offenderNo").isEqualTo("A1234AA")
  }
}
