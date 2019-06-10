package uk.gov.justice.digital.hmpps.whereabouts.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import uk.gov.justice.digital.hmpps.whereabouts.dto.EventDto;
import uk.gov.justice.digital.hmpps.whereabouts.dto.OffenderEventDto;
import uk.gov.justice.digital.hmpps.whereabouts.model.EventType;
import uk.gov.justice.digital.hmpps.whereabouts.model.OffenderEvent;
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod;
import uk.gov.justice.digital.hmpps.whereabouts.repository.OffenderEventRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@Validated
@Slf4j
public class OffenderEventService {

    private final OffenderEventRepository repository;

    @Autowired
    public OffenderEventService(OffenderEventRepository repository) {
        this.repository = repository;
    }


    public void createOffenderEvent(OffenderEventDto dto) {
        List<OffenderEvent> offenderEvent = repository.findByPrisonIdAndOffenderNoAndEventIdAndEventType(dto.getPrisonId(), dto.getOffenderNo(), dto.getEventId(), EventType.valueOf(dto.getEventType()));

        if (offenderEvent.isEmpty()) {
            // create a new event record
            repository.save(OffenderEvent.builder()
                    .prisonId(dto.getPrisonId())
                    .offenderNo(dto.getOffenderNo())
                    .eventDate(dto.getEventDate())
                    .eventId(dto.getEventId())
                    .eventType(EventType.valueOf(dto.getEventType()))
                    .period(TimePeriod.valueOf(dto.getPeriod()))
                    .currentLocation(dto.getCurrentLocation())
                    .build());
        }
    }

    public List<OffenderEventDto> getOffenderEventsByEvent(String prisonId, Long eventId, String eventType) {
        final List<OffenderEvent> eventEntities = repository.findByPrisonIdAndEventIdAndEventType(prisonId, eventId, EventType.valueOf(eventType));
        return eventEntities.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public List<OffenderEventDto> getOffenderEventsByEventList(String prisonId, List<EventDto> eventList) {
        /* In clause with multiple parameters isn't straightforward to implement
         - going with simple iteration through list for now.  If performance is an issue could consider a temporary table or other approach:
        http://stevenyue.com/blogs/build-sql-queries-with-temporary-table-vs-where-in/
         */

        final List<OffenderEvent> eventEntities = eventList.stream()
                .map(eventIdentifier -> repository.findByPrisonIdAndEventIdAndEventType(prisonId, eventIdentifier.getEventId(), EventType.valueOf(eventIdentifier.getEventType())))
                .flatMap(List::stream)
                .collect(Collectors.toList());

        return eventEntities.stream().map(this::convertToDto).collect(Collectors.toList());
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
