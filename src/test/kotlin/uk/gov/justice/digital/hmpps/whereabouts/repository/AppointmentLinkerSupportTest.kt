package uk.gov.justice.digital.hmpps.whereabouts.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBooking
import uk.gov.justice.digital.hmpps.whereabouts.security.AuthenticationFacade

@ActiveProfiles("test")
@Import(TestAuditConfiguration::class)
@DataJpaTest
@Transactional
class AppointmentLinkerSupportTest {
  @Autowired
  lateinit var videoLinkAppointmentRepository: VideoLinkAppointmentRepository

  @Autowired
  lateinit var videoLinkBookingRepository: VideoLinkBookingRepository

  @MockBean
  lateinit var authenticationFacade: AuthenticationFacade

  @BeforeEach
  fun setup() {
    clearRepositories()
    insertTestData()
  }

  @AfterEach
  fun tearDown() {
    clearRepositories()
  }

  private fun clearRepositories() {
    videoLinkBookingRepository.deleteAll()
    videoLinkAppointmentRepository.deleteAll()
  }

  /**
   * Creates appointments for 5 booking ids.
   * bookingId 1 has one linked and one unlinked MAIN appointment.
   * bookingId 2 has one linked MAIN, PRE and POST appointment, two unlinked PRE and two unlinked POST appointment.
   * bookingId 3 has two unlinked MAIN appointments. bookingId 3 should be in the result.
   * bookingId 4 has one unlinked PRE appointment. bookingId 4 should not be in the result.
   * bookingId 5 has one unlinked POST appointment. bookingId 4 should not be in the result.
   */

  private fun insertTestData() {
    var appointmentIdCounter = 100L

    videoLinkBookingRepository.save(
      VideoLinkBooking(
        main = VideoLinkAppointment(
          bookingId = 1L,
          appointmentId = appointmentIdCounter++,
          court = "Wimbledon",
          hearingType = HearingType.MAIN,
          madeByTheCourt = true
        ),
      )
    )

    videoLinkBookingRepository.save(
      VideoLinkBooking(
        main = VideoLinkAppointment(
          bookingId = 2L,
          appointmentId = appointmentIdCounter++,
          court = "Wimbledon",
          hearingType = HearingType.MAIN,
          madeByTheCourt = true
        ),
        pre = VideoLinkAppointment(
          bookingId = 2L,
          appointmentId = appointmentIdCounter++,
          court = "Wimbledon",
          hearingType = HearingType.PRE,
          madeByTheCourt = true
        ),
        post = VideoLinkAppointment(
          bookingId = 2L,
          appointmentId = appointmentIdCounter++,
          court = "Wimbledon",
          hearingType = HearingType.POST,
          madeByTheCourt = true
        )
      )
    )

    videoLinkAppointmentRepository.save(
      VideoLinkAppointment(
        bookingId = 1L,
        appointmentId = appointmentIdCounter++,
        court = "Wimbledon",
        hearingType = HearingType.MAIN,
        madeByTheCourt = false
      )
    )

    videoLinkAppointmentRepository.save(
      VideoLinkAppointment(
        bookingId = 2L,
        appointmentId = appointmentIdCounter++,
        court = "Wimbledon",
        hearingType = HearingType.PRE,
        madeByTheCourt = false
      )
    )

    videoLinkAppointmentRepository.save(
      VideoLinkAppointment(
        bookingId = 2L,
        appointmentId = appointmentIdCounter++,
        court = "Wimbledon",
        hearingType = HearingType.PRE,
        madeByTheCourt = false
      )
    )

    videoLinkAppointmentRepository.save(
      VideoLinkAppointment(
        bookingId = 2L,
        appointmentId = appointmentIdCounter++,
        court = "Wimbledon",
        hearingType = HearingType.POST,
        madeByTheCourt = false
      )
    )

    videoLinkAppointmentRepository.save(
      VideoLinkAppointment(
        bookingId = 2L,
        appointmentId = appointmentIdCounter++,
        court = "Wimbledon",
        hearingType = HearingType.POST,
        madeByTheCourt = false
      )
    )

    videoLinkAppointmentRepository.save(
      VideoLinkAppointment(
        bookingId = 3L,
        appointmentId = appointmentIdCounter++,
        court = "Wimbledon",
        hearingType = HearingType.MAIN,
        madeByTheCourt = false
      )
    )

    videoLinkAppointmentRepository.save(
      VideoLinkAppointment(
        bookingId = 3L,
        appointmentId = appointmentIdCounter++,
        court = "Wimbledon",
        hearingType = HearingType.MAIN,
        madeByTheCourt = false
      )
    )

    videoLinkAppointmentRepository.save(
      VideoLinkAppointment(
        bookingId = 4L,
        appointmentId = appointmentIdCounter++,
        court = "Wimbledon",
        hearingType = HearingType.PRE,
        madeByTheCourt = false
      )
    )

    videoLinkAppointmentRepository.save(
      VideoLinkAppointment(
        bookingId = 5L,
        appointmentId = appointmentIdCounter,
        court = "Wimbledon",
        hearingType = HearingType.POST,
        madeByTheCourt = false
      )
    )
  }

  @Test
  fun `should find bookingIds of unlinked appointments`() {
    assertThat(
      videoLinkAppointmentRepository.bookingIdsOfUnlinkedAppointments()
    )
      .containsExactlyInAnyOrder(1L, 3L)
  }

  @Test
  fun `Retrieving unlinked MAIN appointments by booking id`() {
    assertThat(videoLinkAppointmentRepository.unlinkedMainAppointmentsForBookingId(1)).hasSize(1)
    assertThat(videoLinkAppointmentRepository.unlinkedMainAppointmentsForBookingId(2)).hasSize(0)
    assertThat(videoLinkAppointmentRepository.unlinkedMainAppointmentsForBookingId(3)).hasSize(2)
    assertThat(videoLinkAppointmentRepository.unlinkedMainAppointmentsForBookingId(4)).hasSize(0)
    assertThat(videoLinkAppointmentRepository.unlinkedMainAppointmentsForBookingId(5)).hasSize(0)
  }

  @Test
  fun `Retrieving unlinked PRE appointments by booking id`() {
    assertThat(videoLinkAppointmentRepository.unlinkedPreAppointmentsForBookingId(1)).hasSize(0)
    assertThat(videoLinkAppointmentRepository.unlinkedPreAppointmentsForBookingId(2)).hasSize(2)
    assertThat(videoLinkAppointmentRepository.unlinkedPreAppointmentsForBookingId(3)).hasSize(0)
    assertThat(videoLinkAppointmentRepository.unlinkedPreAppointmentsForBookingId(4)).hasSize(1)
    assertThat(videoLinkAppointmentRepository.unlinkedPreAppointmentsForBookingId(5)).hasSize(0)
  }

  @Test
  fun `Retrieving unlinked POST appointments by booking id`() {
    assertThat(videoLinkAppointmentRepository.unlinkedPostAppointmentsForBookingId(1)).hasSize(0)
    assertThat(videoLinkAppointmentRepository.unlinkedPostAppointmentsForBookingId(2)).hasSize(2)
    assertThat(videoLinkAppointmentRepository.unlinkedPostAppointmentsForBookingId(3)).hasSize(0)
    assertThat(videoLinkAppointmentRepository.unlinkedPostAppointmentsForBookingId(4)).hasSize(0)
    assertThat(videoLinkAppointmentRepository.unlinkedPostAppointmentsForBookingId(5)).hasSize(1)
  }
}
