package uk.gov.justice.digital.hmpps.whereabouts.repository

import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
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
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBooking
import uk.gov.justice.digital.hmpps.whereabouts.security.AuthenticationFacade

@ActiveProfiles("test")
@Import(TestAuditConfiguration::class)
@DataJpaTest
@Transactional
class VideoLinkBookingRepositoryTest {

  @Autowired
  lateinit var repository: VideoLinkBookingRepository

  @Autowired
  lateinit var jdbcTemplate: JdbcTemplate

  @MockBean
  lateinit var authenticationFacade: AuthenticationFacade

  fun deleteAll() {
    JdbcTestUtils.deleteFromTables(jdbcTemplate, "VIDEO_LINK_BOOKING", "VIDEO_LINK_APPOINTMENT")
    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()
  }

  @Test
  fun `should persist a booking (main only)`() {
    deleteAll()

    whenever(authenticationFacade.currentUsername).thenReturn("username1")

    val transientBooking = VideoLinkBooking(
      main = VideoLinkAppointment(
        bookingId = 1,
        appointmentId = 2,
        court = "A Court",
        hearingType = HearingType.MAIN,
        madeByTheCourt = true
      )
    )

    val id = repository.save(transientBooking).id!!
    assertThat(id).isNotNull

    TestTransaction.flagForCommit()
    TestTransaction.end()

    val persistentBooking = repository.findById(id)
    assertThat(persistentBooking).isNotEmpty
    val main = persistentBooking.get().main
    assertThat(main).isNotNull
    assertThat(main.id).isNotNull
    assertThat(main).isEqualToIgnoringGivenFields(transientBooking.main, "id")
  }

  @Test
  fun `should persist a booking (main, pre and post)`() {
    deleteAll()

    whenever(authenticationFacade.currentUsername).thenReturn("username1")

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

    val id = repository.save(transientBooking).id!!
    assertThat(id).isNotNull

    TestTransaction.flagForCommit()
    TestTransaction.end()

    val persistentBookingOptional = repository.findById(id)
    assertThat(persistentBookingOptional).isNotEmpty

    val persistentBooking = persistentBookingOptional.get()

    assertThat(persistentBooking).isEqualToIgnoringGivenFields(transientBooking, "id")

    val hearingTypes = jdbcTemplate.queryForList("select hearing_type from video_link_appointment", String::class.java)
    assertThat(hearingTypes).contains("PRE", "MAIN", "POST")
  }

  @Test
  fun `Deleting a booking should delete its appointments`() {
    deleteAll()

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
    deleteAll()
    assertThat(repository.findByMainAppointmentIds(listOf())).isEmpty()
  }

  @Test
  fun `findByMainAppointmentIds sparse`() {
    deleteAll()
    repository.saveAll(videoLinkBookings())

    assertThat(repository.findByMainAppointmentIds((-999L..1000L step 2).map { it }))
      .hasSize(5)
      .extracting("main.appointmentId").containsExactlyInAnyOrder(1L, 3L, 5L, 7L, 9L)
  }

  fun videoLinkBookings(): List<VideoLinkBooking> =
    (1..10L).map {
      VideoLinkBooking(
        main = VideoLinkAppointment(
          bookingId = it * 100L,
          appointmentId = it,
          court = "Court",
          hearingType = HearingType.MAIN,
          madeByTheCourt = true
        )
      )
    }
}
