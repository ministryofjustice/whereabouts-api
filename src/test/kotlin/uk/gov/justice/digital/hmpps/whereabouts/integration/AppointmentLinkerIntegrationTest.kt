package uk.gov.justice.digital.hmpps.whereabouts.integration

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
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

  @Autowired
  lateinit var videoLinkAppointmentRepository: VideoLinkAppointmentRepository

  @Autowired
  lateinit var videoLinkBookingRepository: VideoLinkBookingRepository

  @Autowired
  lateinit var objectMapper: ObjectMapper

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

    TestTransaction.flagForCommit()
    TestTransaction.end()

    prisonApiMockServer.stubGetPrisonAppointmentsForBookingId(
      bookingId = 1,
      offset = 0,
      responseJson = objectMapper.writeValueAsString(
        prisonAppointments(1, LocalDateTime.of(2020, 11, 1, 9, 0), 0, 1, 2, 3, 4, 5)
      )
    )

    prisonApiMockServer.stubGetPrisonAppointmentsForBookingId(bookingId = 1, offset = 6, responseJson = "[]")

    prisonApiMockServer.stubGetPrisonAppointmentsForBookingId(
      bookingId = 2,
      offset = 0,
      responseJson = objectMapper.writeValueAsString(
        prisonAppointments(2, LocalDateTime.of(2020, 11, 7, 9, 0), 101, 102, 103)
      )
    )

    prisonApiMockServer.stubGetPrisonAppointmentsForBookingId(bookingId = 2, offset = 3, responseJson = "[]")

    webTestClient.post()
      .uri("/court/appointment-linker")
      .headers(setHeaders())
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

  private fun prisonAppointments(
    bookingId: Long,
    startTime: LocalDateTime,
    vararg eventIds: Long
  ): List<PrisonAppointment> {
    var time = startTime
    return eventIds.map { eventId ->
      PrisonAppointment(
        eventId = eventId,
        agencyId = "WWI",
        bookingId = bookingId,
        startTime = time,
        endTime = time.plusHours(1),
        eventLocationId = 1L,
        eventSubType = "VLB"
      ).also { time = time.plusHours(1) }
    }
  }
}
