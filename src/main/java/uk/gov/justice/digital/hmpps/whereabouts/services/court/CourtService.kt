package uk.gov.justice.digital.hmpps.whereabouts.services.court

import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.whereabouts.model.Court
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkAppointment
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

  fun findName(courtId: String): String? = idToNameMap[courtId]
  fun findId(courtName: String): String? = nameToIdMap[courtName.trim().lowercase()]

  fun chooseCourtName(appointment: VideoLinkAppointment): String {
    val courtName = appointment.courtId?.let { findName(it) } ?: appointment.court ?: UNKNOWN_COURT_NAME
    println("CourtService.chooseCourtName: appointment $appointment. Found courtName $courtName")
    return courtName
  }
}
