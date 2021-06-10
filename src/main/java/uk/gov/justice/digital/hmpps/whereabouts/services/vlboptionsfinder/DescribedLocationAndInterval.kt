package uk.gov.justice.digital.hmpps.whereabouts.services.vlboptionsfinder

import uk.gov.justice.digital.hmpps.whereabouts.services.locationfinder.Interval
import uk.gov.justice.digital.hmpps.whereabouts.services.locationfinder.LocationIdAndDescription

data class DescribedLocationAndInterval(
  val location: LocationIdAndDescription,
  val interval: Interval
)
