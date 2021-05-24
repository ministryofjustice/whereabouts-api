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
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkAppointment
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
    JdbcTestUtils.deleteFromTables(jdbcTemplate, "VIDEO_LINK_BOOKING", "VIDEO_LINK_APPOINTMENT")
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
      main = VideoLinkAppointment(
        bookingId = 1,
        appointmentId = 2,
        court = "A Court",
        courtId = "TSTCRT",
        hearingType = HearingType.MAIN,
        madeByTheCourt = true
      )
    )

    val id = repository.save(transientBooking).id!!
    assertThat(id).isNotNull

    TestTransaction.flagForCommit()
    TestTransaction.end()

    val persistentBooking = repository.getOne(id)

    assertThat(persistentBooking)
      .usingRecursiveComparison()
      .ignoringFields("id", "main.id")
      .isEqualTo(transientBooking)

    assertThat(persistentBooking.main.createdByUsername).isEqualTo(USERNAME)
  }

  @Test
  fun `should persist a booking (main, pre and post)`() {

    val transientBooking = VideoLinkBooking(
      main = VideoLinkAppointment(
        bookingId = 1,
        appointmentId = 4,
        court = "A Court",
        hearingType = HearingType.MAIN,
        madeByTheCourt = true
      ),
      pre = VideoLinkAppointment(
        bookingId = 1,
        appointmentId = 12,
        court = "A Court",
        courtId = "TSTCRT",
        hearingType = HearingType.PRE,
        madeByTheCourt = true
      ),
      post = VideoLinkAppointment(
        bookingId = 1,
        appointmentId = 22,
        court = null,
        courtId = "TSTCRT",
        hearingType = HearingType.POST,
        madeByTheCourt = true
      ),
    )

    val id = repository.save(transientBooking).id!!
    assertThat(id).isNotNull

    TestTransaction.flagForCommit()
    TestTransaction.end()

    val persistentBooking = repository.getOne(id)

    assertThat(persistentBooking)
      .usingRecursiveComparison()
      .ignoringFields("id", "pre.id", "main.id", "post.id")
      .isEqualTo(transientBooking)

    val hearingTypes = jdbcTemplate.queryForList("select hearing_type from video_link_appointment", String::class.java)
    assertThat(hearingTypes).contains("PRE", "MAIN", "POST")

    assertThat(persistentBooking)
      .extracting(
        "main.createdByUsername",
        "pre.createdByUsername",
        "post.createdByUsername"
      )
      .containsExactly(
        USERNAME,
        USERNAME,
        USERNAME
      )
  }

  @Test
  fun `Deleting a booking should delete its appointments`() {

    assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "VIDEO_LINK_BOOKING")).isEqualTo(0)
    assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "VIDEO_LINK_APPOINTMENT")).isEqualTo(0)
    val id = repository.save(
      VideoLinkBooking(
        main = VideoLinkAppointment(
          bookingId = 1,
          appointmentId = 4,
          court = "A Court",
          hearingType = HearingType.MAIN,
          madeByTheCourt = true
        ),
        pre = VideoLinkAppointment(
          bookingId = 1,
          appointmentId = 12,
          court = "A Court",
          hearingType = HearingType.PRE,
          madeByTheCourt = true
        ),
        post = VideoLinkAppointment(
          bookingId = 1,
          appointmentId = 22,
          court = "A Court",
          hearingType = HearingType.POST,
          madeByTheCourt = true
        ),
      )
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
  fun `findByMainAppointmentIds no Ids`() {
    assertThat(repository.findByMainAppointmentIds(listOf())).isEmpty()
  }

  @Test
  fun `findByMainAppointmentIds sparse`() {
    repository.saveAll(videoLinkBookings())

    assertThat(repository.findByMainAppointmentIds((-999L..1000L step 2).map { it }))
      .hasSize(5)
      .extracting("main.appointmentId").containsExactlyInAnyOrder(1L, 3L, 5L, 7L, 9L)
  }

  @Test
  fun `Removing appointments from a booking should delete the appointments`() {

    val id = repository.save(
      VideoLinkBooking(
        main = VideoLinkAppointment(
          bookingId = 1,
          appointmentId = 4,
          court = "A Court",
          hearingType = HearingType.MAIN,
          madeByTheCourt = true
        ),
        pre = VideoLinkAppointment(
          bookingId = 1,
          appointmentId = 12,
          court = "A Court",
          hearingType = HearingType.PRE,
          madeByTheCourt = true
        ),
        post = VideoLinkAppointment(
          bookingId = 1,
          appointmentId = 22,
          court = "A Court",
          hearingType = HearingType.POST,
          madeByTheCourt = true
        ),
      )
    ).id!!

    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()

    val booking = repository.getOne(id)

    booking.pre = null
    booking.post = null

    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()
    assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "VIDEO_LINK_APPOINTMENT")).isEqualTo(1)
  }

  @Test
  fun `Replacing appointments should delete old and persist new`() {

    val id = repository.save(
      VideoLinkBooking(
        main = VideoLinkAppointment(
          bookingId = 1,
          appointmentId = 4,
          court = "A Court",
          hearingType = HearingType.MAIN,
          madeByTheCourt = true
        ),
        pre = VideoLinkAppointment(
          bookingId = 1,
          appointmentId = 12,
          court = "A Court",
          hearingType = HearingType.PRE,
          madeByTheCourt = true
        ),
        post = VideoLinkAppointment(
          bookingId = 1,
          appointmentId = 22,
          court = "A Court",
          hearingType = HearingType.POST,
          madeByTheCourt = true
        ),
      )
    ).id!!

    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()

    val booking = repository.getOne(id)

    booking.main = VideoLinkAppointment(
      bookingId = 1,
      appointmentId = 100,
      court = "A Court",
      hearingType = HearingType.MAIN,
      madeByTheCourt = false
    )

    booking.pre = VideoLinkAppointment(
      bookingId = 1,
      appointmentId = 101,
      court = "A Court",
      hearingType = HearingType.PRE,
      madeByTheCourt = false
    )

    booking.post = VideoLinkAppointment(
      bookingId = 1,
      appointmentId = 102,
      court = "A Court",
      hearingType = HearingType.POST,
      madeByTheCourt = false
    )

    /**
     * Confirm that calling flush() populates ids in the new VideoLinkAppointment objects.
     * CourtService#updateVideoLinkBooking depends on this behaviour to add those ids to an Application Insights custom
     * event. Confirming the required behaviour like this is easier than changing the CourtIntegrationTest class so that
     * it uses this repository instead of a mock.
     */
    assertThat(booking.pre?.id).isNull()
    assertThat(booking.main.id).isNull()
    assertThat(booking.post?.id).isNull()
    repository.flush()
    assertThat(booking.pre?.id).isNotNull
    assertThat(booking.main.id).isNotNull
    assertThat(booking.post?.id).isNotNull

    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()
    assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "VIDEO_LINK_APPOINTMENT")).isEqualTo(3)

    val updatedBooking = repository.getOne(id)
    assertThat(updatedBooking.main.appointmentId).isEqualTo(100)
    assertThat(updatedBooking.pre?.appointmentId).isEqualTo(101)
    assertThat(updatedBooking.post?.appointmentId).isEqualTo(102)
  }

  fun videoLinkBookings(): List<VideoLinkBooking> =
    (1..10L).map {
      VideoLinkBooking(
        main = VideoLinkAppointment(
          bookingId = it * 100L,
          appointmentId = it,
          court = "Court",
          courtId = "TSTCRT",
          hearingType = HearingType.MAIN,
          madeByTheCourt = true
        )
      )
    }
}
