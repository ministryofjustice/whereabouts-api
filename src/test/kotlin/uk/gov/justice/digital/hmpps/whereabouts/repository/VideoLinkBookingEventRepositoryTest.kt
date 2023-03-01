package uk.gov.justice.digital.hmpps.whereabouts.repository

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
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
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBookingEventType.CREATE
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBookingEventType.DELETE
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBookingEventType.UPDATE
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

  fun makeEvent(
    timestamp: LocalDateTime = aTimestamp,
    videoLinkBookingId: Long = 1L,
    eventType: VideoLinkBookingEventType = CREATE
  ) = VideoLinkBookingEvent(
    timestamp = timestamp,
    eventType = CREATE,
    userId = "X",
    videoLinkBookingId = videoLinkBookingId,
    agencyId = "MDI",
    comment = "Comment",
    court = "Court",
    courtId = "EYI",
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
    postNomisAppointmentId = 31L
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

  @Nested
  inner class `find by timestamp between` {
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

  @Nested
  inner class `find by start time between` {
    val referenceDay = LocalDate.of(2021, 3, 1)
    var eventTimestamp = referenceDay.minusDays(2).atStartOfDay()

    fun newEvent(
      mainStartTime: LocalDateTime? = null,
      videoLinkBookingId: Long,
      eventType: VideoLinkBookingEventType
    ): VideoLinkBookingEvent {
      eventTimestamp = eventTimestamp.plusSeconds(1L)

      return VideoLinkBookingEvent(
        timestamp = eventTimestamp,
        eventType = eventType,
        userId = "X",
        videoLinkBookingId = videoLinkBookingId,
        agencyId = "MDI",
        comment = "Comment",
        court = "Court",
        courtId = "EYI",
        madeByTheCourt = false,
        offenderBookingId = 2L,
        mainLocationId = 10L,
        mainStartTime = mainStartTime,
        mainEndTime = LocalDateTime.of(2021, 3, 1, 10, 0),
        mainNomisAppointmentId = 11L,
        preLocationId = 20L,
        preStartTime = LocalDateTime.of(2021, 3, 1, 8, 0),
        preEndTime = LocalDateTime.of(2021, 3, 1, 9, 0),
        preNomisAppointmentId = 21L,
        postLocationId = 30L,
        postStartTime = LocalDateTime.of(2021, 3, 1, 10, 0),
        postEndTime = LocalDateTime.of(2021, 3, 1, 11, 0),
        postNomisAppointmentId = 31L
      )
    }

    @Test
    fun `Can find bookings started on a specific day`() {
      repository.save(
        newEvent(
          videoLinkBookingId = 1,
          eventType = CREATE,
          mainStartTime = referenceDay.atStartOfDay().minusSeconds(1)
        )
      )
      repository.save(
        newEvent(
          videoLinkBookingId = 2,
          eventType = CREATE,
          mainStartTime = referenceDay.atStartOfDay()
        )
      )
      repository.save(
        newEvent(
          videoLinkBookingId = 3,
          eventType = CREATE,
          mainStartTime = referenceDay.plusDays(1).atStartOfDay().minusSeconds(1)
        )
      )
      repository.save(
        newEvent(
          videoLinkBookingId = 4,
          eventType = CREATE,
          mainStartTime = referenceDay.plusDays(1).atStartOfDay()
        )
      )

      TestTransaction.flagForCommit()
      TestTransaction.end()
      TestTransaction.start()

      assertThat(
        repository.findByStartTimeBetween(
          referenceDay,
          referenceDay
        )
      ).extracting(VideoLinkBookingEvent::videoLinkBookingId).containsExactly(tuple(2L), tuple(3L))
    }

    @Test
    fun `Can find bookings started between two dates`() {
      repository.save(
        newEvent(
          videoLinkBookingId = 1,
          eventType = CREATE,
          mainStartTime = referenceDay.atStartOfDay().minusSeconds(1)
        )
      )
      repository.save(
        newEvent(
          videoLinkBookingId = 2,
          eventType = CREATE,
          mainStartTime = referenceDay.atStartOfDay()
        )
      )
      repository.save(
        newEvent(
          videoLinkBookingId = 3,
          eventType = CREATE,
          mainStartTime = referenceDay.plusDays(1).atStartOfDay().minusSeconds(1)
        )
      )
      repository.save(
        newEvent(
          videoLinkBookingId = 4,
          eventType = CREATE,
          mainStartTime = referenceDay.plusDays(1).atStartOfDay()
        )
      )

      TestTransaction.flagForCommit()
      TestTransaction.end()
      TestTransaction.start()

      assertThat(
        repository.findByStartTimeBetween(
          referenceDay,
          referenceDay.plusDays(1)
        )
      ).extracting(VideoLinkBookingEvent::videoLinkBookingId).containsExactly(tuple(2L), tuple(3L), tuple(4L))
    }

    @Test
    fun `Only find single booking that main start time in time range and has been updated multiple times`() {
      repository.save(
        newEvent(
          videoLinkBookingId = 1,
          eventType = CREATE,
          mainStartTime = referenceDay.atStartOfDay()
        )
      )
      repository.save(
        newEvent(
          videoLinkBookingId = 1,
          eventType = UPDATE,
          mainStartTime = referenceDay.atStartOfDay().plusMinutes(30)
        )
      )
      repository.save(
        newEvent(
          videoLinkBookingId = 1,
          eventType = UPDATE,
          mainStartTime = referenceDay.atStartOfDay().plusHours(1)
        )
      )
      repository.save(
        newEvent(
          videoLinkBookingId = 1,
          eventType = UPDATE,
          mainStartTime = referenceDay.atStartOfDay().plusMinutes(32)
        )
      )

      TestTransaction.flagForCommit()
      TestTransaction.end()
      TestTransaction.start()

      assertThat(
        repository.findByStartTimeBetween(
          referenceDay,
          referenceDay
        )
      ).extracting(VideoLinkBookingEvent::videoLinkBookingId, VideoLinkBookingEvent::mainStartTime)
        .containsExactly(tuple(1L, referenceDay.atStartOfDay().plusMinutes(32)))
    }

    @Test
    fun `Can find booking that has had its main start time updated to be included in between 2 dates`() {
      repository.save(
        newEvent(
          videoLinkBookingId = 1,
          eventType = CREATE,
          mainStartTime = referenceDay.atStartOfDay().minusSeconds(1)
        )
      )
      repository.save(
        newEvent(
          videoLinkBookingId = 1,
          eventType = UPDATE,
          mainStartTime = referenceDay.atStartOfDay()
        )
      )

      TestTransaction.flagForCommit()
      TestTransaction.end()
      TestTransaction.start()

      assertThat(
        repository.findByStartTimeBetween(
          referenceDay,
          referenceDay
        )
      ).extracting(VideoLinkBookingEvent::videoLinkBookingId).containsExactly(tuple(1L))
    }

    @Test
    fun `Does not return booking that has had its main start time updated to be excluded in between 2 dates`() {
      repository.save(
        newEvent(
          videoLinkBookingId = 1,
          eventType = CREATE,
          mainStartTime = referenceDay.atStartOfDay()
        )
      )
      repository.save(
        newEvent(
          videoLinkBookingId = 1,
          eventType = UPDATE,
          mainStartTime = referenceDay.atStartOfDay().minusSeconds(1)
        )
      )

      TestTransaction.flagForCommit()
      TestTransaction.end()
      TestTransaction.start()

      assertThat(repository.findByStartTimeBetween(referenceDay, referenceDay)).isEmpty()
    }

    @Test
    fun `Does not return deleted bookings that previously had its main start time between 2 dates`() {
      repository.save(
        newEvent(
          videoLinkBookingId = 1,
          eventType = CREATE,
          mainStartTime = referenceDay.atStartOfDay()
        )
      )
      repository.save(
        newEvent(
          videoLinkBookingId = 1,
          eventType = DELETE
        )
      )

      TestTransaction.flagForCommit()
      TestTransaction.end()
      TestTransaction.start()

      assertThat(repository.findByStartTimeBetween(referenceDay, referenceDay)).isEmpty()
    }
  }
}
