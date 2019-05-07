package uk.gov.justice.digital.hmpps.whereabouts.services


import com.google.common.collect.Lists
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.runners.MockitoJUnitRunner
import uk.gov.justice.digital.hmpps.whereabouts.dto.EventDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.OffenderEventDto
import uk.gov.justice.digital.hmpps.whereabouts.model.EventType
import uk.gov.justice.digital.hmpps.whereabouts.model.OffenderEvent
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod
import uk.gov.justice.digital.hmpps.whereabouts.repository.OffenderEventRepository
import java.time.LocalDate
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class OffenderEventServiceTest {


    @Mock
    private val repository: OffenderEventRepository? = null

    private var offenderEventService: OffenderEventService? = null

    @Before
    fun setUp() {
        offenderEventService = OffenderEventService(repository)
    }

    @Test
    fun createOffenderEvent() {
    }

    @Test
    fun getOffenderEventsByEvent() {
        val oeList = ArrayList<OffenderEvent>()
        val eventApp1 = getTestOffenderEvent(1L, 123L, EventType.APP)
        oeList.add(eventApp1)
        val eventApp2 = getTestOffenderEvent(2L, 123L, EventType.APP)
        oeList.add(eventApp2)
        val eventApp3 = getTestOffenderEvent(3L, 123L, EventType.APP)
        oeList.add(eventApp1)
        val eventApp4 = getTestOffenderEvent(4L, 123L, EventType.APP)
        oeList.add(eventApp2)

        `when`(repository!!.findByPrisonIdAndEventIdAndEventType("LEI", 123L, EventType.APP)).thenReturn(oeList)

        val resultList = offenderEventService!!.getOffenderEventsByEvent("LEI", 123L, EventType.APP.name)

        assertThat(resultList).containsExactly(convertToDto(eventApp1), convertToDto(eventApp2), convertToDto(eventApp3), convertToDto(eventApp4))
    }

    @Test
    fun getOffenderEventsByEventNoData() {

        `when`(repository!!.findByPrisonIdAndEventIdAndEventType("LEI", 123L, EventType.APP)).thenReturn(Lists.newArrayList())

        val resultList = offenderEventService!!.getOffenderEventsByEvent("LEI", 123L, EventType.APP.name)

        assertThat(resultList).hasSize(0)
    }

    @Test
    fun getOffenderEventsByEventList() {
        val oeList = ArrayList<OffenderEvent>()
        val eventApp1 = getTestOffenderEvent(1L, 123L, EventType.APP)
        oeList.add(eventApp1)
        val eventApp2 = getTestOffenderEvent(2L, 123L, EventType.APP)
        oeList.add(eventApp2)

        val oeList2 = ArrayList<OffenderEvent>()
        val eventVisit1 = getTestOffenderEvent(3L, 333L, EventType.VISIT)
        oeList2.add(eventVisit1)
        val eventVisit2 = getTestOffenderEvent(4L, 333L, EventType.VISIT)
        oeList2.add(eventVisit2)

        `when`(repository!!.findByPrisonIdAndEventIdAndEventType("LEI", 123L, EventType.APP)).thenReturn(oeList)
        `when`(repository.findByPrisonIdAndEventIdAndEventType("LEI", 333L, EventType.VISIT)).thenReturn(oeList2)

        val resultList = offenderEventService!!.getOffenderEventsByEventList("LEI",
                Lists.newArrayList(EventDto(123L, EventType.APP.name), EventDto(333L, EventType.VISIT.name)))

        assertThat(resultList).containsExactly(convertToDto(eventApp1), convertToDto(eventApp2), convertToDto(eventVisit1), convertToDto(eventVisit2))
    }

    @Test
    fun getOffenderEventsByEventListNoData() {

        `when`(repository!!.findByPrisonIdAndEventIdAndEventType("LEI", 123L, EventType.APP)).thenReturn(Lists.newArrayList())
        `when`(repository.findByPrisonIdAndEventIdAndEventType("LEI", 333L, EventType.VISIT)).thenReturn(Lists.newArrayList())

        val resultList = offenderEventService!!.getOffenderEventsByEventList("LEI",
                Lists.newArrayList(EventDto(123L, EventType.APP.name), EventDto(333L, EventType.VISIT.name)))

        assertThat(resultList).hasSize(0)
    }

    private fun getTestOffenderEvent(id: Long?, eventId: Long?, type: EventType): OffenderEvent {
        val event = OffenderEvent.builder()
                .eventId(eventId)
                .eventType(type)
                .eventDate(LocalDate.of(2018, 5, 6))
                .offenderNo("123A")
                .prisonId("LEI")
                .currentLocation(false)
                .period(TimePeriod.ED)
                .build()
        event.id = id
        return event
    }

    private fun convertToDto(oe: OffenderEvent): OffenderEventDto {
        return OffenderEventDto.builder()
                .eventId(oe.eventId)
                .eventType(oe.eventType.name)
                .eventDate(oe.eventDate)
                .offenderNo(oe.offenderNo)
                .prisonId(oe.prisonId)
                .currentLocation(oe.currentLocation)
                .period(oe.period.name)
                .build()
    }
}
