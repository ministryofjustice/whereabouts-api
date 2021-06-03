package uk.gov.justice.digital.hmpps.whereabouts.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.transaction.TestTransaction
import org.springframework.test.jdbc.JdbcTestUtils
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBookingEvent
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBookingEventType
import java.time.LocalDate
import java.time.LocalDateTime

@ActiveProfiles("test")
@DataJpaTest
@Transactional
class VideoLinkBookingEventRepositoryTest(
  @Autowired val repository: VideoLinkBookingEventRepository,
  @Autowired val jdbcTemplate: JdbcTemplate
) {

  private val aTimestamp: LocalDateTime = LocalDateTime.of(2021, 3, 1, 12, 0)

  fun makeEvent(timestamp: LocalDateTime = aTimestamp) = VideoLinkBookingEvent(
    timestamp = timestamp,
    eventType = VideoLinkBookingEventType.CREATE,
    userId = "X",
    videoLinkBookingId = 1L,
    agencyId = "MDI",
    comment = "Comment",
    court = "Court",
    madeByTheCourt = false,
    offenderBookingId = 2L,
    mainLocationId = 10L,
    mainStartTime = LocalDateTime.of(2021, 3, 1, 9, 0),
    mainEndTime = LocalDateTime.of(2021, 3, 1, 10, 0),
    mainNomisAppointmentId = 11L,
    preLocationId = 20L,
    preStartTime = LocalDateTime.of(2021, 3, 1, 8, 0),
    preEndTime = LocalDateTime.of(2021, 3, 1, 9, 0),
    preNomisAppointmentId = 21L,
    postLocationId = 30L,
    postStartTime = LocalDateTime.of(2021, 3, 1, 10, 0),
    postEndTime = LocalDateTime.of(2021, 3, 1, 11, 0),
    postNomisAppointmentId = 31L,
  )

  @BeforeEach
  fun deleteAll() {
    JdbcTestUtils.deleteFromTables(jdbcTemplate, "VIDEO_LINK_BOOKING_EVENT")
    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()
  }

  @Test
  fun `persist Create event`() {
    repository.save(makeEvent())
    TestTransaction.flagForCommit()
    TestTransaction.end()

    TestTransaction.start()
    val events = repository.findAll()
    assertThat(events).hasSize(1)

    val event = events[0]
    assertThat(event).usingRecursiveComparison().ignoringFields("eventId").isEqualTo(makeEvent())
    assertThat(event.eventId).isNotNull
  }

  @Test
  fun `find by timestamp between`() {
    val referenceDay = LocalDate.of(2021, 3, 1)

    repository.save(makeEvent(timestamp = referenceDay.atStartOfDay().minusSeconds(1)))
    repository.save(makeEvent(timestamp = referenceDay.atStartOfDay()))
    repository.save(makeEvent(timestamp = referenceDay.plusDays(1).atStartOfDay().minusSeconds(1)))
    repository.save(makeEvent(timestamp = referenceDay.plusDays(1).atStartOfDay()))

    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()

    assertThat(repository.findByDatesBetween(referenceDay, referenceDay)).hasSize(2)
    assertThat(repository.findByDatesBetween(referenceDay, referenceDay.plusDays(1))).hasSize(3)
    assertThat(repository.findByDatesBetween(referenceDay.minusDays(1), referenceDay.plusDays(1))).hasSize(4)
  }
}
