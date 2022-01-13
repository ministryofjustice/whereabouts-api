package uk.gov.justice.digital.hmpps.whereabouts.services.court

import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType.MAIN
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType.POST
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType.PRE
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBookingEvent
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBookingEventType
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingEventRepository
import uk.gov.justice.digital.hmpps.whereabouts.security.AuthenticationFacade
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class EventStoreListenerTest {
  private val repository: VideoLinkBookingEventRepository = mock()
  private val clock: Clock = Clock.fixed(Instant.parse("2020-10-01T00:00:00Z"), ZoneId.of("UTC"))
  private val authenticationFacade: AuthenticationFacade = mock()
  private val booking = EventListenerTestData.booking
  private val createSpecification = EventListenerTestData.createSpecification
  private val updateSpecification = EventListenerTestData.updateSpecification

  private val listener = EventStoreListener(repository, clock, authenticationFacade)

  @BeforeEach
  fun programMocks() {
    whenever(authenticationFacade.currentUsername).thenReturn("A_USER")
  }

  @Test
  fun `booking created`() {
    listener.bookingCreated(booking, createSpecification, "WWI")

    verify(repository).save(
      VideoLinkBookingEvent(
        eventType = VideoLinkBookingEventType.CREATE,
        timestamp = LocalDateTime.now(clock),
        userId = "A_USER",
        videoLinkBookingId = booking.id!!,
        agencyId = "WWI",
        court = createSpecification.court,
        courtId = createSpecification.courtId,
        comment = createSpecification.comment,
        offenderBookingId = createSpecification.bookingId,
        madeByTheCourt = createSpecification.madeByTheCourt,
        mainNomisAppointmentId = booking.appointments[MAIN]!!.appointmentId,
        mainLocationId = createSpecification.main.locationId,
        mainStartTime = createSpecification.main.startTime,
        mainEndTime = createSpecification.main.endTime,
        preNomisAppointmentId = booking.appointments[PRE]!!.appointmentId,
        preLocationId = createSpecification.pre!!.locationId,
        preStartTime = createSpecification.pre!!.startTime,
        preEndTime = createSpecification.pre!!.endTime,
        postLocationId = createSpecification.post!!.locationId,
        postNomisAppointmentId = booking.appointments[POST]!!.appointmentId,
        postStartTime = createSpecification.post!!.startTime,
        postEndTime = createSpecification.post!!.endTime
      )
    )
  }

  @Test
  fun `booking updated`() {
    listener.bookingUpdated(booking, updateSpecification)

    verify(repository).save(
      VideoLinkBookingEvent(
        eventType = VideoLinkBookingEventType.UPDATE,
        timestamp = LocalDateTime.now(clock),
        userId = "A_USER",
        videoLinkBookingId = booking.id!!,
        courtId = updateSpecification.courtId,
        comment = updateSpecification.comment,
        mainNomisAppointmentId = booking.appointments[MAIN]!!.appointmentId,
        mainLocationId = updateSpecification.main.locationId,
        mainStartTime = updateSpecification.main.startTime,
        mainEndTime = updateSpecification.main.endTime,
        preNomisAppointmentId = booking.appointments[PRE]!!.appointmentId,
        preLocationId = updateSpecification.pre!!.locationId,
        preStartTime = updateSpecification.pre!!.startTime,
        preEndTime = updateSpecification.pre!!.endTime,
        postLocationId = updateSpecification.post!!.locationId,
        postNomisAppointmentId = booking.appointments[POST]!!.appointmentId,
        postStartTime = updateSpecification.post!!.startTime,
        postEndTime = updateSpecification.post!!.endTime
      )
    )
  }

  @Test
  fun `booking deleted`() {
    listener.bookingDeleted(booking)

    verify(repository).save(
      VideoLinkBookingEvent(
        eventType = VideoLinkBookingEventType.DELETE,
        timestamp = LocalDateTime.now(clock),
        userId = "A_USER",
        videoLinkBookingId = booking.id!!
      )
    )
  }
}
