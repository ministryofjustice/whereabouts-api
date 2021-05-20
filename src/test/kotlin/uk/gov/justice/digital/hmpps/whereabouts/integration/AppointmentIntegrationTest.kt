package uk.gov.justice.digital.hmpps.whereabouts.integration

import com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyLong
import org.springframework.boot.test.mock.mockito.MockBean
import uk.gov.justice.digital.hmpps.whereabouts.model.MainRecurringAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.RecurringAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.RepeatPeriod
import uk.gov.justice.digital.hmpps.whereabouts.repository.RecurringAppointmentRepository
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingRepository
import uk.gov.justice.digital.hmpps.whereabouts.utils.makePrisonAppointment
import uk.gov.justice.digital.hmpps.whereabouts.utils.makeVideoLinkBooking
import java.time.LocalDateTime
import java.util.Optional

class AppointmentIntegrationTest : IntegrationTest() {

  @MockBean
  lateinit var videoLinkBookingRepository: VideoLinkBookingRepository

  @MockBean
  lateinit var recurringAppointmentRepository: RecurringAppointmentRepository

  @BeforeEach
  fun beforeEach() {
    prisonApiMockServer.resetAll()
  }

  @Nested
  inner class RequestAppointmentDetails {

    @BeforeEach
    fun beforeEach() {
      prisonApiMockServer.stubGetPrisonAppointment(
        APPOINTMENT_ID,
        objectMapper.writeValueAsString(
          makePrisonAppointment(
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
        .jsonPath("$.appointment.id").isEqualTo(APPOINTMENT_ID)
        .jsonPath("$.appointment.agencyId").isEqualTo("MDI")
        .jsonPath("$.appointment.locationId").isEqualTo(2)
        .jsonPath("$.appointment.appointmentTypeCode").isEqualTo("INST")
        .jsonPath("$.appointment.offenderNo").isEqualTo(OFFENDER_NO)
        .jsonPath("$.appointment.startTime").isEqualTo(START_TIME.toString())
        .jsonPath("$.appointment.endTime").isEqualTo(END_TIME.toString())
    }

    @Test
    fun `should return video link booking details`() {
      whenever(videoLinkBookingRepository.findByMainAppointmentIds(any()))
        .thenReturn(listOf(makeVideoLinkBooking(id = 1)))

      webTestClient.get()
        .uri("/appointment/$APPOINTMENT_ID")
        .headers(setHeaders())
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.videoLinkBooking.id").isEqualTo(1)
        .jsonPath("$.videoLinkBooking.main.id").isEqualTo(1)
        .jsonPath("$.videoLinkBooking.main.bookingId").isEqualTo(BOOKING_ID)
        .jsonPath("$.videoLinkBooking.main.appointmentId").isEqualTo(1)
        .jsonPath("$.videoLinkBooking.main.court").isEqualTo("Court 1")
        .jsonPath("$.videoLinkBooking.main.hearingType").isEqualTo("MAIN")
        .jsonPath("$.videoLinkBooking.main.createdByUsername").isEqualTo("SA")
        .jsonPath("$.videoLinkBooking.main.madeByTheCourt").isEqualTo(true)
        .jsonPath("$.videoLinkBooking.pre.id").isEqualTo(2)
        .jsonPath("$.videoLinkBooking.pre.bookingId").isEqualTo(BOOKING_ID)
        .jsonPath("$.videoLinkBooking.pre.appointmentId").isEqualTo(2)
        .jsonPath("$.videoLinkBooking.pre.court").isEqualTo("Court 1")
        .jsonPath("$.videoLinkBooking.pre.hearingType").isEqualTo("PRE")
        .jsonPath("$.videoLinkBooking.pre.createdByUsername").isEqualTo("SA")
        .jsonPath("$.videoLinkBooking.pre.madeByTheCourt").isEqualTo(true)
        .jsonPath("$.videoLinkBooking.post.id").isEqualTo(3)
        .jsonPath("$.videoLinkBooking.post.bookingId").isEqualTo(BOOKING_ID)
        .jsonPath("$.videoLinkBooking.post.appointmentId").isEqualTo(3)
        .jsonPath("$.videoLinkBooking.post.court").isEqualTo("Court 1")
        .jsonPath("$.videoLinkBooking.post.hearingType").isEqualTo("POST")
        .jsonPath("$.videoLinkBooking.post.createdByUsername").isEqualTo("SA")
        .jsonPath("$.videoLinkBooking.post.madeByTheCourt").isEqualTo(true)
    }

    @Test
    fun `should return recurring appointment information`() {
      whenever(recurringAppointmentRepository.findById(anyLong())).thenReturn(
        Optional.of(
          MainRecurringAppointment(
            id = 1,
            repeatPeriod = RepeatPeriod.Fortnightly,
            count = 10,
            recurringAppointments = listOf(RecurringAppointment(1))
          )
        )
      )

      webTestClient.get()
        .uri("/appointment/$APPOINTMENT_ID")
        .headers(setHeaders())
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.recurring.id").isEqualTo(1)
        .jsonPath("$.recurring.repeatPeriod").isEqualTo(RepeatPeriod.Fortnightly.toString())
        .jsonPath("$.recurring.count").isEqualTo(10)
    }
  }

  @Nested
  inner class CreateAnAppointment {
    @BeforeEach
    fun beforeEach() {
      prisonApiMockServer.stubAddAppointment(
        mapOf(
          "appointmentEventId" to 1,
          "bookingId" to 2,
          "startTime" to START_TIME,
          "endTime" to END_TIME,
          "recurringAppointmentEventIds" to setOf(1, 2, 3)
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
          .withRequestBody(matchingJsonPath("$[?(@.locationId == 1)]"))
          .withRequestBody(matchingJsonPath("$[?(@.startTime == '2020-10-10T20:00:01')]"))
          .withRequestBody(matchingJsonPath("$[?(@.endTime == '2020-10-10T21:00:02')]"))
          .withRequestBody(matchingJsonPath("$[?(@.comment == 'test')]"))
          .withRequestBody(matchingJsonPath("$[?(@.appointmentType == 'ABC')]"))
      )
    }

    @Test
    fun `should create a recurring appointment`() {
      webTestClient.post()
        .uri("/appointment")
        .bodyValue(
          makeCreateAppointmentMap(
            startTime = START_TIME,
            endTime = END_TIME,
            repeatPeriod = RepeatPeriod.Daily,
            count = 1
          )
        )
        .headers(setHeaders())
        .exchange()
        .expectStatus().isCreated

      prisonApiMockServer.verify(
        postRequestedFor(urlEqualTo("/api/appointments"))
          .withRequestBody(matchingJsonPath("$[?(@.repeat.repeatPeriod == 'Daily')]"))
          .withRequestBody(matchingJsonPath("$[?(@.repeat.count == '1')]"))
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

  companion object {
    private const val APPOINTMENT_ID = 2L
    private const val BOOKING_ID = -1L
    private const val OFFENDER_NO = "A12345"
    private val START_TIME = LocalDateTime.parse("2020-10-10T20:00:01")
    private val END_TIME = LocalDateTime.parse("2020-10-10T21:00:02")
  }
}
