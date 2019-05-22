package uk.gov.justice.digital.hmpps.whereabouts.integration

import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.google.gson.reflect.TypeToken
import org.assertj.core.api.Assertions.assertThat
import org.junit.ClassRule
import org.junit.Test
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import uk.gov.justice.digital.hmpps.whereabouts.dto.AbsentReasonsDto
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason

class AbsentReasonsIntegrationTest : IntegrationTest() {
    companion object {
        @get:ClassRule
        @JvmStatic
        val elite2MockServer = WireMockRule(8999)
    }

    @Test
    fun `should return the correct absent reasons`() {
        val response: ResponseEntity<String> =
                restTemplate.exchange("/attendance/absence-reasons", HttpMethod.GET, createHeaderEntity("headers"))

        val result: AbsentReasonsDto = gson.fromJson(response.body, object : TypeToken<AbsentReasonsDto>() {}.type)

        val paidReasons = setOf(
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

        val expected = AbsentReasonsDto(paidReasons, unpaidReasons)

        assertThat(result).isEqualTo(expected)
    }

}

