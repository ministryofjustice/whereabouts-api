package uk.gov.justice.digital.hmpps.whereabouts.services

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import uk.gov.justice.digital.hmpps.whereabouts.model.Location

class LocationGroupFromElite2ServiceTest {

  private val prisonApiService: PrisonApiService = mock()
  private val service = LocationGroupFromPrisonApiService(prisonApiService)

  private val cellA1: Location =
    aLocation(locationId = -320L, locationType = "CELL", description = "LEI-A-1-001", parentLocationId = -32L)
  private val cellAA1: Location =
    aLocation(locationId = -320L, locationType = "CELL", description = "LEI-AA-1-001", parentLocationId = -32L)
  private val cellA3: Location =
    aLocation(locationId = -320L, locationType = "CELL", description = "LEI-A-3-001", parentLocationId = -32L)
  private val cellB1: Location =
    aLocation(locationId = -320L, locationType = "CELL", description = "LEI-B-2-001", parentLocationId = -32L)

  @Test
  fun locationGroupFilters() {
    val filter = service.locationGroupFilter("LEI", "A")
    assertThat(listOf(cellA1, cellA3, cellB1, cellAA1).filter(filter::test))
      .containsExactlyInAnyOrder(cellA1, cellA3)
  }

  private fun aLocation(locationId: Long, locationType: String, description: String, parentLocationId: Long): Location {
    return Location(
      locationId = locationId,
      locationType = locationType,
      description = description,
      locationUsage = "",
      agencyId = "",
      parentLocationId = parentLocationId,
      currentOccupancy = 0,
      locationPrefix = description,
      operationalCapacity = 0,
      userDescription = "",
      internalLocationCode = "",
    )
  }
}
