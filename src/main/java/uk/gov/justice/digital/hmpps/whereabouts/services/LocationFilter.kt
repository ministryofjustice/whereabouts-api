package uk.gov.justice.digital.hmpps.whereabouts.services

import uk.gov.justice.digital.hmpps.whereabouts.dto.prisonapi.ScheduledAppointmentSearchDto

interface LocationFilter {
  fun filterLocations(appointment: ScheduledAppointmentSearchDto): Boolean
}

class NoOpFilter : LocationFilter {
  override fun filterLocations(appointment: ScheduledAppointmentSearchDto): Boolean {
    return true
  }
}

class OffenderLocationFilter(
  private val offenderLocationPrefix: String,
  private val offenderLocationDescriptionByOffenderNo: Map<String, String?>
) : LocationFilter {
  override fun filterLocations(appointment: ScheduledAppointmentSearchDto): Boolean {
    return offenderLocationDescriptionByOffenderNo[appointment.offenderNo].orEmpty()
      .startsWith(offenderLocationPrefix)
  }
}
