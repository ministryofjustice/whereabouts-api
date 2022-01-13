package uk.gov.justice.digital.hmpps.whereabouts.integration

import com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.isA
import org.mockito.kotlin.isNull
import org.mockito.kotlin.whenever
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.domain.Sort
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType
import uk.gov.justice.digital.hmpps.whereabouts.model.RecurringAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.RelatedAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.RepeatPeriod
import uk.gov.justice.digital.hmpps.whereabouts.repository.CourtRepository
import uk.gov.justice.digital.hmpps.whereabouts.repository.RecurringAppointmentRepository
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingRepository
import uk.gov.justice.digital.hmpps.whereabouts.utils.DataHelpers
import java.time.Duration
import java.time.LocalDateTime
import java.util.Optional

class AppointmentIntegrationTest : IntegrationTest() {
  @MockBean
  lateinit var courtRepository: CourtRepository

  @MockBean
  lateinit var videoLinkBookingRepository: VideoLinkBookingRepository

  @MockBean
  lateinit var recurringAppointmentRepository: RecurringAppointmentRepository

  @BeforeEach
  fun beforeEach() {
    prisonApiMockServer.resetAll()

    whenever(courtRepository.findAll(isA<Sort>())).thenReturn(listOf())
  }

  @Nested
  inner class RequestAppointmentDetails {

    @BeforeEach
    fun beforeEach() {
      prisonApiMockServer.stubGetPrisonAppointment(
        APPOINTMENT_ID,
        objectMapper.writeValueAsString(
          DataHelpers.makeCreatePrisonAppointment(
            appointmentId = APPOINTMENT_ID,
            startTime = START_TIME,
            endTime = END_TIME
          )
        )
      )

      prisonApiMockServer.stubGetBooking(bookingId = BOOKING_ID, offenderNo = OFFENDER_NO)
    }

    @Test
    fun `should return appointment details`() {
      webTestClient.get()
        .uri("/appointment/$APPOINTMENT_ID")
        .headers(setHeaders())
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json(loadJsonFile("appointment-details.json"))
    }

    @Test
    fun `should return video link booking details`() {
      whenever(videoLinkBookingRepository.findByAppointmentIdsAndHearingType(any(), eq(HearingType.MAIN), isNull(), isNull()))
        .thenReturn(listOf(DataHelpers.makeVideoLinkBooking(id = 1)))

      webTestClient.mutate().responseTimeout(Duration.ofSeconds(10)).build().get()
        .uri("/appointment/$APPOINTMENT_ID")
        .headers(setHeaders())
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json(loadJsonFile("appointment-details-video-link.json"))
    }

    @Test
    fun `should return recurring appointment information`() {
      whenever(recurringAppointmentRepository.findRecurringAppointmentByRelatedAppointmentsContains(any())).thenReturn(
        Optional.of(
          RecurringAppointment(
            id = 1,
            repeatPeriod = RepeatPeriod.FORTNIGHTLY,
            count = 10,
            startTime = LocalDateTime.of(2021, 1, 21, 0, 0, 0),
            relatedAppointments = mutableListOf(RelatedAppointment(1))
          )
        )
      )

      webTestClient.get()
        .uri("/appointment/$APPOINTMENT_ID")
        .headers(setHeaders())
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json(loadJsonFile("appointment-details-recurring.json"))
    }
  }

  @Nested
  inner class CreateAnAppointment {
    @BeforeEach
    fun beforeEach() {
      prisonApiMockServer.stubAddAppointment(
        listOf(
          mapOf(
            "appointmentEventId" to 1,
            "appointmentType" to "INST",
            "locationId" to 1,
            "bookingId" to 2,
            "startTime" to START_TIME,
            "endTime" to END_TIME,
          ),
          mapOf(
            "appointmentEventId" to 2,
            "appointmentType" to "INST",
            "locationId" to 1,
            "bookingId" to 2,
            "startTime" to START_TIME,
            "endTime" to END_TIME,
          ),
          mapOf(
            "appointmentEventId" to 3,
            "appointmentType" to "INST",
            "locationId" to 1,
            "bookingId" to 2,
            "startTime" to START_TIME,
            "endTime" to END_TIME,
          )
        )
      )
    }

    @Test
    fun `should create an appointment`() {
      webTestClient.post()
        .uri("/appointment")
        .bodyValue(makeCreateAppointmentMap(startTime = START_TIME, endTime = END_TIME))
        .headers(setHeaders())
        .exchange()
        .expectStatus().isCreated

      prisonApiMockServer.verify(
        postRequestedFor(urlEqualTo("/api/appointments"))
          .withRequestBody(equalToJson(loadJsonFile("create-prison-appointment-no-repeat.json")))
      )

      oauthMockServer.verify(0, postRequestedFor(urlEqualTo("/auth/oauth/token")))
    }

    @Test
    fun `should create a recurring appointment`() {
      webTestClient.mutate().responseTimeout(Duration.ofSeconds(10)).build().post()
        .uri("/appointment")
        .bodyValue(
          makeCreateAppointmentMap(
            startTime = START_TIME,
            endTime = END_TIME,
            repeatPeriod = RepeatPeriod.DAILY,
            count = 1
          )
        )
        .headers(setHeaders())
        .exchange()
        .expectStatus().isCreated
        .expectBody()
        .json(loadJsonFile("appointments-created-response.json"))

      prisonApiMockServer.verify(
        postRequestedFor(urlEqualTo("/api/appointments"))
          .withRequestBody(equalToJson(loadJsonFile("create-prison-appointment-with-repeat.json")))
      )
    }

    private fun makeCreateAppointmentMap(
      locationId: Long = 1,
      startTime: LocalDateTime = LocalDateTime.now(),
      endTime: LocalDateTime = LocalDateTime.now(),
      bookingId: Long = -1L,
      comment: String = "test",
      appointmentType: String = "ABC",
      repeatPeriod: RepeatPeriod? = null,
      count: Long? = null
    ): Map<String, Any> {

      val appointmentMap = mutableMapOf<String, Any>(
        "locationId" to locationId,
        "startTime" to startTime,
        "endTime" to endTime,
        "bookingId" to bookingId,
        "comment" to comment,
        "appointmentType" to appointmentType,
      )

      repeatPeriod?.let { appointmentMap.set("repeat", mapOf("repeatPeriod" to repeatPeriod, "count" to count)) }

      return appointmentMap
    }
  }

  @Nested
  inner class DeleteAppointment {
    @Test
    fun `should delete an appointment`() {
      prisonApiMockServer.stubGetPrisonAppointment(
        APPOINTMENT_ID,
        objectMapper.writeValueAsString(DataHelpers.makePrisonAppointment(eventId = APPOINTMENT_ID))
      )
      prisonApiMockServer.stubDeleteAppointment(APPOINTMENT_ID, 200)

      webTestClient.delete()
        .uri("/appointment/$APPOINTMENT_ID")
        .headers(setHeaders())
        .exchange()
        .expectStatus().isOk

      prisonApiMockServer.verify(
        deleteRequestedFor(urlEqualTo("/api/appointments/$APPOINTMENT_ID"))
      )
    }

    @Test
    fun `should delete a single appointment in the recurring list`() {
      prisonApiMockServer.stubGetPrisonAppointment(
        1,
        objectMapper.writeValueAsString(DataHelpers.makePrisonAppointment(eventId = 1))
      )
      whenever(recurringAppointmentRepository.findRecurringAppointmentByRelatedAppointmentsContains(any())).thenReturn(
        Optional.of(
          RecurringAppointment(
            1, relatedAppointments = mutableListOf(RelatedAppointment(1), RelatedAppointment(2)),
            repeatPeriod = RepeatPeriod.DAILY,
            count = 1,
            startTime = LocalDateTime.of(2021, 1, 21, 0, 0, 0)
          )
        )
      )

      prisonApiMockServer.stubDeleteAppointments(listOf(1))

      webTestClient.delete()
        .uri("/appointment/1")
        .headers(setHeaders())
        .exchange()
        .expectStatus().isOk
    }
  }

  @Nested
  inner class DeleteRecurringAppointmentSequence {
    @Test
    fun `should delete all recurring appointments`() {
      whenever(recurringAppointmentRepository.findById(any())).thenReturn(
        Optional.of(
          RecurringAppointment(
            100, relatedAppointments = mutableListOf(RelatedAppointment(1), RelatedAppointment(2)),
            repeatPeriod = RepeatPeriod.DAILY,
            count = 1,
            startTime = LocalDateTime.of(2021, 1, 21, 0, 0, 0)
          )
        )
      )

      prisonApiMockServer.stubDeleteAppointments(listOf(1, 2))

      webTestClient.delete()
        .uri("/appointment/recurring/100")
        .headers(setHeaders())
        .exchange()
        .expectStatus().isOk
    }
  }

  companion object {
    private const val APPOINTMENT_ID = 2L
    private const val BOOKING_ID = -1L
    private const val OFFENDER_NO = "A12345"
    private val START_TIME = LocalDateTime.parse("2020-10-10T20:00:01")
    private val END_TIME = LocalDateTime.parse("2020-10-10T21:00:02")
  }
}
