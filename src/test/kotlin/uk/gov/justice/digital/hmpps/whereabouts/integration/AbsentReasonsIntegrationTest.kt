package uk.gov.justice.digital.hmpps.whereabouts.integration

import com.google.gson.reflect.TypeToken
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import uk.gov.justice.digital.hmpps.whereabouts.dto.AbsentReasonsDto
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason

class AbsentReasonsIntegrationTest : IntegrationTest() {
  @Test
  fun `should return the correct absent reasons old`() {
    val response: ResponseEntity<String> =
        restTemplate.exchange("/attendance/absence-reasons", HttpMethod.GET, createHeaderEntity("headers"))

    val result: AbsentReasonsDto = gson.fromJson(response.body, object : TypeToken<AbsentReasonsDto>() {}.type)

    val paidReasons = setOf(
        AbsentReason.ApprovedCourse,
        AbsentReason.AcceptableAbsence,
        AbsentReason.NotRequired
    )
    val unpaidReasons = setOf(
        AbsentReason.SessionCancelled,
        AbsentReason.RestInCell,
        AbsentReason.RestDay,
        AbsentReason.UnacceptableAbsence,
        AbsentReason.Refused,
        AbsentReason.Sick
    )
    val triggersIEPWarnings = setOf(
        AbsentReason.Refused,
        AbsentReason.UnacceptableAbsence
    )

    val expected = AbsentReasonsDto(paidReasons, unpaidReasons, triggersIEPWarnings)

    assertThat(result).isEqualTo(expected)
  }

  @Test
  fun `should return the correct absent reasons`() {
    val response: ResponseEntity<String> =
        restTemplate.exchange("/absence-reasons", HttpMethod.GET, createHeaderEntity("headers"))

    val result: AbsentReasonsDto = gson.fromJson(response.body, object : TypeToken<AbsentReasonsDto>() {}.type)

    val paidReasons = setOf(
        AbsentReason.ApprovedCourse,
        AbsentReason.AcceptableAbsence,
        AbsentReason.NotRequired
    )
    val unpaidReasons = setOf(
        AbsentReason.SessionCancelled,
        AbsentReason.RestInCell,
        AbsentReason.RestDay,
        AbsentReason.UnacceptableAbsence,
        AbsentReason.Refused,
        AbsentReason.Sick
    )
    val triggersIEPWarnings = setOf(
        AbsentReason.Refused,
        AbsentReason.UnacceptableAbsence
    )

    val expected = AbsentReasonsDto(paidReasons, unpaidReasons, triggersIEPWarnings)

    assertThat(result).isEqualTo(expected)
  }

}

