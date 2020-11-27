package uk.gov.justice.digital.hmpps.whereabouts.repository

import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.transaction.TestTransaction
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

  @MockBean
  lateinit var authenticationFacade: AuthenticationFacade

  @Test
  fun `should persist a booking (main only)`() {
    repository.deleteAll()

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
    repository.deleteAll()

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
  }
}
