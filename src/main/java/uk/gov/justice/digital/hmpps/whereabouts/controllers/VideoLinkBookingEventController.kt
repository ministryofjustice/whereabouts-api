package uk.gov.justice.digital.hmpps.whereabouts.controllers

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.whereabouts.services.court.VideoLinkBookingEventService
import java.time.LocalDate

@RestController
@RequestMapping(path = ["/events"])
@Api(tags = ["events"])
class VideoLinkBookingEventController(val service: VideoLinkBookingEventService) {

  @GetMapping(
    path = ["video-link-booking-events"],
    produces = ["text/csv"]
  )
  @ApiOperation(
    value = "Video Link Booking Events",
    response = String::class,
    notes = "Return details of Video Link Booking Events (Create, Update, Delete) in CSV format. Restrict the response to events occurring within 'days' of start-date."
  )
  fun getVideoLinkBookingEvents(
    @RequestParam(name = "start-date", required = true)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @ApiParam(value = "The earliest date for which to return event details.", required = true)
    startDate: LocalDate,

    @RequestParam(name = "days")
    @ApiParam(value = "Return details of events occurring within this number of days of start-date", defaultValue = "7")
    days: Long?
  ) =
    service.getEventsAsCSV(startDate, days ?: 7L)
}
