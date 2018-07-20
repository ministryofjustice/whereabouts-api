package uk.gov.justice.digital.hmpps.prisonstaffhub.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.hmpps.prisonstaffhub.model.EventType;
import uk.gov.justice.digital.hmpps.prisonstaffhub.model.OffenderEvent;
import uk.gov.justice.digital.hmpps.prisonstaffhub.model.TimePeriod;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Transactional
public class OffenderEventRepositoryTest {


    @Autowired
    private OffenderEventRepository repository;

    @Test
    public void shouldPersistOffenderEventData() {

        OffenderEvent transientEntity = persistTestEvent(123L, EventType.VISIT);

        TestTransaction.flagForCommit();
        TestTransaction.end();

        TestTransaction.start();

        OffenderEvent retrievedEntity = repository.findByPrisonIdAndOffenderNoAndEventIdAndEventType(
                transientEntity.getPrisonId(),
                transientEntity.getOffenderNo(),
                transientEntity.getEventId(),
                transientEntity.getEventType()
        ).get(0);

        assertThat(retrievedEntity).isEqualTo(transientEntity);

        assertThat(retrievedEntity.getCurrentLocation()).isEqualTo(transientEntity.getCurrentLocation());
        assertThat(retrievedEntity.getEventDate()).isEqualTo(transientEntity.getEventDate());
        assertThat(retrievedEntity.getEventType()).isEqualTo(transientEntity.getEventType());
        assertThat(retrievedEntity.getPeriod()).isEqualTo(transientEntity.getPeriod());
        assertThat(retrievedEntity.getPrisonId()).isEqualTo(transientEntity.getPrisonId());

        TestTransaction.end();
    }

    @Test
    public void shouldRetrieveOffenderEventsByEventId() {

        persistTestEvent(11L, EventType.PRISON_ACT);
        persistTestEvent(12L, EventType.PRISON_ACT);
        persistTestEvent(13L, EventType.PRISON_ACT);
        persistTestEvent(11L, EventType.VISIT);
        persistTestEvent(12L, EventType.VISIT);
        persistTestEvent(11L, EventType.APP);
        persistTestEvent(12L, EventType.APP);

        TestTransaction.flagForCommit();
        TestTransaction.end();

        TestTransaction.start();


        List<OffenderEvent> retrievedEntities = repository.findByPrisonIdAndEventIdAndEventType("LEI", 11L, EventType.PRISON_ACT);

        assertThat(retrievedEntities).hasSize(1);

        TestTransaction.end();
    }

    @Test
    public void shouldRetrieveOffenderEventsByEventId_filterbyprison() {

        persistTestEvent(1L, EventType.PRISON_ACT);

        TestTransaction.flagForCommit();
        TestTransaction.end();

        TestTransaction.start();


        List<OffenderEvent> retrievedEntities = repository.findByPrisonIdAndEventIdAndEventType("UKN", 1L, EventType.PRISON_ACT);

        assertThat(retrievedEntities).hasSize(0);

        TestTransaction.end();
    }

    private OffenderEvent persistTestEvent(Long eventId, EventType type) {
        OffenderEvent transientEntity = transientEntity(eventId, type);

        OffenderEvent persistedEntity = repository.save(transientEntity);

        assertThat(persistedEntity.getId()).isNotNull();
        return transientEntity;
    }


    private OffenderEvent transientEntity(Long eventId, EventType type) {
        return OffenderEvent
                .builder()
                .currentLocation(Boolean.TRUE)
                .eventDate(LocalDate.of(2018, 5, 1))
                .offenderNo("abc1")
                .eventType(type)
                .eventId(eventId)
                .prisonId("LEI")
                .period(TimePeriod.AM)
                .build();
    }

}
