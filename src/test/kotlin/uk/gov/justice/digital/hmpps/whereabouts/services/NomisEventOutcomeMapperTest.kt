package uk.gov.justice.digital.hmpps.whereabouts.services

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.Test
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason

class NomisEventOutcomeMapperTest {
    private val eventOutcomeMapper = NomisEventOutcomeMapper()

    @Test
    fun `should throw an exception when a unpaid reason is used for a paid attendance`() {
        val unpaidReasons = AbsentReason.getUnpaidReasons()

        unpaidReasons.forEach {
            val reason = it
            try {
                eventOutcomeMapper.getEventOutcome(reason, false, true)
                triggerFail()
            } catch (e: IllegalArgumentException) {
                assertThat(e.message).isEqualTo(String.format("%s is not a valid paid reason", reason))
            }
        }
    }

    @Test
    fun `should throw an exception when a paid reason is used for an unpaid attendance`() {
        val unpaidReasons = AbsentReason.getPaidReasons()

        unpaidReasons.forEach {
            val reason = it
            try {
                eventOutcomeMapper.getEventOutcome(reason, false, false)
                triggerFail()
            } catch (e: IllegalArgumentException) {
                assertThat(e.message).isEqualTo(String.format("%s is not a valid unpaid reason", reason))
            }
        }
    }

    @Test
    fun `should throw an exception when an absent reason is supplied for a a valid attendance`() {
        try {
            eventOutcomeMapper.getEventOutcome(AbsentReason.SessionCancelled, true, true)
            triggerFail()
        } catch (e: IllegalArgumentException) {
            assertThat(e.message).isEqualTo("An absent reason was supplied for a valid attendance")
        }
    }

    @Test
    fun `should make absent reason required when attendance is not set`() {
        try {
            eventOutcomeMapper.getEventOutcome(null, false, true)
            triggerFail()
        } catch (e: IllegalArgumentException) {
            assertThat(e.message).isEqualTo("An absent reason was not supplied")
        }
    }

    fun triggerFail() {
        fail("should of thrown an IllegalArgumentException")
    }

}
