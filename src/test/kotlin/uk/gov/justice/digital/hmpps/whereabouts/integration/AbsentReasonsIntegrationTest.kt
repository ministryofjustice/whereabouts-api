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
        val response: ResponseEntity<String > =
                restTemplate?.exchange("/attendance/absence-reasons", HttpMethod.GET, createHeaderEntity("headers"))!!

        val result: Set<Map<*,*>> = gson.fromJson(response.body,  object: TypeToken<Set<Map<*,*>>>(){}.type)

        assertThat(result).containsAnyElementsOf(setOf(
                mapOf("id" to 1.0, "reason" to "Acceptable absence", "paidReason" to true),
                mapOf("id" to 2.0, "reason" to "Not required", "paidReason" to true),
                mapOf("id" to 8.0, "reason" to "Refused", "paidReason" to false),
                mapOf("id" to 6.0, "reason" to "Rest Day", "paidReason" to false),
                mapOf("id" to 4.0, "reason" to "Rest in cell", "paidReason" to false),
                mapOf("id" to 3.0, "reason" to "Session Cancelled", "paidReason" to false),
                mapOf("id" to 5.0, "reason" to "Sick", "paidReason" to false),
                mapOf("id" to 7.0, "reason" to "Unacceptable absence", "paidReason" to false)
        ))

    }
}

