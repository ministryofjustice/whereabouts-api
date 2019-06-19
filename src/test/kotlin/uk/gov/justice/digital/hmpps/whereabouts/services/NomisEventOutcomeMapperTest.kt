package uk.gov.justice.digital.hmpps.whereabouts.services

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
                eventOutcomeMapper.getEventOutcome(reason, false, true, null)
            }.hasMessage(String.format("%s is not a valid paid reason", reason))
        }
    }

    @Test
    fun `should throw an exception when a paid reason is used for an unpaid attendance`() {

        val unpaidReasons = AbsentReason.getPaidReasons()

        unpaidReasons.forEach {
            val reason = it

            assertThatThrownBy {
                eventOutcomeMapper.getEventOutcome(reason, false, false, null)
            }.hasMessage(String.format("%s is not a valid unpaid reason", reason))
        }
    }

    @Test
    fun `should throw an exception when an absent reason is supplied for a a valid attendance`() {
        assertThatThrownBy {
            eventOutcomeMapper.getEventOutcome(AbsentReason.SessionCancelled, true, true, null)
        }.hasMessage("An absent reason was supplied for a valid attendance")
    }

    @Test
    fun `should make absent reason required when attendance is not set`() {
        assertThatThrownBy {
            eventOutcomeMapper.getEventOutcome(null, false, true, null)
        }.hasMessage("An absent reason was not supplied")
    }
}
