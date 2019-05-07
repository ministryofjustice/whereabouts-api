package uk.gov.justice.digital.hmpps.whereabouts.integration

import com.google.gson.reflect.TypeToken
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity

class AbsentReasonsIntegrationTest : IntegrationTest() {

    @Test
    fun `should return the correct absent reasons`() {
        val response: ResponseEntity<String> =
                restTemplate.exchange("/attendance/absence-reasons", HttpMethod.GET, createHeaderEntity("headers"))

        val result: List<String> = gson.fromJson(response.body, object : TypeToken<List<String>>() {}.type)

        assertThat(result).containsAnyElementsOf(setOf(
                "Acceptable absence",
                "Not required",
                "Refused",
                "Rest Day",
                "Rest in cell",
                "Session Cancelled",
                "Sick", "paidReason",
                "Unacceptable absence"))
    }

}

