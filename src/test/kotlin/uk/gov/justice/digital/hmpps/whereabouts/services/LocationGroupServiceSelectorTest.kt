package uk.gov.justice.digital.hmpps.whereabouts.services

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner.StrictStubs
import uk.gov.justice.digital.hmpps.whereabouts.model.LocationGroup

@RunWith(StrictStubs::class)
class LocationGroupServiceSelectorTest {

  private val defaultService: LocationGroupService = mock()
  private val overrideService: LocationGroupService = mock()
  private var service: LocationGroupService = LocationGroupServiceSelector(defaultService, overrideService)

  @Test
  fun locationGroupsCallsDefaultWhenNoOverride() {
      `when`(defaultService.getLocationGroups("LEI")).thenReturn(listOf(LG1))
      assertThat(service.getLocationGroups("LEI")).contains(LG1)
      verify(overrideService).getLocationGroups("LEI")
    }

  @Test
  fun locationGroupsDoesNotCallDefaultWhenOverridden() {
      `when`(overrideService.getLocationGroups("LEI")).thenReturn(listOf(LG1))
      assertThat(service.getLocationGroups("LEI")).contains(LG1)
      verify(overrideService).getLocationGroups("LEI")
      verifyNoMoreInteractions(defaultService)
    }

  @Test
  fun locationGroupsForAgencyDelegatesToGetLocationGroups() {
      `when`(defaultService.getLocationGroups("LEI")).thenReturn(listOf(LG1))
      assertThat(service.getLocationGroupsForAgency("LEI")).contains(LG1)
      verify(overrideService).getLocationGroups("LEI")
    }

  @Test
  fun locationGroupsFiltersCallsDefaultWhenNoOverride() {
    service.locationGroupFilter("LEI", "Z")
    verify(defaultService).locationGroupFilter("LEI", "Z")
  }

  @Test
  fun locationGroupsFiltersCallsOverrideOnlyIfOverridden() {
    `when`(overrideService.getLocationGroups("LEI")).thenReturn(listOf(LG1))
    service.locationGroupFilter("LEI", "Z")
    verify(overrideService).locationGroupFilter("LEI", "Z")
    verifyNoMoreInteractions(defaultService)
  }

  companion object {
    private val LG1 = LocationGroup.builder().key("A").name("A").build()
  }
}