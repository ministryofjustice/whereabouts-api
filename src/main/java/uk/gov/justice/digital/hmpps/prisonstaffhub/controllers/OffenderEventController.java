package uk.gov.justice.digital.hmpps.prisonstaffhub.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import uk.gov.justice.digital.hmpps.prisonstaffhub.dto.EventDto;
import uk.gov.justice.digital.hmpps.prisonstaffhub.dto.OffenderEventDto;
import uk.gov.justice.digital.hmpps.prisonstaffhub.services.OffenderEventService;

import javax.validation.Valid;
import java.util.List;

@Api(tags = {"whereabouts"})

@RestController
@RequestMapping(
        value="whereabouts",
        produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
public class OffenderEventController {

    private final OffenderEventService offenderEventService;

    public OffenderEventController(OffenderEventService offenderEventService) {

        this.offenderEventService = offenderEventService;
    }

    @PostMapping(path = "/offender-event")
    public void createOffenderEvent(
            @ApiParam(value = "Offender event details." , required=true )
            @Valid
            @RequestBody
                    OffenderEventDto offenderEventDto

    ) {
        offenderEventService.createOffenderEvent(offenderEventDto);
    }

    @GetMapping(path = "/{prisonId}/offender-event/{eventId}/event-type/{eventType}")
    public List<OffenderEventDto> createOffenderEvent(
            @ApiParam(value = "prisonId", required = true)
            @NotEmpty
            @PathVariable("prisonId")
                    String prisonId,

            @ApiParam(value = "eventId", required = true)
            @NotEmpty
            @PathVariable("eventId")
                    Long eventId,

            @ApiParam(value = "eventType", required = true)
            @NotEmpty
            @PathVariable("eventType")
                    String eventType
    ) {
        return offenderEventService.getOffenderEventsByEvent(prisonId, eventId,eventType);
    }

    @PostMapping(path = "/{prisonId}/offender-event")
    public List<OffenderEventDto> getOffenderEventsListPost(
            @ApiParam(value = "prisonId", required = true)
            @NotEmpty
            @PathVariable("prisonId") String prisonId,
            @ApiParam(value = "List of event ids and types to identify events to be returned. Event type can be one of PRISON_ACT, VISIT or APP")
            @RequestBody @NotEmpty List<EventDto> events
    ) {
        Validate.notEmpty(events, "Please provide a list of Event ids and types.");
        return offenderEventService.getOffenderEventsByEventList(prisonId, events);
    }

}
