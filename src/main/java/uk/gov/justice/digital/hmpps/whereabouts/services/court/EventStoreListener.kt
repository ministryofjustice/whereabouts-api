package uk.gov.justice.digital.hmpps.whereabouts.services.court

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingSpecification
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingUpdateSpecification
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBooking
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBookingEvent
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBookingEventType
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingEventRepository
import uk.gov.justice.digital.hmpps.whereabouts.security.AuthenticationFacade
import java.time.Clock
import java.time.LocalDateTime

@Component
@Transactional
@Qualifier("delegate")
class EventStoreListener(
  val repository: VideoLinkBookingEventRepository,
  val clock: Clock,
  private val authenticationFacade: AuthenticationFacade,
) : VideoLinkBookingEventListener {
  override fun bookingCreated(booking: VideoLinkBooking, specification: VideoLinkBookingSpecification, agencyId: String) {
    repository.save(
      VideoLinkBookingEvent(
        eventType = VideoLinkBookingEventType.CREATE,
        timestamp = LocalDateTime.now(clock),
        userId = authenticationFacade.currentUsername,
        videoLinkBookingId = booking.id!!,
        agencyId = agencyId,
        court = specification.court,
        comment = specification.comment,
        offenderBookingId = specification.bookingId,
        madeByTheCourt = specification.madeByTheCourt,
        mainNomisAppointmentId = booking.main.appointmentId,
        mainLocationId = specification.main.locationId,
        mainStartTime = specification.main.startTime,
        mainEndTime = specification.main.endTime,
        preNomisAppointmentId = specification.pre?.locationId,
        preLocationId = specification.pre?.locationId,
        preStartTime = specification.pre?.startTime,
        preEndTime = specification.pre?.endTime,
        postLocationId = specification.post?.locationId,
        postNomisAppointmentId = booking.post?.appointmentId,
        postStartTime = specification.post?.startTime,
        postEndTime = specification.post?.endTime
      )
    )
  }

  override fun bookingUpdated(booking: VideoLinkBooking, specification: VideoLinkBookingUpdateSpecification) {
    repository.save(
      VideoLinkBookingEvent(
        eventType = VideoLinkBookingEventType.UPDATE,
        timestamp = LocalDateTime.now(clock),
        userId = authenticationFacade.currentUsername,
        videoLinkBookingId = booking.id!!,
        comment = specification.comment,
        mainNomisAppointmentId = booking.main.appointmentId,
        mainLocationId = specification.main.locationId,
        mainStartTime = specification.main.startTime,
        mainEndTime = specification.main.endTime,
        preNomisAppointmentId = specification.pre?.locationId,
        preLocationId = specification.pre?.locationId,
        preStartTime = specification.pre?.startTime,
        preEndTime = specification.pre?.endTime,
        postLocationId = specification.post?.locationId,
        postNomisAppointmentId = booking.post?.appointmentId,
        postStartTime = specification.post?.startTime,
        postEndTime = specification.post?.endTime
      )
    )
  }

  override fun bookingDeleted(booking: VideoLinkBooking) {
    repository.save(
      VideoLinkBookingEvent(
        eventType = VideoLinkBookingEventType.DELETE,
        timestamp = LocalDateTime.now(clock),
        userId = authenticationFacade.currentUsername,
        videoLinkBookingId = booking.id!!
      )
    )
  }
}
