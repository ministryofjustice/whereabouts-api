package uk.gov.justice.digital.hmpps.whereabouts.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.whereabouts.services.court.VideoLinkBookingEventService
import java.time.LocalDate

@RestController
@RequestMapping(path = ["/events"])
@Tag(name = "events")
@PreAuthorize("hasAnyRole('MAINTAIN_WHEREABOUTS')")
class VideoLinkBookingEventController(val service: VideoLinkBookingEventService) {

  @GetMapping(
    path = ["video-link-booking-events"],
    produces = ["text/csv"],
  )
  @Operation(
    summary = "Video Link Booking Events",
    description = "Return details of Video Link Booking Events (Create, Update, Delete) in CSV format. Restrict the response to events occurring within 'days' of start-date.",
  )
  fun getVideoLinkBookingEvents(
    @RequestParam(name = "start-date", required = true)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Parameter(description = "The earliest date for which to return event details.", required = true)
    startDate: LocalDate,

    @RequestParam(name = "days")
    @Parameter(description = "Return details of events occurring within this number of days of start-date")
    days: Long?,
  ) =
    service.getEventsAsCSV(startDate, days ?: 7L)
}
