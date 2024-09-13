package uk.gov.justice.digital.hmpps.whereabouts.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.whereabouts.services.court.VideoLinkMigrationService

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
}
