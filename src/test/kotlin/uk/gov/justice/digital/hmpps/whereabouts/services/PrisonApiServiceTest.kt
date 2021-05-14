package uk.gov.justice.digital.hmpps.whereabouts.services

import com.github.tomakehurst.wiremock.client.WireMock.absent
import com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException.InternalServerError
import uk.gov.justice.digital.hmpps.whereabouts.dto.prisonapi.ScheduledAppointmentDto
import uk.gov.justice.digital.hmpps.whereabouts.integration.wiremock.PrisonApiMockServer
import uk.gov.justice.digital.hmpps.whereabouts.model.Location
import java.time.LocalDate

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
      deleteRequestedFor(urlEqualTo("/api/appointments/$appointmentId"))
    )
  }

  @Test
  fun `when appointment to delete is not found`() {
    val appointmentId = 1L
    prisonApiMockServer.stubDeleteAppointment(appointmentId, 404)
    prisonApiService.deleteAppointment(appointmentId)
    prisonApiMockServer.verify(
      deleteRequestedFor(urlEqualTo("/api/appointments/$appointmentId"))
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
      deleteRequestedFor(urlEqualTo("/api/appointments/$appointmentId"))
    )
  }

  @Test
  fun `update appointment comment - main flow`() {
    val appointmentId = 100L
    val comment = "New comment"

    prisonApiMockServer.stubUpdateAppointmentComment(appointmentId)
    prisonApiService.updateAppointmentComment(appointmentId, comment)
    prisonApiMockServer.verify(
      putRequestedFor(urlEqualTo("/api/appointments/$appointmentId/comment"))
        .withRequestBody(equalTo(comment))
    )
  }

  @Test
  fun `update appointment comment - empty comment`() {
    val appointmentId = 100L
    val comment = ""

    prisonApiMockServer.stubUpdateAppointmentComment(appointmentId)
    prisonApiService.updateAppointmentComment(appointmentId, comment)
    prisonApiMockServer.verify(
      putRequestedFor(urlEqualTo("/api/appointments/$appointmentId/comment"))
        .withRequestBody(absent())
    )
  }

  @Test
  fun `update appointment comment - null comment`() {
    val appointmentId = 100L
    val comment = null

    prisonApiMockServer.stubUpdateAppointmentComment(appointmentId)
    prisonApiService.updateAppointmentComment(appointmentId, comment)
    prisonApiMockServer.verify(
      putRequestedFor(urlEqualTo("/api/appointments/$appointmentId/comment"))
        .withRequestBody(absent())
    )
  }

  @Test
  fun `get appointments for agency on date`() {
    val date = LocalDate.of(2020, 12, 25)
    val agencyId = "WWI"

    prisonApiMockServer.stubGetScheduledAppointmentsByAgencyAndDate(agencyId)
    val scheduledAppointments = prisonApiService.getScheduledAppointments(agencyId, date, null, null)
    assertThat(scheduledAppointments).containsExactlyInAnyOrder(
      ScheduledAppointmentDto(
        id = 1L,
        agencyId = agencyId,
        locationId = 10L,
        appointmentTypeCode = "VLB",
        startTime = date.atTime(9, 0),
        endTime = date.atTime(9, 30),
        offenderNo = "A1234AA"
      ),
      ScheduledAppointmentDto(
        id = 2L,
        agencyId = agencyId,
        locationId = 11L,
        appointmentTypeCode = "MEH",
        startTime = date.atTime(10, 0),
        endTime = date.atTime(10, 30),
        offenderNo = "B2345BB"
      )
    )
  }

  @Test
  fun `get Agency Locations for type, unrestricted`() {
    prisonApiMockServer.stubGetAgencyLocationsForTypeUnrestricted("WWI")
    val locations = prisonApiService.getAgencyLocationsForTypeUnrestricted("WWI", "APP")
    assertThat(locations)
      .containsExactly(
        Location(
          locationId = 1L,
          locationType = "VIDE",
          description = "A VLB location",
          agencyId = "WWI",
          currentOccupancy = 0,
          locationPrefix = "XXX",
          operationalCapacity = 10
        )
      )
  }
}
