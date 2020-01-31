package uk.gov.justice.digital.hmpps.whereabouts.services

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import uk.gov.justice.digital.hmpps.whereabouts.model.LocationGroup

class WhereaboutsEnabledServiceTest {

  private val locationGroupService: LocationGroupService = mock()
  private val enabledAgencies = setOf("MDI")
  private val whereaboutsEnabledService = WhereaboutsEnabledService(locationGroupService, enabledAgencies)

  @Test
  fun `isEnabled - no location groups and not enabled - false`() {
    whenever(locationGroupService.getLocationGroups(anyString())).thenReturn(emptyList())

    assertThat(whereaboutsEnabledService.isEnabled("any agency")).isFalse()
  }

  @Test
  fun `isEnabled - no location groups and enabled - true`() {
    whenever(locationGroupService.getLocationGroups(anyString())).thenReturn(emptyList())

    assertThat(whereaboutsEnabledService.isEnabled("MDI")).isTrue()
  }

  @Test
  fun `isEnabled - location groups and not enabled - true`() {
    whenever(locationGroupService.getLocationGroups(anyString())).thenReturn(listOf(LocationGroup("any name", "any key", emptyList())))

    assertThat(whereaboutsEnabledService.isEnabled("not enabled agency")).isTrue()
  }

  @Test
  fun `isEnabled - location groups and enabled - true`() {
    whenever(locationGroupService.getLocationGroups(anyString())).thenReturn(listOf(LocationGroup("any name", "any key", emptyList())))

    assertThat(whereaboutsEnabledService.isEnabled("MDI")).isTrue()
  }

}