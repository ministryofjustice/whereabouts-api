package uk.gov.justice.digital.hmpps.whereabouts.services.court

import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.whereabouts.model.Court
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBooking
import uk.gov.justice.digital.hmpps.whereabouts.repository.CourtRepository

const val UNKNOWN_COURT_NAME = "Unknown"

@Service
class CourtService(private val courtRepository: CourtRepository) {
  val courtNames: List<String> by lazy {
    courts.map { it.name }
  }

  val courts: List<Court> by lazy { courtRepository.findAll(Sort.by("name")) }

  private val idToNameMap: Map<String, String> by lazy { courts.associate { it.id to it.name } }
  private val nameToIdMap: Map<String, String> by lazy { courts.associate { it.name.lowercase() to it.id } }

  fun getCourtNameForCourtId(courtId: String): String? = idToNameMap[courtId]
  fun getCourtIdForCourtName(courtName: String): String? = nameToIdMap[courtName.trim().lowercase()]

  fun chooseCourtName(booking: VideoLinkBooking): String {
    return booking.courtId?.let { getCourtNameForCourtId(it) } ?: booking.courtName ?: UNKNOWN_COURT_NAME
  }
}
