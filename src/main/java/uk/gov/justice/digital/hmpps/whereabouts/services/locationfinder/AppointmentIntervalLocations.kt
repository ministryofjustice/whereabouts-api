package uk.gov.justice.digital.hmpps.whereabouts.services.locationfinder

/**
 * Internal data class corresponding to AvailableLocations.  This version holds the locations as as
 * a mutable set of Long so that it may be built up over time.
 *
 * TODO: I'd give this package visibility, but I 'm not sure know how to do that in Kotlin.
 */
data class AppointmentIntervalLocations(
  val appointmentInterval: Interval,
  var locationIds: MutableSet<Long>?
) {
  constructor(
    appointmentInterval: Interval,
    vararg locationIds: Long
  ) : this(
    appointmentInterval,
    locationIds.toMutableSet()
  )
}
