package uk.gov.justice.digital.hmpps.prisonstaffhub.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.digital.hmpps.prisonstaffhub.model.EventType;
import uk.gov.justice.digital.hmpps.prisonstaffhub.model.OffenderEvent;

import java.util.List;

public interface OffenderEventRepository extends CrudRepository<OffenderEvent,Long> {
    List<OffenderEvent> findByPrisonIdAndOffenderNoAndEventIdAndEventType(String prisonId, String offenderNo, Long eventId, EventType eventType);
    List<OffenderEvent> findByPrisonIdAndEventIdAndEventType(String prisonId, Long eventId, EventType eventType);
}
