package uk.gov.justice.digital.hmpps.whereabouts.integration

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.mock.mockito.MockBean
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingRepository
import uk.gov.justice.digital.hmpps.whereabouts.utils.makePrisonAppointment
import uk.gov.justice.digital.hmpps.whereabouts.utils.makeVideoLinkBooking
import java.time.LocalDateTime

class AppointmentIntegrationTest : IntegrationTest() {

  @MockBean
  lateinit var videoLinkBookingRepository: VideoLinkBookingRepository

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
  }

  companion object {
    private const val APPOINTMENT_ID = 2L
    private const val BOOKING_ID = -1L
    private const val OFFENDER_NO = "A12345"
    private val START_TIME = LocalDateTime.now()
    private val END_TIME = LocalDateTime.now()
  }
}
