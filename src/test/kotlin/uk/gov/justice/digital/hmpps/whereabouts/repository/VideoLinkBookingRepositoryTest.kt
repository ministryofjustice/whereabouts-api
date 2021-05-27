package uk.gov.justice.digital.hmpps.whereabouts.repository

import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.transaction.TestTransaction
import org.springframework.test.jdbc.JdbcTestUtils
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.whereabouts.config.AuditConfiguration
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType.MAIN
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType.POST
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType.PRE
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBooking
import uk.gov.justice.digital.hmpps.whereabouts.security.AuthenticationFacade

const val USERNAME = "username1"

@ActiveProfiles("test")
@Import(AuditConfiguration::class)
@DataJpaTest
@Transactional
class VideoLinkBookingRepositoryTest(
  @Autowired val repository: VideoLinkBookingRepository,
  @Autowired val jdbcTemplate: JdbcTemplate
) {
  @MockBean
  lateinit var authenticationFacade: AuthenticationFacade

  @BeforeEach
  fun deleteAll() {
    JdbcTestUtils.deleteFromTables(jdbcTemplate, "VIDEO_LINK_APPOINTMENT", "VIDEO_LINK_BOOKING")
    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()
  }

  @BeforeEach
  fun mockCurrentUsername() {
    whenever(authenticationFacade.currentUsername).thenReturn(USERNAME)
  }

  @Test
  fun `should persist a booking (main only)`() {

    val transientBooking = VideoLinkBooking(
      offenderBookingId = 1,
      courtName = "A Court",
      courtId = "TSTCRT",
      madeByTheCourt = true
    ).apply {
      addMainAppointment(appointmentId = 2)
    }

    val id = repository.save(transientBooking).id!!
    assertThat(id).isNotNull

    TestTransaction.flagForCommit()
    TestTransaction.end()

    val persistentBooking = repository.getOne(id)

    assertThat(persistentBooking)
      .usingRecursiveComparison()
      .ignoringFields("id", "mainAppointment")
      .isEqualTo(transientBooking)

    assertThat(persistentBooking.createdByUsername).isEqualTo(USERNAME)
  }

  @Test
  fun `should persist a booking (main, pre and post)`() {

    val transientBooking = VideoLinkBooking(
      offenderBookingId = 1,
      courtName = "A Court",
      madeByTheCourt = true,
    ).apply {
      addMainAppointment(appointmentId = 4)
      addPreAppointment(appointmentId = 12)
      addPostAppointment(appointmentId = 22)
    }

    val id = repository.save(transientBooking).id!!
    assertThat(id).isNotNull

    TestTransaction.flagForCommit()
    TestTransaction.end()

    val persistentBooking = repository.getOne(id)

    assertThat(persistentBooking)
      .usingRecursiveComparison()
      .ignoringFields("id")
      .isEqualTo(transientBooking)

    val hearingTypes = jdbcTemplate.queryForList("select hearing_type from video_link_appointment", String::class.java)
    assertThat(hearingTypes).contains("PRE", "MAIN", "POST")

    assertThat(persistentBooking.createdByUsername).isEqualTo(USERNAME)
  }

  @Test
  fun `Deleting a booking by id should delete its appointments`() {

    assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "VIDEO_LINK_BOOKING")).isEqualTo(0)
    assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "VIDEO_LINK_APPOINTMENT")).isEqualTo(0)

    val id = repository.save(
      VideoLinkBooking(offenderBookingId = 1, courtName = "A Court", madeByTheCourt = true).apply {
        addMainAppointment(appointmentId = 4)
        addPreAppointment(appointmentId = 12)
        addPostAppointment(appointmentId = 22)
      }
    ).id!!
    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()
    assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "VIDEO_LINK_BOOKING")).isEqualTo(1)
    assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "VIDEO_LINK_APPOINTMENT")).isEqualTo(3)

    repository.deleteById(id)

    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()
    assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "VIDEO_LINK_BOOKING")).isEqualTo(0)
    assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "VIDEO_LINK_APPOINTMENT")).isEqualTo(0)
  }

  @Test
  fun `Deleting a booking should delete its appointments`() {

    assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "VIDEO_LINK_BOOKING")).isEqualTo(0)
    assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "VIDEO_LINK_APPOINTMENT")).isEqualTo(0)

    val id = repository.save(
      VideoLinkBooking(offenderBookingId = 1, courtName = "A Court", madeByTheCourt = true).apply {
        addMainAppointment(appointmentId = 4)
        addPreAppointment(appointmentId = 12)
        addPostAppointment(appointmentId = 22)
      }
    ).id!!
    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()
    assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "VIDEO_LINK_BOOKING")).isEqualTo(1)
    assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "VIDEO_LINK_APPOINTMENT")).isEqualTo(3)

    val booking = repository.findById(id)
    assertThat(booking).isPresent

    repository.delete(booking.get())

    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()
    assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "VIDEO_LINK_BOOKING")).isEqualTo(0)
    assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "VIDEO_LINK_APPOINTMENT")).isEqualTo(0)
  }

  @Test
  fun `findByMainAppointmentIds no Ids`() {
    assertThat(repository.findByAppointmentIdsAndHearingType(listOf(), MAIN)).isEmpty()
  }

  @Test
  fun `findByMainAppointmentIds sparse`() {
    repository.saveAll(videoLinkBookings())

    val bookings = repository.findByAppointmentIdsAndHearingType((-999L..1000L step 2).map { it }, MAIN)
    assertThat(bookings).hasSize(5)

    val appointmentIds = bookings.map { it.appointments[MAIN]?.appointmentId }
    assertThat(appointmentIds).containsExactlyInAnyOrder(1L, 3L, 5L, 7L, 9L)
  }

  @Test
  fun `Removing appointments from a booking should delete the appointments`() {

    val id = repository.save(
      VideoLinkBooking(offenderBookingId = 1, courtName = "A Court", madeByTheCourt = true).apply {
        addMainAppointment(appointmentId = 4)
        addPreAppointment(appointmentId = 12)
        addPostAppointment(appointmentId = 22)
      }
    ).id!!

    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()

    val booking = repository.getOne(id)

    booking.appointments.remove(PRE)
    booking.appointments.remove(POST)

    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()
    assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "VIDEO_LINK_APPOINTMENT")).isEqualTo(1)
  }

  @Test
  fun `Replacing appointments should delete old and persist new`() {

    val id = repository.save(
      VideoLinkBooking(
        offenderBookingId = 1,
        courtName = "A Court",
        madeByTheCourt = true
      ).apply {
        addMainAppointment(appointmentId = 4)
        addPreAppointment(appointmentId = 12)
        addPostAppointment(appointmentId = 22)
      }
    ).id!!

    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()

    val booking = repository.getOne(id)

    /**
     * Have to flush() to force Hibernate to delete any old appointments before adding replacements.
     * This seems wrong to me. Now the clients have to remember to call flush(). (See updateVideoLinkBooking#updateVideoLinkBooking)
     */
    booking.appointments.clear()
    repository.flush()

    booking.addMainAppointment(appointmentId = 100)
    booking.addPreAppointment(appointmentId = 101)
    booking.addPostAppointment(appointmentId = 102)

    /**
     * Confirm that calling flush()  populates ids in the new VideoLinkAppointment objects.
     * CourtService#updateVideoLinkBooking depends on this behaviour to add those ids to an Application Insights custom
     * event.
     */
    assertThat(booking.appointments.values).allSatisfy { assertThat(it.id).isNull() }

    repository.flush()

    assertThat(booking.appointments.values).allSatisfy { assertThat(it.id).isNotNull() }

    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()

    assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "VIDEO_LINK_APPOINTMENT")).isEqualTo(3)

    val updatedBooking = repository.getOne(id)
    assertThat(updatedBooking.appointments.values.map { it.appointmentId }).contains(100, 101, 102)
  }

  fun videoLinkBookings(): List<VideoLinkBooking> =
    (1..10L).map {
      VideoLinkBooking(
        offenderBookingId = it * 100L,
        courtName = "Court",
        courtId = "TSTCRT",
        madeByTheCourt = true
      ).apply {
        addMainAppointment(appointmentId = it)
      }
    }
}
