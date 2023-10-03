package uk.gov.justice.digital.hmpps.whereabouts.services.court

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.whereabouts.model.LocationIdAndDescription
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBookingEvent
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBookingEventWithRoomNames
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingEventRepository
import uk.gov.justice.digital.hmpps.whereabouts.repository.findByDatesBetween
import uk.gov.justice.digital.hmpps.whereabouts.repository.findByStartTimeBetween
import uk.gov.justice.digital.hmpps.whereabouts.services.LocationService
import java.time.LocalDate
import java.util.stream.Collectors
import java.util.stream.Stream

@Service
class VideoLinkBookingEventService(
  val repository: VideoLinkBookingEventRepository,
  val csvConverter: EventToCsvConverter,
  val courtsService: CourtService,
  private val locationService: LocationService,
) {
  @Transactional(readOnly = true)
  fun getEventsAsCSV(startDate: LocalDate, days: Long): String {
    var bookingEvents = repository.findByDatesBetween(startDate, startDate.plusDays(days))
    return getBookingsAsCSV(bookingEvents)
  }

  @Transactional(readOnly = true)
  fun getBookingsByStartDateAsCSV(startDate: LocalDate, days: Long): String {
    var bookingEvents = repository.findByStartTimeBetween(startDate, startDate.plusDays(days))
    return getBookingsAsCSV(bookingEvents)
  }

  private fun getBookingsAsCSV(bookingEventsStream: Stream<VideoLinkBookingEvent>): String {
    var bookingEvents = bookingEventsStream.collect(Collectors.toList())
    bookingEvents = bookingEvents.map(::withCourtName)
    val agencyIds = bookingEvents.map { it.agencyId }.distinct()
    val roomNames = roomNamesMap(agencyIds)
    val bookingEventsWithRoomNames = bookingEvents.map { withRoomNames(it, roomNames) }
    return csvConverter.toCsv(bookingEventsWithRoomNames)
  }

  private fun withCourtName(event: VideoLinkBookingEvent): VideoLinkBookingEvent {
    val courtName = event.courtId?.let { courtsService.getCourtNameForCourtId(it) } ?: event.court
    return event.copy(court = courtName)
  }

  private fun getRoomsInLocation(agencyId: String?): List<LocationIdAndDescription> {
    return if (agencyId == null) listOf() else locationService.getAllLocationsForPrison(agencyId)
  }

  private fun roomNamesMap(agencyIds: List<String?>): Map<Long, String> {
    return agencyIds.map(::getRoomsInLocation)
      .flatten()
      .associateBy({ it.locationId }, { it.description })
  }

  private fun withRoomNames(event: VideoLinkBookingEvent, roomNames: Map<Long, String>): VideoLinkBookingEventWithRoomNames {
    return VideoLinkBookingEventWithRoomNames(
      eventType = event.eventType,
      timestamp = event.timestamp,
      userId = event.userId,
      videoLinkBookingId = event.videoLinkBookingId,
      agencyId = event.agencyId,
      offenderBookingId = event.offenderBookingId,
      court = event.court,
      courtId = event.courtId,
      madeByTheCourt = event.madeByTheCourt,
      comment = event.comment,
      mainNomisAppointmentId = event.mainNomisAppointmentId,
      mainLocationId = event.mainLocationId,
      mainStartTime = event.mainStartTime,
      mainEndTime = event.mainEndTime,
      preNomisAppointmentId = event.preNomisAppointmentId,
      preLocationId = event.preLocationId,
      preStartTime = event.preStartTime,
      preEndTime = event.preEndTime,
      postNomisAppointmentId = event.postNomisAppointmentId,
      postLocationId = event.postLocationId,
      postStartTime = event.postStartTime,
      postEndTime = event.postEndTime,
      mainLocationName = roomNames[event.mainLocationId],
      preLocationName = roomNames[event.preLocationId],
      postLocationName = roomNames[event.postLocationId],
    )
  }
}
