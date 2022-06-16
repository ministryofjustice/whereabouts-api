package uk.gov.justice.digital.hmpps.whereabouts.model

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class AttendanceChangeValuesTest {

  companion object {
    @JvmStatic
    private fun getAbsentReasons() = AbsentReason.values()
  }

  @ParameterizedTest
  @MethodSource("getAbsentReasons")
  fun `should map from absent reason to attendance change values`(reason: AbsentReason) {
    AttendanceChangeValues.valueOf(reason.toString())
  }
}
