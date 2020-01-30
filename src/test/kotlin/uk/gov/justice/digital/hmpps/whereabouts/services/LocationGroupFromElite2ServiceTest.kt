package uk.gov.justice.digital.hmpps.whereabouts.services

import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import uk.gov.justice.digital.hmpps.whereabouts.model.Location

class LocationGroupFromElite2ServiceTest {

  private val elite2ApiService: Elite2ApiService = mock()
  private val service = LocationGroupFromEliteService(elite2ApiService)

  private val CELL_A_1: Location = Location.builder().locationId(-320L).locationType("CELL").description("LEI-A-1-001").parentLocationId(-32L).build()
  private val CELL_AA_1: Location = Location.builder().locationId(-320L).locationType("CELL").description("LEI-AA-1-001").parentLocationId(-32L).build()
  private val CELL_A_3: Location = Location.builder().locationId(-320L).locationType("CELL").description("LEI-A-3-001").parentLocationId(-32L).build()
  private val CELL_B_1: Location = Location.builder().locationId(-320L).locationType("CELL").description("LEI-B-2-001").parentLocationId(-32L).build()

  @Test
    fun locationGroupFilters() {
      listOf(CELL_A_1, CELL_A_3, CELL_B_1, CELL_AA_1).map { it.locationPrefix = it.description }
      val filter = service.locationGroupFilter("LEI", "A")
      assertThat(listOf(CELL_A_1, CELL_A_3, CELL_B_1, CELL_AA_1).filter(filter::test))
          .containsExactlyInAnyOrder(CELL_A_1, CELL_A_3)
    }

}