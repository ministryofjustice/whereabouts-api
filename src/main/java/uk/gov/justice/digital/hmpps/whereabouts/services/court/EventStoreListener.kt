package uk.gov.justice.digital.hmpps.whereabouts.services.court

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingSpecification
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingUpdateSpecification
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType.MAIN
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType.POST
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType.PRE
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBooking
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBookingEvent
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBookingEventType
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingEventRepository
import uk.gov.justice.digital.hmpps.whereabouts.security.AuthenticationFacade
import java.time.Clock
import java.time.LocalDateTime

@Component
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
        courtId = specification.courtId,
        comment = specification.comment,
        offenderBookingId = specification.bookingId,
        madeByTheCourt = specification.madeByTheCourt,
        mainNomisAppointmentId = booking.appointments[MAIN]?.appointmentId,
        mainLocationId = specification.main.locationId,
        mainStartTime = specification.main.startTime,
        mainEndTime = specification.main.endTime,
        preNomisAppointmentId = booking.appointments[PRE]?.appointmentId,
        preLocationId = specification.pre?.locationId,
        preStartTime = specification.pre?.startTime,
        preEndTime = specification.pre?.endTime,
        postLocationId = specification.post?.locationId,
        postNomisAppointmentId = booking.appointments[POST]?.appointmentId,
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
        courtId = specification.courtId,
        court = booking.courtName,
        comment = specification.comment,
        mainNomisAppointmentId = booking.appointments[MAIN]?.appointmentId,
        mainLocationId = specification.main.locationId,
        mainStartTime = specification.main.startTime,
        mainEndTime = specification.main.endTime,
        preNomisAppointmentId = booking.appointments[PRE]?.appointmentId,
        preLocationId = specification.pre?.locationId,
        preStartTime = specification.pre?.startTime,
        preEndTime = specification.pre?.endTime,
        postLocationId = specification.post?.locationId,
        postNomisAppointmentId = booking.appointments[POST]?.appointmentId,
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
        videoLinkBookingId = booking.id!!,
        court = booking.courtName,
        courtId = booking.courtId
      )
    )
  }
}
