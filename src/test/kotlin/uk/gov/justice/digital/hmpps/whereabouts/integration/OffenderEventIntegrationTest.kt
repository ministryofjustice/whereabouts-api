package uk.gov.justice.digital.hmpps.whereabouts.integration

import com.google.common.collect.Lists
import com.google.gson.reflect.TypeToken
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import uk.gov.justice.digital.hmpps.whereabouts.dto.EventDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.OffenderEventDto
import uk.gov.justice.digital.hmpps.whereabouts.model.EventType
import java.time.LocalDate


class OffenderEventIntegrationTest : IntegrationTest() {

    @Test
    fun `Offender Event record can be recorded`() {
        val response: ResponseEntity<String> =
                restTemplate.exchange("/whereabouts/offender-event", HttpMethod.POST,
                        createHeaderEntity(getOffenderEvent(123L, EventType.APP.toString())))!!

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `Offender Event records can be retrieved for a list of events`() {
       restTemplate.exchange< ResponseEntity<String>>("/whereabouts/offender-event", HttpMethod.POST, createHeaderEntity(getOffenderEvent(1L, "APP")))
        restTemplate.exchange< ResponseEntity<String>>("/whereabouts/offender-event", HttpMethod.POST, createHeaderEntity(getOffenderEvent(1L, "VISIT")))
        restTemplate.exchange< ResponseEntity<String>>("/whereabouts/offender-event", HttpMethod.POST, createHeaderEntity(getOffenderEvent(2L, "VISIT")))

        val offenderEventResponse = restTemplate!!.exchange("/whereabouts/LEI/offender-event", HttpMethod.POST, createHeaderEntity(Lists.newArrayList(
                EventDto(1L, "APP"),
                EventDto(2L, "VISIT"))), String::class.java)!!


        val eventsList: List<OffenderEventDto> = gson.fromJson(offenderEventResponse.body!!,
                object: TypeToken<List<OffenderEventDto>>(){}.type)

        assertThat(eventsList.count()).isEqualTo(2)
        assertThat(offenderEventResponse.statusCode).isEqualTo(HttpStatus.OK)
    }

    fun getOffenderEvent(eventId: Long?, eventType: String): OffenderEventDto {
        return OffenderEventDto.builder()
                .currentLocation(java.lang.Boolean.TRUE)
                .offenderNo("123")
                .eventId(eventId)
                .eventType(eventType)
                .period("PM")
                .prisonId("LEI")
                .eventDate(LocalDate.of(2018, 7, 19))
                .build()

    }
}

