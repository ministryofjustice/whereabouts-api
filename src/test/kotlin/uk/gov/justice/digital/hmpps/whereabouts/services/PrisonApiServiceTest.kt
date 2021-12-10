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
import org.springframework.data.domain.Pageable
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException.InternalServerError
import uk.gov.justice.digital.hmpps.whereabouts.dto.prisonapi.OffenderAttendance
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
    val webClient = WebClient.create("http://localhost:${prisonApiMockServer.port()}/api")
    prisonApiService = PrisonApiService(webClient)
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
    val scheduledAppointments = prisonApiService.getScheduledAppointments(agencyId, date)
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
    val agencyId = "WWI"
    val locationType = "APP"

    prisonApiMockServer.stubGetAgencyLocationsForTypeUnrestricted(agencyId, locationType, getLocation())
    val locations = prisonApiService.getAgencyLocationsForTypeUnrestricted(agencyId, locationType)
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
          comment = "comment 1"
        ),
        OffenderAttendance(
          eventDate = "2021-05-04",
          outcome = "ACCAB",
          prisonId = "MDI",
          activity = "act 2",
          description = "desc 2",
          comment = "comment 2"
        ),
        OffenderAttendance(
          eventDate = "2021-06-04",
          outcome = "UNACAB",
          prisonId = "MDI",
          activity = null,
          description = "desc 3",
          comment = "comment 3"
        ),
        OffenderAttendance(
          eventDate = "2021-06-05",
          outcome = "ATT",
          prisonId = "WWI",
          activity = "act 4",
          description = "desc 4",
          comment = null
        ),
        OffenderAttendance(
          eventDate = "2021-07-14",
          outcome = "UNACAB",
          prisonId = "WWI",
          activity = "act 5",
          description = "desc 5",
          comment = "comment 5"
        ),
        OffenderAttendance(
          eventDate = "2021-08-14",
          outcome = null,
          prisonId = "WWI",
          activity = "act 6",
          description = "desc 6",
          comment = "comment 6"
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
      userDescription = null, internalLocationCode = ""
    )
  )
