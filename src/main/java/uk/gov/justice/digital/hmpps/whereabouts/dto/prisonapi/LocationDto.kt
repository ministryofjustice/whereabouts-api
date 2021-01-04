package uk.gov.justice.digital.hmpps.whereabouts.dto.prisonapi

data class LocationDto(
  val locationId: Long,
  val locationType: String,
  val description: String?,
  val agencyId: String,
  val parentLocationId: Long?,
  val currentOccupancy: Long?,
  val locationPrefix: String?,
  val internalLocationCode: String?,
  val userDescription: String?,
  val locationUsage: String?,
  val operationalCapacity: Long?
)
