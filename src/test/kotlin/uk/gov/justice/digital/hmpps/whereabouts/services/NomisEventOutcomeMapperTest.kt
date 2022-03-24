package uk.gov.justice.digital.hmpps.whereabouts.services

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason.Companion.paidReasons
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason.Companion.unpaidReasons
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentSubReason

class NomisEventOutcomeMapperTest {
  private val eventOutcomeMapper = NomisEventOutcomeMapper()

  companion object {
    @JvmStatic
    private fun getUnpaidReasons() = unpaidReasons
    @JvmStatic
    private fun getPaidReasons() = paidReasons
  }

  @ParameterizedTest
  @MethodSource("getUnpaidReasons")
  fun `should throw an exception when a unpaid reason is used for a paid attendance`(reason: AbsentReason) {
    assertThatThrownBy {
      eventOutcomeMapper.getEventOutcome(reason, subReason = null, attended = false, paid = true, comment = null)
    }.hasMessage("$reason is not a valid paid reason")
  }

  @ParameterizedTest
  @MethodSource("getPaidReasons")
  fun `should throw an exception when a paid reason is used for an unpaid attendance`(reason: AbsentReason) {
    assertThatThrownBy {
      eventOutcomeMapper.getEventOutcome(reason, subReason = null, attended = false, paid = false, comment = null)
    }.hasMessage("$reason is not a valid unpaid reason")
  }

  @Test
  fun `should throw an exception when an absent reason is supplied for a a valid attendance`() {
    assertThatThrownBy {
      eventOutcomeMapper.getEventOutcome(AbsentReason.SessionCancelled, subReason = null, attended = true, paid = true, comment = null)
    }.hasMessage("An absent reason was supplied for a valid attendance")
  }

  @Test
  fun `should make absent reason required' when attendance is not set`() {
    assertThatThrownBy {
      eventOutcomeMapper.getEventOutcome(null, subReason = null, false, paid = true, comment = null)
    }.hasMessage("An absent reason was not supplied")
  }

  @Test
  fun `should return 'ATT' and 'STANDARD' outcome`() {
    val nomisOutcome = eventOutcomeMapper.getEventOutcome(null, subReason = null, true, true, "hello")

    assertThat(nomisOutcome).isEqualTo(EventOutcome("ATT", "STANDARD", "hello"))
  }

  @Test
  fun `should return 'ACCAB' for 'AcceptableAbsence'`() {
    val nomisOutcome = eventOutcomeMapper.getEventOutcome(AbsentReason.AcceptableAbsence, subReason = null, false, true, "hello")

    assertThat(nomisOutcome).isEqualTo(EventOutcome("ACCAB", null, "hello"))
  }

  @Test
  fun `should return 'NREQ' for 'NotRequired'`() {
    val nomisOutcome = eventOutcomeMapper.getEventOutcome(AbsentReason.NotRequired, subReason = null, false, true, "hello")

    assertThat(nomisOutcome).isEqualTo(EventOutcome("NREQ", null, "hello"))
  }

  @Test
  fun `should return 'CANC' for 'SessionCancelled'`() {
    val nomisOutcome = eventOutcomeMapper.getEventOutcome(AbsentReason.SessionCancelled, subReason = null, false, false, "hello")

    assertThat(nomisOutcome).isEqualTo(EventOutcome("CANC", null, "hello"))
  }

  @Test
  fun `should return 'REST' for 'RestInCellOrSick`() {
    val nomisOutcome = eventOutcomeMapper.getEventOutcome(AbsentReason.RestInCellOrSick, subReason = null, false, false, "hello")

    assertThat(nomisOutcome).isEqualTo(EventOutcome("REST", null, "hello"))
  }

  @Test
  fun `should return 'REST' for 'RestDay'`() {
    val nomisOutcome = eventOutcomeMapper.getEventOutcome(AbsentReason.RestDay, subReason = null, false, false, "hello")

    assertThat(nomisOutcome).isEqualTo(EventOutcome("REST", null, "hello"))
  }

  @Test
  fun `should return 'UNACAB' for 'Refused'`() {
    val nomisOutcome = eventOutcomeMapper.getEventOutcome(AbsentReason.Refused, subReason = null, false, false, "hello")

    assertThat(nomisOutcome).isEqualTo(EventOutcome("UNACAB", null, "hello"))
  }

  @Test
  fun `should return 'UNACAB' for 'UnacceptableAbsence'`() {
    val nomisOutcome = eventOutcomeMapper.getEventOutcome(AbsentReason.UnacceptableAbsence, subReason = null, false, false, "hello")

    assertThat(nomisOutcome).isEqualTo(EventOutcome("UNACAB", null, "hello"))
  }

  @Test
  fun `should return 'ACCAB' for 'ApprovedCourse'`() {
    val nomisOutcome = eventOutcomeMapper.getEventOutcome(AbsentReason.ApprovedCourse, subReason = null, false, true, "hello")

    assertThat(nomisOutcome).isEqualTo(EventOutcome("ACCAB", null, "hello"))
  }

  @Test
  fun `should return 'UNACAB' for 'RefusedIncentiveLevelWarning'`() {
    val nomisOutcome =
      eventOutcomeMapper.getEventOutcome(AbsentReason.RefusedIncentiveLevelWarning, subReason = null, false, false, "hello")

    assertThat(nomisOutcome).isEqualTo(EventOutcome("UNACAB", null, "hello"))
  }

  @Test
  fun `should throw an exception when behaviour is supplied for a paid attendance`() {
    assertThatThrownBy {
      eventOutcomeMapper.getEventOutcome(AbsentReason.AcceptableAbsence, subReason = AbsentSubReason.Behaviour, attended = false, paid = true, comment = null)
    }.hasMessage("Behaviour is not a valid paid sub reason")
  }

  @Test
  fun `should add the sub reason to the comment`() {
    val nomisOutcome =
      eventOutcomeMapper.getEventOutcome(AbsentReason.RefusedIncentiveLevelWarning, subReason = AbsentSubReason.ExternalMoves, false, false, "hello")

    assertThat(nomisOutcome).isEqualTo(EventOutcome("UNACAB", null, "External moves. hello"))
  }

  @Test
  fun `should truncate the comment if sub reason added and comment with sub reason more than 240 characters`() {
    val longComment = "hello".repeat(240 / 5)
    val nomisOutcome =
      eventOutcomeMapper.getEventOutcome(AbsentReason.RefusedIncentiveLevelWarning, subReason = AbsentSubReason.ExternalMoves, false, false, longComment)

    assertThat(nomisOutcome).isEqualTo(EventOutcome("UNACAB", null, "External moves. ${"hello".repeat(240 / 5 - 4)}hell"))
  }
}
