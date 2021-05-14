package uk.gov.justice.digital.hmpps.whereabouts.services

import uk.gov.justice.digital.hmpps.whereabouts.dto.prisonapi.ScheduledAppointmentDto

interface LocationFilter {
  fun filterLocations(appointment: ScheduledAppointmentDto): Boolean
}

class NoOpFilter : LocationFilter {
  override fun filterLocations(appointment: ScheduledAppointmentDto): Boolean {
    return true
  }
}

class OffenderLocationFilter(
  private val offenderLocationPrefix: String,
  private val offenderLocationDescriptionByOffenderNo: Map<String, String?>
) : LocationFilter {
  override fun filterLocations(appointment: ScheduledAppointmentDto): Boolean {
    return offenderLocationDescriptionByOffenderNo[appointment.offenderNo].orEmpty()
      .startsWith(offenderLocationPrefix)
  }
}
