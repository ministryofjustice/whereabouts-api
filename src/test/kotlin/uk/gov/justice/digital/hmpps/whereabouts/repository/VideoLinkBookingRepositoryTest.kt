package uk.gov.justice.digital.hmpps.whereabouts.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
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
import uk.gov.justice.digital.hmpps.whereabouts.model.CourtHearingType
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType.MAIN
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType.POST
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType.PRE
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBooking
import uk.gov.justice.digital.hmpps.whereabouts.security.AuthenticationFacade
import java.time.LocalDateTime
import java.util.function.Consumer

const val USERNAME = "username1"

@ActiveProfiles("test")
@Import(AuditConfiguration::class)
@DataJpaTest
@Transactional
class VideoLinkBookingRepositoryTest(
  @Autowired val repository: VideoLinkBookingRepository,
  @Autowired val jdbcTemplate: JdbcTemplate,
) {
  @MockBean
  lateinit var authenticationFacade: AuthenticationFacade

  val startDateTime = LocalDateTime.of(2022, 1, 1, 10, 0, 0)
  val endDateTime = LocalDateTime.of(2022, 1, 1, 11, 0, 0)
  val prisonId = "WWI"

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
    val theBooking = VideoLinkBooking(
      offenderBookingId = 1,
      courtName = "A Court",
      courtId = "TSTCRT",
      madeByTheCourt = true,
      courtHearingType = CourtHearingType.APPEAL,
      prisonId = prisonId,
    ).apply {
      addMainAppointment(2, 20L, startDateTime, endDateTime)
    }

    val id = repository.save(theBooking).id!!
    assertThat(id).isNotNull

    TestTransaction.flagForCommit()
    TestTransaction.end()

    TestTransaction.start()

    val persistentBooking = repository.getById(id)

    assertThat(persistentBooking)
      .extracting("offenderBookingId", "courtName", "courtId", "madeByTheCourt", "courtHearingType")
      .containsExactly(1L, "A Court", "TSTCRT", true, CourtHearingType.APPEAL)

    assertThat(persistentBooking.appointments).isEqualTo(theBooking.appointments)

    assertThat(persistentBooking.createdByUsername).isEqualTo(USERNAME)
  }

  @Test
  fun `should persist a booking (main, pre and post)`() {
    val theBooking = VideoLinkBooking(
      offenderBookingId = 1,
      courtName = "A Court",
      madeByTheCourt = true,
      prisonId = prisonId,
    ).apply {
      addMainAppointment(4, 20L, startDateTime, endDateTime)
      addPreAppointment(12, 20L, startDateTime, endDateTime)
      addPostAppointment(22, 20L, startDateTime, endDateTime)
    }

    val id = repository.save(theBooking).id!!
    assertThat(id).isNotNull

    TestTransaction.flagForCommit()
    TestTransaction.end()

    TestTransaction.start()

    val persistentBooking = repository.getById(id)

    assertThat(persistentBooking)
      .extracting("offenderBookingId", "courtName", "courtId", "madeByTheCourt")
      .containsExactly(1L, "A Court", null, true)

    assertThat(persistentBooking.appointments).isEqualTo(theBooking.appointments)

    val hearingTypes = jdbcTemplate.queryForList("select hearing_type from video_link_appointment", String::class.java)
    assertThat(hearingTypes).contains("PRE", "MAIN", "POST")

    assertThat(persistentBooking.createdByUsername).isEqualTo(USERNAME)
  }

  @Test
  fun `Deleting a booking by id should delete its appointments`() {
    assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "VIDEO_LINK_BOOKING")).isEqualTo(0)
    assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "VIDEO_LINK_APPOINTMENT")).isEqualTo(0)

    val id = repository.save(
      VideoLinkBooking(offenderBookingId = 1, courtName = "A Court", madeByTheCourt = true, prisonId = prisonId).apply {
        addMainAppointment(4, 20L, startDateTime, endDateTime)
        addPreAppointment(12, 20L, startDateTime, endDateTime)
        addPostAppointment(22, 20L, startDateTime, endDateTime)
      },
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
      VideoLinkBooking(offenderBookingId = 1, courtName = "A Court", madeByTheCourt = true, prisonId = prisonId).apply {
        addMainAppointment(4, 20L, startDateTime, endDateTime)
        addPreAppointment(12, 20L, startDateTime, endDateTime)
        addPostAppointment(22, 20L, startDateTime, endDateTime)
      },
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
  fun `findByAppointmentIdsAndHearingType no Ids`() {
    assertThat(repository.findByAppointmentIdsAndHearingType(listOf(), MAIN)).isEmpty()
  }

  @Test
  fun `findByAppointmentIdsAndHearingType sparse`() {
    repository.saveAll(videoLinkBookings())

    // commit and start new transaction so that Hibernate doesn't pull persisted objects from the session cache
    TestTransaction.flagForCommit()
    TestTransaction.end()

    TestTransaction.start()

    // -9 ... -1, 1, 3, 5, ... 39
    val appointmentIds = (-9L..40L step 2).map { it }
    val bookings = repository.findByAppointmentIdsAndHearingType(appointmentIds, MAIN)
    assertThat(bookings).hasSize(5)

    assertThat(bookings.map { it.appointments[PRE]?.appointmentId }).containsExactlyInAnyOrder(2L, 8L, 14L, 20L, 26L)
    assertThat(bookings.map { it.appointments[MAIN]?.appointmentId }).containsExactlyInAnyOrder(3L, 9L, 15L, 21L, 27L)
    assertThat(bookings.map { it.appointments[POST]?.appointmentId }).containsExactlyInAnyOrder(4L, 10L, 16L, 22L, 28L)
  }

  @Test
  fun `findByAppointmentIdsAndHearingType filter by courtName`() {
    repository.save(
      VideoLinkBooking(
        offenderBookingId = 1L,
        courtName = "C1",
        prisonId = prisonId,
      ).apply { addMainAppointment(100L, 20L, startDateTime, endDateTime) },
    )
    repository.save(
      VideoLinkBooking(
        offenderBookingId = 2L,
        courtName = "C2",
        prisonId = prisonId,
      ).apply { addMainAppointment(101L, 20L, startDateTime, endDateTime) },
    )
    repository.save(
      VideoLinkBooking(
        offenderBookingId = 3L,
        courtName = "C1",
        prisonId = prisonId,
      ).apply { addMainAppointment(102L, 20L, startDateTime, endDateTime) },
    )
    repository.save(
      VideoLinkBooking(
        offenderBookingId = 4L,
        courtName = "C2",
        prisonId = prisonId,
      ).apply { addMainAppointment(103L, 20L, startDateTime, endDateTime) },
    )

    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()

    assertThat(repository.findByAppointmentIdsAndHearingType(listOf(100L, 101L), MAIN))
      .extracting("offenderBookingId")
      .containsExactlyInAnyOrder(1L, 2L)

    assertThat(repository.findByAppointmentIdsAndHearingType(listOf(100L, 101L), MAIN, courtName = "C1"))
      .extracting("offenderBookingId")
      .containsExactlyInAnyOrder(1L)

    assertThat(repository.findByAppointmentIdsAndHearingType(listOf(100L, 101L), MAIN, courtName = "C2"))
      .extracting("offenderBookingId")
      .containsExactlyInAnyOrder(2L)
  }

  @Test
  fun `findByAppointmentIdsAndHearingType filter by courtId`() {
    repository.save(
      VideoLinkBooking(
        offenderBookingId = 1L,
        courtId = "C1",
        prisonId = prisonId,
      ).apply { addMainAppointment(100L, 20L, startDateTime, endDateTime) },
    )
    repository.save(
      VideoLinkBooking(
        offenderBookingId = 2L,
        courtId = "C2",
        prisonId = prisonId,
      ).apply { addMainAppointment(101L, 20L, startDateTime, endDateTime) },
    )
    repository.save(
      VideoLinkBooking(
        offenderBookingId = 3L,
        courtId = "C1",
        prisonId = prisonId,
      ).apply { addMainAppointment(102L, 20L, startDateTime, endDateTime) },
    )
    repository.save(
      VideoLinkBooking(
        offenderBookingId = 4L,
        courtId = "C2",
        prisonId = prisonId,
      ).apply { addMainAppointment(103L, 20L, startDateTime, endDateTime) },
    )

    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()

    assertThat(repository.findByAppointmentIdsAndHearingType(listOf(100L, 101L), MAIN))
      .extracting("offenderBookingId")
      .containsExactlyInAnyOrder(1L, 2L)

    assertThat(repository.findByAppointmentIdsAndHearingType(listOf(100L, 101L), MAIN, courtId = "C1"))
      .extracting("offenderBookingId")
      .containsExactlyInAnyOrder(1L)

    assertThat(repository.findByAppointmentIdsAndHearingType(listOf(100L, 101L), MAIN, courtId = "C2"))
      .extracting("offenderBookingId")
      .containsExactlyInAnyOrder(2L)
  }

  @Test
  fun `Removing appointments from a booking should delete the appointments`() {
    val id = repository.save(
      VideoLinkBooking(offenderBookingId = 1, courtName = "A Court", madeByTheCourt = true, prisonId = prisonId).apply {
        addMainAppointment(4, 20L, startDateTime, endDateTime)
        addPreAppointment(12, 20L, startDateTime, endDateTime)
        addPostAppointment(22, 20L, startDateTime, endDateTime)
      },
    ).id!!

    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()

    val booking = repository.getById(id)

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
        madeByTheCourt = true,
        prisonId = prisonId,
      ).apply {
        addMainAppointment(4, 20L, startDateTime, endDateTime)
        addPreAppointment(12, 20L, startDateTime, endDateTime)
        addPostAppointment(22, 20L, startDateTime, endDateTime)
      },
    ).id!!

    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()

    val booking = repository.getById(id)

    /**
     * Have to flush() to force Hibernate to delete any old appointments before adding replacements.
     * This seems wrong to me. Now the clients have to remember to call flush(). (See updateVideoLinkBooking#updateVideoLinkBooking)
     */
    booking.appointments.clear()
    repository.flush()

    booking.addMainAppointment(100, 20L, startDateTime, endDateTime)
    booking.addPreAppointment(101, 20L, startDateTime, endDateTime)
    booking.addPostAppointment(102, 20L, startDateTime, endDateTime)

    /**
     * Confirm that calling flush()  populates ids in the new VideoLinkAppointment objects.
     * CourtService#updateVideoLinkBooking depends on this behaviour to add those ids to an Application Insights custom
     * event.
     */
    assertThat(booking.appointments.values).allSatisfy(Consumer { assertThat(it.id).isNull() })

    repository.flush()

    assertThat(booking.appointments.values).allSatisfy(Consumer { assertThat(it.id).isNotNull() })

    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()

    assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "VIDEO_LINK_APPOINTMENT")).isEqualTo(3)

    val updatedBooking = repository.getById(id)
    assertThat(updatedBooking.appointments.values.map { it.appointmentId }).contains(100, 101, 102)
  }

  fun videoLinkBookings(): List<VideoLinkBooking> =
    (1..10L).map {
      VideoLinkBooking(
        offenderBookingId = it * 100L,
        courtName = "Court",
        courtId = "TSTCRT",
        madeByTheCourt = true,
        prisonId = prisonId,
      ).apply {
        addPreAppointment(it * 3 - 1, 20L, startDateTime, endDateTime)
        addMainAppointment(it * 3, 20L, startDateTime, endDateTime)
        addPostAppointment(it * 3 + 1, 20L, startDateTime, endDateTime)
      }
    }
}
