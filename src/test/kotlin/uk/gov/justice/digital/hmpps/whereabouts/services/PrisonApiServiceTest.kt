package uk.gov.justice.digital.hmpps.whereabouts.services

import com.github.tomakehurst.wiremock.client.WireMock
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException.InternalServerError
import uk.gov.justice.digital.hmpps.whereabouts.integration.wiremock.PrisonApiMockServer

class PrisonApiServiceTest {

  private lateinit var prisonApiService: PrisonApiService

  companion object {
    @JvmField
    internal val prisonApiMockServer = PrisonApiMockServer()

    @BeforeAll
    @JvmStatic
    fun startMocks() {
      prisonApiMockServer.start()
    }

    @AfterAll
    @JvmStatic
    fun stopMocks() {
      prisonApiMockServer.stop()
    }
  }

  @BeforeEach
  fun resetStubs() {
    prisonApiMockServer.resetAll()
    prisonApiService = PrisonApiService(WebClient.create("http://localhost:${prisonApiMockServer.port()}/api"))

  }

  @Test
  fun `when successful delete`() {
    val appointmentId = 1L
    prisonApiMockServer.stubDeleteAppointment(appointmentId, 200)
    prisonApiService.deleteAppointment(appointmentId)
    prisonApiMockServer.verify(
      WireMock.deleteRequestedFor(WireMock.urlEqualTo("/api/appointments/${appointmentId}"))
    )
  }

  @Test
  fun `when appointment to delete is not found`() {
    val appointmentId = 1L
    prisonApiMockServer.stubDeleteAppointment(appointmentId, 404)
    prisonApiService.deleteAppointment(appointmentId)
    prisonApiMockServer.verify(
      WireMock.deleteRequestedFor(WireMock.urlEqualTo("/api/appointments/${appointmentId}"))
    )
  }

  @Test
  fun `when an unexpected error is thrown when deleting an appointment`() {
    val appointmentId = 1L
    prisonApiMockServer.stubDeleteAppointment(appointmentId, 500)
    Assertions.assertThrows(InternalServerError::class.java) {
      prisonApiService.deleteAppointment(appointmentId)
    }
    prisonApiMockServer.verify(
      WireMock.deleteRequestedFor(WireMock.urlEqualTo("/api/appointments/${appointmentId}"))
    )
  }

}
