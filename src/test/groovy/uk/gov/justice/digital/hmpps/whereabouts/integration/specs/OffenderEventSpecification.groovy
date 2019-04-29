package uk.gov.justice.digital.hmpps.whereabouts.integration.specs

import groovy.json.JsonSlurper
import org.assertj.core.util.Lists
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.whereabouts.dto.EventDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.OffenderEventDto

import java.time.LocalDate

class OffenderEventSpecification extends TestSpecification {

    def jsonSlurper = new JsonSlurper()

    def 'Offender Event record can be recorded'() {

        when:
        def response = restTemplate.exchange("/whereabouts/offender-event", HttpMethod.POST, createHeaderEntity(getOffenderEvent(123L, "APP")), String.class)

        then:
        response.statusCode == HttpStatus.OK  //should be created?
    }



    def 'Offender Event records can be retrieved for a list of events'() {

        given:
        restTemplate.exchange("/whereabouts/offender-event", HttpMethod.POST, createHeaderEntity(getOffenderEvent(1L, "APP")), String.class)
        restTemplate.exchange("/whereabouts/offender-event", HttpMethod.POST, createHeaderEntity(getOffenderEvent(1L, "VISIT")), String.class)
        restTemplate.exchange("/whereabouts/offender-event", HttpMethod.POST, createHeaderEntity(getOffenderEvent(2L, "VISIT")), String.class)


        when:
        def response = restTemplate.exchange("/whereabouts/LEI/offender-event", HttpMethod.POST, createHeaderEntity(Lists.newArrayList(
                new EventDto(1L, "APP"),
                new EventDto(2L, "VISIT"))), String.class);
        then:
        response.statusCode == HttpStatus.OK
        def eventList = jsonSlurper.parseText(response.body)
        eventList.size == 2;

    }


    def 'Offender event records can be retrieved for a specified event'() {

        given:
        restTemplate.exchange("/whereabouts/offender-event", HttpMethod.POST, createHeaderEntity(getOffenderEvent(1L, "APP")), String.class)
        restTemplate.exchange("/whereabouts/offender-event", HttpMethod.POST, createHeaderEntity(getOffenderEvent(2L, "APP")), String.class)

        when:
        def response = restTemplate.exchange("/whereabouts/LEI/offender-event/1/event-type/APP", HttpMethod.GET, createHeaderEntity("header"), String.class)

        then:
        response.statusCode == HttpStatus.OK
        def eventList = jsonSlurper.parseText(response.body)
        eventList.size == 1;
        eventList[0].period == 'PM'
        eventList[0].eventDate == '2018-07-19'
        eventList[0].eventType == 'APP'
        eventList[0].eventId == 1L
        eventList[0].offenderNo == '123'
        eventList[0].prisonId == 'LEI'
        eventList[0].currentLocation == true
    }

    OffenderEventDto getOffenderEvent(Long eventId, String eventType){
        return OffenderEventDto.builder()
                .currentLocation(Boolean.TRUE)
                .offenderNo("123")
                .eventId(eventId)
                .eventType(eventType)
                .period("PM")
                .prisonId("LEI")
                .eventDate(LocalDate.of(2018, 7, 19))
                .build();

    }
}



