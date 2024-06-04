package uk.gov.justice.digital.hmpps.whereabouts.services

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.whereabouts.integration.wiremock.PrisonRegisterApiMockServer
import uk.gov.justice.digital.hmpps.whereabouts.services.court.VideoLinkBookingService

class PrisonRegisterApiServiceTest {

  private lateinit var prisonRegisterClient: PrisonRegisterClient

  companion object {
    @JvmField
    internal val prisonRegisterApiMockServer = PrisonRegisterApiMockServer()

    @BeforeAll
    @JvmStatic
    fun startMocks() {
      prisonRegisterApiMockServer.start()
    }

    @AfterAll
    @JvmStatic
    fun stopMocks() {
      prisonRegisterApiMockServer.stop()
    }
  }

  @BeforeEach
  fun resetStubs() {
    prisonRegisterApiMockServer.resetAll()
    val webClient = WebClient.create("http://localhost:${prisonRegisterApiMockServer.port()}")
    prisonRegisterClient = PrisonRegisterClient(webClient)
  }

  @Test
  fun `happy path`() {
    prisonRegisterApiMockServer.stubGetPrisonEmailAddress("NMI")
    var result = prisonRegisterClient.getPrisonEmailAddress("NMI", VideoLinkBookingService.DepartmentType.VIDEOLINK_CONFERENCING_CENTRE)
    Assertions.assertEquals(result?.emailAddress, "someEmailAddress@gov.uk")
  }

  @Test
  fun `details not found`() {
    prisonRegisterApiMockServer.stubGetPrisonEmailAddressReturnsNotFound("NMI")
    Assertions.assertThrows(WebClientResponseException.NotFound::class.java) {
      prisonRegisterClient.getPrisonEmailAddress("NMI", VideoLinkBookingService.DepartmentType.VIDEOLINK_CONFERENCING_CENTRE)
    }
  }
}
