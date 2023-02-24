package uk.gov.justice.digital.hmpps.whereabouts.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.persistence.EntityNotFoundException
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.whereabouts.dto.CourtEmailDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.CourtLocationsResponse
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkAppointmentsResponse
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingResponse
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingSearchDetails
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingSpecification
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingUpdateSpecification
import uk.gov.justice.digital.hmpps.whereabouts.model.Court
import uk.gov.justice.digital.hmpps.whereabouts.model.CourtHearingType
import uk.gov.justice.digital.hmpps.whereabouts.services.court.CourtService
import uk.gov.justice.digital.hmpps.whereabouts.services.court.VideoLinkBookingEventService
import uk.gov.justice.digital.hmpps.whereabouts.services.court.VideoLinkBookingService
import uk.gov.justice.digital.hmpps.whereabouts.services.vlboptionsfinder.IVideoLinkBookingOptionsService
import uk.gov.justice.digital.hmpps.whereabouts.services.vlboptionsfinder.VideoLinkBookingOptions
import uk.gov.justice.digital.hmpps.whereabouts.services.vlboptionsfinder.VideoLinkBookingSearchSpecification
import java.time.LocalDate

@Tag(name = "court")
@RestController
@RequestMapping(value = ["court"], produces = [MediaType.APPLICATION_JSON_VALUE])
class VideoLinkBookingController(
  private val courtService: CourtService,
  private val videoLinkBookingService: VideoLinkBookingService,
  private val videoLinkBookingEventService: VideoLinkBookingEventService,

  private val videoLinkBookingOptionsService: IVideoLinkBookingOptionsService,
) {
  @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE], path = ["/all-courts"])
  @ResponseStatus(HttpStatus.OK)
  @Operation(
    description = "All court locations",
    summary = "Return all court locations"
  )
  fun getCourtNames() = CourtLocationsResponse(courtLocations = courtService.courtNames)

  @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE], path = ["/courts"])
  @ResponseStatus(HttpStatus.OK)
  @Operation(
    summary = "All courts",
    description = "Return information about all courts."
  )
  fun getCourts(): List<Court> = courtService.courts

  @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE], path = ["/court-hearing-types"])
  @ResponseStatus(HttpStatus.OK)
  @Operation(
    summary = "All court hearing types",
    description = "Return a list of all court hearing types."
  )
  fun getCourtHearingTypes(): Array<CourtHearingType> = CourtHearingType.values()

  @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE], path = ["/courts/{courtId}/email"])
  @ResponseStatus(HttpStatus.OK)
  @Operation(
    summary = "Court email address",
    description = "Return information about email address."
  )
  fun getEmailByCourtId(
    @Parameter(description = "Court id", required = true)
    @PathVariable("courtId")
    courtId: String
  ): CourtEmailDto =
    CourtEmailDto(courtService.getCourtEmailForCourtId(courtId) ?: throw EntityNotFoundException("Email not exist"))

  @PostMapping(produces = [MediaType.APPLICATION_JSON_VALUE], path = ["/video-link-appointments"])
  @ResponseStatus(HttpStatus.OK)
  @Operation(
    summary = "Video link appointments",
    description = "Return video link appointments"
  )
  fun getVideoLinkAppointments(@RequestBody appointmentIds: Set<Long>): VideoLinkAppointmentsResponse {
    val courtAppointments = videoLinkBookingService.getVideoLinkAppointments(appointmentIds)

    if (courtAppointments.isEmpty()) return VideoLinkAppointmentsResponse()

    return VideoLinkAppointmentsResponse(appointments = courtAppointments)
  }

  @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE], path = ["/video-link-bookings/{videoBookingId}"])
  @ResponseStatus(HttpStatus.OK)
  @Operation(
    summary = "A video Link Booking",
    description = "Return a video Link Booking"
  )
  fun getVideoLinkBooking(
    @Parameter(description = "Video link booking id", required = true)
    @PathVariable("videoBookingId")
    videoBookingId: Long
  ) = videoLinkBookingService.getVideoLinkBooking(videoBookingId)

  @PostMapping(
    path = ["/video-link-bookings/date/{date}"],
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  @ResponseStatus(HttpStatus.OK)
  @Operation(description = "Return all video link bookings for the specified date and prisons, optionally filtering by court.")
  fun getVideoLinkBookingsBySearchDetails(
    @Parameter(description = "Video link bookings search details parameters", required = true)
    @RequestBody
    @Valid
    searchDetails: VideoLinkBookingSearchDetails,

    @Parameter(description = "Return video link bookings for this date only. ISO-8601 date format")
    @PathVariable(name = "date")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    date: LocalDate
  ): List<VideoLinkBookingResponse> {
    return videoLinkBookingService.getVideoLinkBookingsBySearchDetails(searchDetails, date)
  }

  @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], path = ["/video-link-bookings"])
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(description = "Create a Video Link Booking")
  fun createVideoLinkBooking(
    @RequestBody
    @Valid
    videoLinkBookingSpecification: VideoLinkBookingSpecification
  ) =
    videoLinkBookingService.createVideoLinkBooking(videoLinkBookingSpecification)

  @PutMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], path = ["/video-link-bookings/{videoBookingId}"])
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(description = "Update a Video Link Booking")
  fun updateVideoLinkBooking(
    @Parameter(description = "Video link booking id", required = true)
    @PathVariable("videoBookingId")
    @NotNull
    videoBookingId: Long?,

    @RequestBody
    @Valid
    videoLinkBookingUpdateSpecification: VideoLinkBookingUpdateSpecification?
  ): ResponseEntity<Void> {
    videoLinkBookingService.updateVideoLinkBooking(videoBookingId!!, videoLinkBookingUpdateSpecification!!)
    /**
     * The Open API implementation used to document this resource contains a bug which manifests
     * when a PUT operation has a Unit return type. For that reason this method returns a ResponseEntity<Void>.
     * When bug is fixed the following line should be removed along with the return type of this method
     * so that it reverts to an implicit type of :Unit
     */
    return ResponseEntity.noContent().build()
  }

  @DeleteMapping(path = ["/video-link-bookings/{videoBookingId}"])
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(description = "Delete a Video Link Booking")
  fun deleteVideoLinkBooking(
    @Parameter(description = "Video link booking id", required = true)
    @PathVariable("videoBookingId")
    videoBookingId: Long
  ): ResponseEntity<Void> {
    videoLinkBookingService.deleteVideoLinkBooking(videoBookingId)
    return ResponseEntity.noContent().build()
  }

  @PutMapping(
    path = ["/video-link-bookings/{videoLinkBookingId}/comment"],
    consumes = [MediaType.TEXT_PLAIN_VALUE]
  )
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(description = "Update the comment for a Video Link Booking")
  fun updateVideoLinkBookingComment(
    @Parameter(description = "Video link booking id", required = true)
    @PathVariable("videoLinkBookingId")
    videoLinkBookingId: Long,

    @RequestBody(required = false)
    comment: String?
  ): ResponseEntity<Void> {
    videoLinkBookingService.updateVideoLinkBookingComment(videoLinkBookingId, comment)
    /**
     * The Open API implementation used to document this resource contains a bug which manifests
     * when a PUT operation has a Unit return type. For that reason this method returns a ResponseEntity<Void>.
     * When bug is fixed the following line should be removed along with the return type of this method
     * so that it reverts to an implicit type of :Unit
     */
    return ResponseEntity.noContent().build()
  }

  @PostMapping(
    path = ["/video-link-booking-check"],
    consumes = [MediaType.APPLICATION_JSON_VALUE],
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  @Operation(
    description = "Check that a potential video link booking, described by the supplied specification, can be made.  If not then return information about some alternatives.",
  )
  fun findAvailableVideoLinkBookingOptions(
    @Valid
    @RequestBody
    specification: VideoLinkBookingSearchSpecification
  ): VideoLinkBookingOptions =
    videoLinkBookingOptionsService.findVideoLinkBookingOptions(specification)

  @GetMapping(
    path = ["/video-link-bookings"],
    produces = ["text/csv"]
  )
  @Operation(
    summary = "Video Link Bookings",
    description = "Return details of Video Link Bookings in CSV format. Restrict the response to bookings with a main start time within 'days' of start-date."
  )
  fun getVideoLinkBookingsByStartDate(
    @RequestParam(name = "start-date", required = true)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Parameter(description = "The earliest booking start time for which to return bookings for.", required = true)
    startDate: LocalDate,

    @RequestParam(name = "days")
    @Parameter(description = "Return details of bookings occurring within this number of days of start-date")
    days: Long?
  ) =
    videoLinkBookingEventService.getBookingsByStartDateAsCSV(startDate, days ?: 7L)
}
