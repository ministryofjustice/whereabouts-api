package uk.gov.justice.digital.hmpps.whereabouts.services.locationfinder

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingRepository
import uk.gov.justice.digital.hmpps.whereabouts.services.PrisonApiService
import uk.gov.justice.digital.hmpps.whereabouts.services.vlboptionsfinder.VideoLinkBookingOptions
import uk.gov.justice.digital.hmpps.whereabouts.services.vlboptionsfinder.VideoLinkBookingOptionsFinder
import uk.gov.justice.digital.hmpps.whereabouts.services.vlboptionsfinder.VideoLinkBookingSearchSpecification

@Service
class VideoLinkBookingOptionsService(
  private val prisonApiService: PrisonApiService,
  private val videoLinkBookingRepository: VideoLinkBookingRepository,
  private val videoLinkBookingOptionsFinder: VideoLinkBookingOptionsFinder
) {
  fun findVideoLinkBookingOptions(specification: VideoLinkBookingSearchSpecification): VideoLinkBookingOptions {
    val excludedAppointmentIds =
      specification.vlbIdToExclude?.let { appointmentIdsFromBookingIds(listOf(it)) } ?: emptyList()

    val locationIds = listOfNotNull(
      specification.preAppointment?.locationId,
      specification.mainAppointment.locationId,
      specification.postAppointment?.locationId
    ).distinct()

    val scheduledAppointments = locationIds
      .flatMap { prisonApiService.getScheduledAppointments(specification.agencyId, specification.date, null, it) }
      .filter { it.endTime != null }
      .filterNot { excludedAppointmentIds.contains(it.id) }

    return videoLinkBookingOptionsFinder.findOptions(specification, scheduledAppointments)
  }

  private fun appointmentIdsFromBookingIds(vlbIdsToExclude: List<Long>): Set<Long> =
    if (vlbIdsToExclude.isEmpty())
      emptySet()
    else
      videoLinkBookingRepository
        .findAllById(vlbIdsToExclude)
        .flatMap { booking -> booking.appointments.values.map { it.appointmentId } }
        .toSet()
}