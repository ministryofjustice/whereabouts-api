package uk.gov.justice.digital.hmpps.whereabouts.services

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.whereabouts.dto.CourtAppointmentDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.CreateBookingAppointment
import uk.gov.justice.digital.hmpps.whereabouts.dto.CreateCourtAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.CourtAppointment
import uk.gov.justice.digital.hmpps.whereabouts.repository.CourtAppointmentRepository
import javax.transaction.Transactional

@Service
class CourtService(private val elite2ApiService: Elite2ApiService, private val courtAppointmentRepository: CourtAppointmentRepository) {

  @Transactional
  fun addCourtAppointment(createCourtAppointment: CreateCourtAppointment) {
    val eventId = elite2ApiService.postAppointment(createCourtAppointment.bookingId, CreateBookingAppointment(
        appointmentType = "VLB",
        locationId = createCourtAppointment.locationId,
        comment = createCourtAppointment.comment,
        startTime = createCourtAppointment.startTime,
        endTime = createCourtAppointment.endTime
    ))

    courtAppointmentRepository.save(CourtAppointment(
        appointmentId = eventId,
        bookingId = createCourtAppointment.bookingId,
        court = createCourtAppointment.court,
        hearingType = createCourtAppointment.hearingType
    ))
  }

  fun getCourtAppointments(appointmentIds: Set<Long>): Set<CourtAppointmentDto> {
    return courtAppointmentRepository
        .findCourtAppointmentByAppointmentIdIn(appointmentIds)
        .asSequence()
        .map {
          CourtAppointmentDto(
              id = it.id,
              bookingId = it.bookingId,
              appointmentId = it.appointmentId,
              hearingType = it.hearingType,
              court = it.court
          )
        }.toSet()
  }
}
