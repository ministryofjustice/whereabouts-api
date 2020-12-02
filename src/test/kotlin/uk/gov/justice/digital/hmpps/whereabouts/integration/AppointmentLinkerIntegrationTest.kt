package uk.gov.justice.digital.hmpps.whereabouts.integration

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.test.context.transaction.TestTransaction
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType
import uk.gov.justice.digital.hmpps.whereabouts.model.PrisonAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkAppointment
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkAppointmentRepository
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingRepository
import java.time.LocalDateTime
import javax.transaction.Transactional

@Transactional
class AppointmentLinkerIntegrationTest : IntegrationTest() {

  @Value("\${appointmentLinkerToken}")
  private val appointmentLinkerToken: String? = null

  @Autowired
  lateinit var videoLinkAppointmentRepository: VideoLinkAppointmentRepository

  @Autowired
  lateinit var videoLinkBookingRepository: VideoLinkBookingRepository

  @Autowired
  lateinit var objectMapper: ObjectMapper

  @Test
  fun `unauthorised client gets 403 Forbidden `() {
    webTestClient.post()
      .uri("/court/appointment-linker")
      .headers(setHeaders())
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `should link dangling appointments`() {
    videoLinkBookingRepository.deleteAll()
    videoLinkAppointmentRepository.deleteAll()

    assertThat(videoLinkBookingRepository.count()).isEqualTo(0)

    videoLinkAppointmentRepository.saveAll(videoLinkAppointments(HearingType.PRE, 1, 0, 3))
    videoLinkAppointmentRepository.saveAll(videoLinkAppointments(HearingType.MAIN, 1, 1, 4))
    videoLinkAppointmentRepository.saveAll(videoLinkAppointments(HearingType.POST, 1, 2, 5))

    videoLinkAppointmentRepository.saveAll(videoLinkAppointments(HearingType.PRE, 2, 100))
    videoLinkAppointmentRepository.saveAll(videoLinkAppointments(HearingType.MAIN, 2, 101))
    videoLinkAppointmentRepository.saveAll(videoLinkAppointments(HearingType.POST, 2, 102))

    videoLinkAppointmentRepository.saveAll(videoLinkAppointments(HearingType.MAIN, 3, 201))

    TestTransaction.flagForCommit()
    TestTransaction.end()

    val t0 = LocalDateTime.of(2020, 10, 10, 9, 0)

    stubPrisonAppointment(1, 0, t0, 30)
    stubPrisonAppointment(1, 1, t0.plusMinutes(30), 30)
    stubPrisonAppointment(1, 2, t0.plusMinutes(60), 30)

    stubPrisonAppointment(1, 3, t0.plusMinutes(90), 30)
    stubPrisonAppointment(1, 4, t0.plusMinutes(120), 30)
    stubPrisonAppointment(1, 5, t0.plusMinutes(150), 30)

    stubPrisonAppointment(2, 100, t0.plusMinutes(15), 30)
    stubPrisonAppointment(2, 101, t0.plusMinutes(45), 30)
    stubPrisonAppointment(2, 102, t0.plusMinutes(75), 30)

    // Make sure that PrisonApiServer handles 404 correctly.
    prisonApiMockServer.stubGetPrisonAppointmentNotFound(201)

    webTestClient.post()
      .uri("/court/appointment-linker")
      .headers {
        it.setBearerAuth(appointmentLinkerToken!!)
        it.setContentType(MediaType.APPLICATION_JSON)
      }
      .exchange()
      .expectStatus().isNoContent

    TestTransaction.start()

    assertThat(videoLinkBookingRepository.findAll())
      .hasSize(3)

    TestTransaction.end()
  }

  private fun videoLinkAppointments(
    hearingType: HearingType,
    bookingId: Long,
    vararg appointmentIds: Long
  ): List<VideoLinkAppointment> =
    appointmentIds.map { appointmentId ->
      VideoLinkAppointment(
        bookingId = bookingId,
        appointmentId = appointmentId,
        hearingType = hearingType,
        madeByTheCourt = true,
        court = "The Court"
      )
    }

  private fun stubPrisonAppointment(
    bookingId: Long,
    eventId: Long,
    startTime: LocalDateTime,
    endTimeOffsetMinutes: Long
  ) {
    prisonApiMockServer.stubGetPrisonAppointment(
      eventId,
      objectMapper.writeValueAsString(
        PrisonAppointment(
          bookingId = bookingId,
          eventId = eventId,
          startTime = startTime,
          endTime = startTime.plusMinutes(endTimeOffsetMinutes),
          eventSubType = "VLB",
          agencyId = "WWI",
          eventLocationId = 1,
        )
      )
    )
  }
}
