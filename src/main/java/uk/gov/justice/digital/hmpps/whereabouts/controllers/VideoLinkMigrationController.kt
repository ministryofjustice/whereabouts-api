package uk.gov.justice.digital.hmpps.whereabouts.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.whereabouts.services.court.VideoLinkMigrationService
import java.time.LocalDate

@Tag(name = "Migration")
@RestController
@RequestMapping(value = ["/migrate"], produces = [MediaType.APPLICATION_JSON_VALUE])
class VideoLinkMigrationController(
  val videoLinkMigrationService: VideoLinkMigrationService,
) {
  @GetMapping(
    path = ["/video-link-booking/{videoBookingId}"],
    produces = [MediaType.APPLICATION_JSON_VALUE],
  )
  @ResponseStatus(HttpStatus.OK)
  @Operation(
    summary = "Returns the data for one video link booking and its associated events",
    description = """
      Requires role: ROLE_BOOK_A_VIDEO_LINK_ADMIN
      Used to migrate the data for one video link booking
      """,
  )
  @PreAuthorize("hasAnyRole('BOOK_A_VIDEO_LINK_ADMIN')")
  fun getVideoLinkBookingToMigrate(
    @Parameter(description = "The video link booking ID to return data for.", required = true)
    @PathVariable videoBookingId: Long,
  ) = videoLinkMigrationService.getVideoLinkBookingToMigrate(videoBookingId)

  @PutMapping(
    path = ["/video-link-bookings"],
    produces = [MediaType.APPLICATION_JSON_VALUE],
  )
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
    summary = "Trigger the process which will emit domain events for all bookings with a main hearing start time that is after the fromDate",
    description = """
      Requires role: ROLE_BOOK_A_VIDEO_LINK_ADMIN
      Triggers the start of the data migration process for video link bookings
      """,
  )
  @PreAuthorize("hasAnyRole('BOOK_A_VIDEO_LINK_ADMIN')")
  fun migrateVideoLinkBookings(
    @Parameter(description = "Earliest date for the main hearing time", required = true, example = "2023-10-01")
    fromDate: LocalDate,
    @Parameter(description = "Page size", required = false, example = "10")
    pageSize: Int = 10,
    @Parameter(description = "Milliseconds to delay between pages")
    waitMillis: Long = 0,
  ) = videoLinkMigrationService.migrateVideoLinkBookingsSinceDate(fromDate, pageSize, waitMillis)
}
