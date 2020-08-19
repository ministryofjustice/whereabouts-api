package uk.gov.justice.digital.hmpps.whereabouts.services

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.whereabouts.dto.CreateBookingAppointment
import uk.gov.justice.digital.hmpps.whereabouts.dto.CreateVideoLinkAppointment
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkAppointmentDto
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkAppointment
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkAppointmentRepository
import uk.gov.justice.digital.hmpps.whereabouts.security.AuthenticationFacade
import javax.transaction.Transactional

@Service
class CourtService(
        private val authenticationFacade: AuthenticationFacade,
        private val elite2ApiService: Elite2ApiService,
        private val videoLinkAppointmentRepository: VideoLinkAppointmentRepository,
        @Value("\${courts}") private val courts: String) {

  fun getCourtLocations() = courts.split(",").toSet()

  @Transactional
  fun createVideoLinkAppointment(createVideoLinkAppointment: CreateVideoLinkAppointment) {

    val eventId = elite2ApiService.postAppointment(createVideoLinkAppointment.bookingId, CreateBookingAppointment(
        appointmentType = "VLB",
        locationId = createVideoLinkAppointment.locationId,
        comment = createVideoLinkAppointment.comment,
        startTime = createVideoLinkAppointment.startTime.toString(),
        endTime = createVideoLinkAppointment.endTime.toString()
    ))

    videoLinkAppointmentRepository.save(VideoLinkAppointment(
        appointmentId = eventId,
        bookingId = createVideoLinkAppointment.bookingId,
        court = createVideoLinkAppointment.court,
        hearingType = createVideoLinkAppointment.hearingType,
        createdByUsername = authenticationFacade.currentUsername,
        madeByTheCourt = createVideoLinkAppointment.madeByTheCourt
    ))
  }

  fun getVideoLinkAppointments(appointmentIds: Set<Long>): Set<VideoLinkAppointmentDto> {
    return videoLinkAppointmentRepository
        .findVideoLinkAppointmentByAppointmentIdIn(appointmentIds)
        .asSequence()
        .map {
          VideoLinkAppointmentDto(
              id = it.id!!,
              bookingId = it.bookingId,
              appointmentId = it.appointmentId,
              hearingType = it.hearingType,
              court = it.court,
              createdByUsername = it.createdByUsername,
              madeByTheCourt = it.madeByTheCourt
          )
        }.toSet()
  }
}
