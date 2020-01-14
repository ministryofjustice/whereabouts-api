package uk.gov.justice.digital.hmpps.whereabouts.services

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason

class NomisEventOutcomeMapperTest {
  private val eventOutcomeMapper = NomisEventOutcomeMapper()

  @Test
  fun `should throw an exception when a unpaid reason is used for a paid attendance`() {
    val unpaidReasons = AbsentReason.getUnpaidReasons()

    unpaidReasons.forEach {
      val reason = it

      assertThatThrownBy {
        eventOutcomeMapper.getEventOutcome(reason, attended = false, paid = true, comment = null)
      }.hasMessage(String.format("%s is not a valid paid reason", reason))
    }
  }

  @Test
  fun `should throw an exception when a paid reason is used for an unpaid attendance`() {

    val unpaidReasons = AbsentReason.getPaidReasons()

    unpaidReasons.forEach {
      val reason = it

      assertThatThrownBy {
        eventOutcomeMapper.getEventOutcome(reason, attended = false, paid = false, comment = null)
      }.hasMessage(String.format("%s is not a valid unpaid reason", reason))
    }
  }

  @Test
  fun `should throw an exception when an absent reason is supplied for a a valid attendance`() {
    assertThatThrownBy {
      eventOutcomeMapper.getEventOutcome(AbsentReason.SessionCancelled, attended = true, paid = true, comment = null)
    }.hasMessage("An absent reason was supplied for a valid attendance")
  }

  @Test
  fun `should make absent reason required' when attendance is not set`() {
    assertThatThrownBy {
      eventOutcomeMapper.getEventOutcome(null, false, paid = true, comment = null)
    }.hasMessage("An absent reason was not supplied")
  }

  @Test
  fun `should return 'ATT' and 'STANDARD' outcome`() {
    val nomisOutcome = eventOutcomeMapper.getEventOutcome(null, true, true, "hello")

    assertThat(nomisOutcome).isEqualTo(EventOutcome("ATT", "STANDARD", "hello"))
  }


  @Test
  fun `should return 'ACCAB' for 'AcceptableAbsence'`() {
    val nomisOutcome = eventOutcomeMapper.getEventOutcome(AbsentReason.AcceptableAbsence, false, true, "hello")

    assertThat(nomisOutcome).isEqualTo(EventOutcome("ACCAB", null, "hello"))
  }

  @Test
  fun `should return 'NREQ' for 'NotRequired'`() {
    val nomisOutcome = eventOutcomeMapper.getEventOutcome(AbsentReason.NotRequired, false, true, "hello")

    assertThat(nomisOutcome).isEqualTo(EventOutcome("NREQ", null, "hello"))
  }

  @Test
  fun `should return 'CANC' for 'SessionCancelled'`() {
    val nomisOutcome = eventOutcomeMapper.getEventOutcome(AbsentReason.SessionCancelled, false, false, "hello")

    assertThat(nomisOutcome).isEqualTo(EventOutcome("CANC", null, "hello"))
  }

  @Test
  fun `should return 'REST' for 'RestInCell`() {
    val nomisOutcome = eventOutcomeMapper.getEventOutcome(AbsentReason.RestInCell, false, false, "hello")

    assertThat(nomisOutcome).isEqualTo(EventOutcome("REST", null, "hello"))
  }

  @Test
  fun `should return 'REST' for 'Sick'`() {
    val nomisOutcome = eventOutcomeMapper.getEventOutcome(AbsentReason.Sick, false, false, "hello")

    assertThat(nomisOutcome).isEqualTo(EventOutcome("REST", null, "hello"))
  }

  @Test
  fun `should return 'REST' for 'RestDay'`() {
    val nomisOutcome = eventOutcomeMapper.getEventOutcome(AbsentReason.RestDay, false, false, "hello")

    assertThat(nomisOutcome).isEqualTo(EventOutcome("REST", null, "hello"))
  }

  @Test
  fun `should return 'UNACAB' for 'Refused'`() {
    val nomisOutcome = eventOutcomeMapper.getEventOutcome(AbsentReason.Refused, false, false, "hello")

    assertThat(nomisOutcome).isEqualTo(EventOutcome("UNACAB", null, "hello"))
  }

  @Test
  fun `should return 'UNACAB' for 'UnacceptableAbsence'`() {
    val nomisOutcome = eventOutcomeMapper.getEventOutcome(AbsentReason.UnacceptableAbsence, false, false, "hello")

    assertThat(nomisOutcome).isEqualTo(EventOutcome("UNACAB", null, "hello"))
  }

  @Test
  fun `should return 'ACCAB' for 'ApprovedCourse'`() {
    val nomisOutcome = eventOutcomeMapper.getEventOutcome(AbsentReason.ApprovedCourse, false, true, "hello")

    assertThat(nomisOutcome).isEqualTo(EventOutcome("ACCAB", null, "hello"))
  }

  @Test
  fun `should return 'UNACAB' for  'RefusedIncentiveLevelWarning`() {
    val nomisOutcome = eventOutcomeMapper.getEventOutcome(AbsentReason.RefusedIncentiveLevelWarning, false, false, "hello")

    assertThat(nomisOutcome).isEqualTo(EventOutcome("UNACAB", null, "hello"))
  }

}
