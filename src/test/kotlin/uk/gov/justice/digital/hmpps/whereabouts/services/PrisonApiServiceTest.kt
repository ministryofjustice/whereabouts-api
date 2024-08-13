package uk.gov.justice.digital.hmpps.whereabouts.services

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
import org.springframework.data.domain.Pageable
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException.InternalServerError
import uk.gov.justice.digital.hmpps.whereabouts.dto.prisonapi.OffenderAttendance
import uk.gov.justice.digital.hmpps.whereabouts.integration.wiremock.PrisonApiMockServer
import uk.gov.justice.digital.hmpps.whereabouts.model.Location
import uk.gov.justice.digital.hmpps.whereabouts.services.PrisonApi.EventPropagation.ALLOW
import uk.gov.justice.digital.hmpps.whereabouts.services.PrisonApi.EventPropagation.DENY
import uk.gov.justice.digital.hmpps.whereabouts.services.court.UpdateComment
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
    val webClient = WebClient.create("http://localhost:${prisonApiMockServer.port()}/api")
    prisonApiService = PrisonApiService(webClient)
  }

  @Test
  fun `when successful delete`() {
    val appointmentId = 1L
    prisonApiMockServer.stubDeleteAppointment(appointmentId, 200)
    prisonApiService.deleteAppointment(appointmentId, DENY)
    prisonApiMockServer.verify(
      deleteRequestedFor(urlEqualTo("/api/appointments/$appointmentId"))
        .withHeader("no-event-propagation", equalTo("true")),
    )
  }

  @Test
  fun `when appointment to delete is not found`() {
    val appointmentId = 1L
    prisonApiMockServer.stubDeleteAppointment(appointmentId, 404)
    prisonApiService.deleteAppointment(appointmentId, ALLOW)
    prisonApiMockServer.verify(
      deleteRequestedFor(urlEqualTo("/api/appointments/$appointmentId"))
        .withHeader("no-event-propagation", equalTo("false")),
    )
  }

  @Test
  fun `when an unexpected error is thrown when deleting an appointment`() {
    val appointmentId = 1L
    prisonApiMockServer.stubDeleteAppointment(appointmentId, 500)
    Assertions.assertThrows(InternalServerError::class.java) {
      prisonApiService.deleteAppointment(appointmentId, ALLOW)
    }
    prisonApiMockServer.verify(
      deleteRequestedFor(urlEqualTo("/api/appointments/$appointmentId"))
        .withHeader("no-event-propagation", equalTo("false")),
    )
  }

  @Test
  fun `update appointment comment - main flow`() {
    val appointmentId = 100L
    val comment = "New comment"

    prisonApiMockServer.stubUpdateAppointmentComment(appointmentId)
    prisonApiService.updateAppointmentComment(appointmentId, UpdateComment(comment), ALLOW)
    prisonApiMockServer.verify(
      putRequestedFor(urlEqualTo("/api/appointments/$appointmentId/comment"))
        .withRequestBody(equalTo("""{"comment":"$comment"}"""))
        .withHeader("no-event-propagation", equalTo("false")),
    )
  }

  @Test
  fun `update appointment comment - empty comment`() {
    val appointmentId = 100L
    val comment = ""

    prisonApiMockServer.stubUpdateAppointmentComment(appointmentId)
    prisonApiService.updateAppointmentComment(appointmentId, UpdateComment(comment), ALLOW)
    prisonApiMockServer.verify(
      putRequestedFor(urlEqualTo("/api/appointments/$appointmentId/comment"))
        .withRequestBody(equalTo("""{"comment":""}"""))
        .withHeader("no-event-propagation", equalTo("false")),
    )
  }

  @Test
  fun `update appointment comment - null comment`() {
    val appointmentId = 100L
    val comment = null

    prisonApiMockServer.stubUpdateAppointmentComment(appointmentId)
    prisonApiService.updateAppointmentComment(appointmentId, UpdateComment(comment), ALLOW)
    prisonApiMockServer.verify(
      putRequestedFor(urlEqualTo("/api/appointments/$appointmentId/comment"))
        .withHeader("no-event-propagation", equalTo("false"))
        .withRequestBody(equalTo("""{"comment":null}""")),
    )
  }

  val pageable = Pageable.ofSize(10000)

  @Test
  fun `get Attendance for offender`() {
    val offenderNo = "A1234AC"
    val fromDate = LocalDate.of(2021, 5, 31)
    val toDate = LocalDate.of(2021, 6, 30)

    prisonApiMockServer.stubGetAttendanceForOffender(offenderNo, fromDate, toDate)

    val result = prisonApiService.getAttendanceForOffender(offenderNo, fromDate, toDate, null, pageable)

    assertThat(result.content)
      .containsExactly(
        OffenderAttendance(
          eventDate = "2021-05-04",
          outcome = "ATT",
          prisonId = "MDI",
          activity = "act 1",
          description = "desc 1",
          comment = "comment 1",
        ),
        OffenderAttendance(
          eventDate = "2021-05-04",
          outcome = "ACCAB",
          prisonId = "MDI",
          activity = "act 2",
          description = "desc 2",
          comment = "comment 2",
        ),
        OffenderAttendance(
          eventDate = "2021-06-04",
          outcome = "UNACAB",
          prisonId = "MDI",
          activity = null,
          description = "desc 3",
          comment = "comment 3",
        ),
        OffenderAttendance(
          eventDate = "2021-06-05",
          outcome = "ATT",
          prisonId = "WWI",
          activity = "act 4",
          description = "desc 4",
          comment = null,
        ),
        OffenderAttendance(
          eventDate = "2021-07-14",
          outcome = "UNACAB",
          prisonId = "WWI",
          activity = "act 5",
          description = "desc 5",
          comment = "comment 5",
        ),
        OffenderAttendance(
          eventDate = "2021-08-14",
          outcome = null,
          prisonId = "WWI",
          activity = "act 6",
          description = "desc 6",
          comment = "comment 6",
        ),
      )
  }
}

private fun getLocation() =
  listOf(
    Location(
      locationId = 1L, locationType = "VIDE", description = "A VLB location",
      locationUsage = null, agencyId = "WWI", parentLocationId = null,
      currentOccupancy = 0, locationPrefix = "XXX", operationalCapacity = 10,
      userDescription = null, internalLocationCode = "",
    ),
  )
