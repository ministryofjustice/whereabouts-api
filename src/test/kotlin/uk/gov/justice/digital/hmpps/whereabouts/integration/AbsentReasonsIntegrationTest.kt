package uk.gov.justice.digital.hmpps.whereabouts.integration

import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.whereabouts.dto.attendance.AbsentReasonDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.attendance.AbsentReasonsDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.attendance.AbsentReasonsV2Dto
import uk.gov.justice.digital.hmpps.whereabouts.dto.attendance.AbsentSubReasonDto
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentSubReason

class AbsentReasonsIntegrationTest : IntegrationTest() {
  @Test
  fun `should return the correct absent reasons`() {
    val paidReasons = setOf(
      AbsentReason.AcceptableAbsence,
      AbsentReason.ApprovedCourse,
      AbsentReason.NotRequired,
    )
    val unpaidReasons = setOf(
      AbsentReason.SessionCancelled,
      AbsentReason.RestDay,
      AbsentReason.UnacceptableAbsence,
      AbsentReason.UnacceptableAbsenceIncentiveLevelWarning,
      AbsentReason.Refused,
      AbsentReason.RestInCellOrSick,
      AbsentReason.RefusedIncentiveLevelWarning,
    )

    val triggersIEPWarnings = setOf(
      AbsentReason.RefusedIncentiveLevelWarning,
      AbsentReason.UnacceptableAbsenceIncentiveLevelWarning,
    )

    val expected = AbsentReasonsDto(
      paidReasons,
      unpaidReasons,
      triggersIEPWarnings,
    )

    webTestClient.get()
      .uri("/absence-reasons")
      .headers(setHeaders())
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .json(objectMapper.writeValueAsString(expected))
  }

  @Test
  fun `should return the correct absent reasons v2`() {
    val paidReasons = listOf(
      AbsentReason.AcceptableAbsence,
      AbsentReason.ApprovedCourse,
      AbsentReason.NotRequired,
    ).map { AbsentReasonDto(it, it.label) }
    val unpaidReasons = listOf(
      AbsentReason.SessionCancelled,
      AbsentReason.RestDay,
      AbsentReason.UnacceptableAbsence,
      AbsentReason.UnacceptableAbsenceIncentiveLevelWarning,
      AbsentReason.Refused,
      AbsentReason.RestInCellOrSick,
      AbsentReason.RefusedIncentiveLevelWarning,
    ).map { AbsentReasonDto(it, it.label) }

    val triggersIEPWarnings = listOf(
      AbsentReason.RefusedIncentiveLevelWarning,
      AbsentReason.UnacceptableAbsenceIncentiveLevelWarning,
    )
    val triggersAbsentSubReason = listOf(
      AbsentReason.AcceptableAbsence,
      AbsentReason.Refused,
      AbsentReason.RefusedIncentiveLevelWarning,
      AbsentReason.SessionCancelled,
      AbsentReason.UnacceptableAbsence,
      AbsentReason.UnacceptableAbsenceIncentiveLevelWarning,
    )
    val paidSubReasons = AbsentSubReason.values().toList()
      .filter { it != AbsentSubReason.Behaviour }
      .map { AbsentSubReasonDto(it, it.label) }
    val unpaidSubReasons = AbsentSubReason.values().toList()
      .map { AbsentSubReasonDto(it, it.label) }

    val expected = AbsentReasonsV2Dto(
      paidReasons,
      unpaidReasons,
      triggersIEPWarnings,
      triggersAbsentSubReason,
      paidSubReasons,
      unpaidSubReasons,
    )

    webTestClient.get()
      .uri("/absence-reasons/v2")
      .headers(setHeaders())
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .json(objectMapper.writeValueAsString(expected))
  }
}
