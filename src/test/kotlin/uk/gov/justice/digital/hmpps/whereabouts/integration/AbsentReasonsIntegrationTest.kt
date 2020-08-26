package uk.gov.justice.digital.hmpps.whereabouts.integration

import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.whereabouts.dto.AbsentReasonsDto
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason

class AbsentReasonsIntegrationTest : IntegrationTest() {
  @Test
  fun `should return the correct absent reasons`() {
    val paidReasons = setOf(
        AbsentReason.AcceptableAbsence,
        AbsentReason.ApprovedCourse,
        AbsentReason.NotRequired
    )
    val unpaidReasons = setOf(
        AbsentReason.SessionCancelled,
        AbsentReason.RestDay,
        AbsentReason.UnacceptableAbsence,
        AbsentReason.Refused,
        AbsentReason.RestInCellOrSick,
        AbsentReason.RefusedIncentiveLevelWarning
    )

    val triggersIEPWarnings = setOf(
        AbsentReason.RefusedIncentiveLevelWarning,
        AbsentReason.UnacceptableAbsence
    )

    val expected = AbsentReasonsDto(paidReasons, unpaidReasons, triggersIEPWarnings)

    webTestClient.get()
        .uri("/absence-reasons")
        .headers(setHeaders())
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json(gson.toJson(expected))
  }
}

