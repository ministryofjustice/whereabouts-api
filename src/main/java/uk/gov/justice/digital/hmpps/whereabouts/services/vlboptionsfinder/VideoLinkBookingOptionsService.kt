package uk.gov.justice.digital.hmpps.whereabouts.services.vlboptionsfinder

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingRepository
import uk.gov.justice.digital.hmpps.whereabouts.services.PrisonApiService

interface IVideoLinkBookingOptionsService {
  fun findVideoLinkBookingOptions(specification: VideoLinkBookingSearchSpecification): VideoLinkBookingOptions
}

@Service
class VideoLinkBookingOptionsService(
  private val prisonApiService: PrisonApiService,
  private val videoLinkBookingRepository: VideoLinkBookingRepository,
  private val videoLinkBookingOptionsFinder: VideoLinkBookingOptionsFinder,
) : IVideoLinkBookingOptionsService {
  override fun findVideoLinkBookingOptions(specification: VideoLinkBookingSearchSpecification): VideoLinkBookingOptions {
    val excludedAppointmentIds =
      specification.vlbIdToExclude?.let { appointmentIdsFromBookingId(it) } ?: emptyList()

    val locationIds = listOfNotNull(
      specification.preAppointment?.locationId,
      specification.mainAppointment.locationId,
      specification.postAppointment?.locationId,
    ).distinct()

    val scheduledAppointments = locationIds
      .flatMap { prisonApiService.getScheduledAppointments(specification.agencyId, specification.date, null, it) }
      .filter { it.endTime != null }
      .filterNot { excludedAppointmentIds.contains(it.id) }

    return videoLinkBookingOptionsFinder.findOptions(specification, scheduledAppointments)
  }

  fun appointmentIdsFromBookingId(vlbIdToExclude: Long): Set<Long> =
    videoLinkBookingRepository
      .findById(vlbIdToExclude)
      .map { booking -> booking.appointments.values.map { it.appointmentId } }
      .orElse(emptyList())
      .toSet()
}
