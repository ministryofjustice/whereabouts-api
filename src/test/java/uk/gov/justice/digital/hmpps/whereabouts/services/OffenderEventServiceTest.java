package uk.gov.justice.digital.hmpps.whereabouts.services;


import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.digital.hmpps.whereabouts.dto.EventDto;
import uk.gov.justice.digital.hmpps.whereabouts.dto.OffenderEventDto;
import uk.gov.justice.digital.hmpps.whereabouts.model.EventType;
import uk.gov.justice.digital.hmpps.whereabouts.model.OffenderEvent;
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod;
import uk.gov.justice.digital.hmpps.whereabouts.repository.OffenderEventRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OffenderEventServiceTest {


    @Mock
    private OffenderEventRepository repository;

    private OffenderEventService offenderEventService;

    @Before
    public void setUp() {
        offenderEventService = new OffenderEventService(repository);
    }

    @Test
    public void createOffenderEvent() {
    }

    @Test
    public void getOffenderEventsByEvent() {
        List<OffenderEvent> oeList = new ArrayList<>();
        final OffenderEvent eventApp1 = getTestOffenderEvent(1L, 123L, EventType.APP);
        oeList.add(eventApp1);
        final OffenderEvent eventApp2 = getTestOffenderEvent(2L, 123L, EventType.APP);
        oeList.add(eventApp2);
        final OffenderEvent eventApp3 = getTestOffenderEvent(3L, 123L, EventType.APP);
        oeList.add(eventApp1);
        final OffenderEvent eventApp4 = getTestOffenderEvent(4L, 123L, EventType.APP);
        oeList.add(eventApp2);

        when(repository.findByPrisonIdAndEventIdAndEventType("LEI", 123L, EventType.APP)).thenReturn(oeList);

        final List<OffenderEventDto> resultList = offenderEventService.getOffenderEventsByEvent("LEI", 123L, EventType.APP.name());

        assertThat(resultList).containsExactly(convertToDto(eventApp1), convertToDto(eventApp2), convertToDto(eventApp3), convertToDto(eventApp4));
    }

    @Test
    public void getOffenderEventsByEventNoData() {

        when(repository.findByPrisonIdAndEventIdAndEventType("LEI", 123L, EventType.APP)).thenReturn(Lists.newArrayList());

        final List<OffenderEventDto> resultList = offenderEventService.getOffenderEventsByEvent("LEI", 123L, EventType.APP.name());

        assertThat(resultList).hasSize(0);
    }

    @Test
    public void getOffenderEventsByEventList() {
        List<OffenderEvent> oeList = new ArrayList<>();
        final OffenderEvent eventApp1 = getTestOffenderEvent(1L, 123L, EventType.APP);
        oeList.add(eventApp1);
        final OffenderEvent eventApp2 = getTestOffenderEvent(2L, 123L, EventType.APP);
        oeList.add(eventApp2);

        List<OffenderEvent> oeList2 = new ArrayList<>();
        final OffenderEvent eventVisit1 = getTestOffenderEvent(3L, 333L, EventType.VISIT);
        oeList2.add(eventVisit1);
        final OffenderEvent eventVisit2 = getTestOffenderEvent(4L, 333L, EventType.VISIT);
        oeList2.add(eventVisit2);

        when(repository.findByPrisonIdAndEventIdAndEventType("LEI", 123L, EventType.APP)).thenReturn(oeList);
        when(repository.findByPrisonIdAndEventIdAndEventType("LEI", 333L, EventType.VISIT)).thenReturn(oeList2);

        final List<OffenderEventDto> resultList = offenderEventService.getOffenderEventsByEventList("LEI",
                Lists.newArrayList(new EventDto(123L, EventType.APP.name()), new EventDto(333L, EventType.VISIT.name())) );

        assertThat(resultList).containsExactly(convertToDto(eventApp1), convertToDto(eventApp2), convertToDto(eventVisit1), convertToDto(eventVisit2));
    }

    @Test
    public void getOffenderEventsByEventListNoData() {

        when(repository.findByPrisonIdAndEventIdAndEventType("LEI", 123L, EventType.APP)).thenReturn(Lists.newArrayList());
        when(repository.findByPrisonIdAndEventIdAndEventType("LEI", 333L, EventType.VISIT)).thenReturn(Lists.newArrayList());

        final List<OffenderEventDto> resultList = offenderEventService.getOffenderEventsByEventList("LEI",
                Lists.newArrayList(new EventDto(123L, EventType.APP.name()), new EventDto(333L, EventType.VISIT.name())) );

        assertThat(resultList).hasSize(0);
    }

    private OffenderEvent getTestOffenderEvent(Long id, Long eventId, EventType type) {
        final OffenderEvent event = OffenderEvent.builder()
                .eventId(eventId)
                .eventType(type)
                .eventDate(LocalDate.of(2018, 5, 6))
                .offenderNo("123A")
                .prisonId("LEI")
                .currentLocation(false)
                .period(TimePeriod.ED)
                .build();
        event.setId(id);
        return event;
    }

    private OffenderEventDto convertToDto(OffenderEvent oe) {
        return OffenderEventDto.builder()
                .eventId(oe.getEventId())
                .eventType(oe.getEventType().name())
                .eventDate(oe.getEventDate())
                .offenderNo(oe.getOffenderNo())
                .prisonId(oe.getPrisonId())
                .currentLocation(oe.getCurrentLocation())
                .period(oe.getPeriod().name())
                .build();
    }
}
