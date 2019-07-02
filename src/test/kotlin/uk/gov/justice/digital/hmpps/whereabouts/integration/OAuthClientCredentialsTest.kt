package uk.gov.justice.digital.hmpps.whereabouts.integration

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.http.HttpHeaders
import com.github.tomakehurst.wiremock.junit.WireMockRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.ClassRule
import org.junit.Test
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import uk.gov.justice.digital.hmpps.whereabouts.dto.CreateAttendanceDto
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod
import java.time.LocalDate

class OAuthClientCredentialsTest : IntegrationTest() {
    companion object {
        @get:ClassRule
        @JvmStatic
        val elite2MockServer = WireMockRule(8999)

        @get:ClassRule
        @JvmStatic
        val oauthMockServer = WireMockRule(8090)
    }

    init {
        val bookingId = 1
        val activityId = 1

        elite2MockServer.stubFor(
                WireMock.put(WireMock.urlPathEqualTo("/api/bookings/$bookingId/activities/$activityId/attendance"))
                        .willReturn(WireMock.aResponse()
                                .withStatus(200)))

        oauthMockServer.stubFor(
                WireMock.post(WireMock.urlEqualTo("/auth/oauth/token"))
                        .willReturn(WireMock.aResponse()
                                .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
                                .withBody(gson.toJson(mapOf("access_token" to "ABCDE"))))
        )
    }

    @Test
    fun `should request a new auth token for each new incoming request`() {
        postAttendance()
        postAttendance()

        oauthMockServer.verify(2, WireMock.postRequestedFor(WireMock.urlEqualTo("/auth/oauth/token")))
    }


    private fun postAttendance() {
        val attendanceDto =
                CreateAttendanceDto
                        .builder()
                        .prisonId("LEI")
                        .attended(true)
                        .paid(true)
                        .bookingId(1)
                        .eventId(1)
                        .eventLocationId(1)
                        .period(TimePeriod.AM)
                        .eventDate(LocalDate.now())
                        .build()

        val response: ResponseEntity<String> =
                restTemplate.exchange("/attendance", HttpMethod.POST, createHeaderEntity(attendanceDto))

        assertThat(response.statusCodeValue).isEqualTo(201)
    }

}
