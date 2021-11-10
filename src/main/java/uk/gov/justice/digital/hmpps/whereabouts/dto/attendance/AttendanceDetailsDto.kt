package uk.gov.justice.digital.hmpps.whereabouts.dto.attendance

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import uk.gov.justice.digital.hmpps.whereabouts.dto.BookingActivity
import java.time.LocalDate

@ApiModel(description = "Attendance data from whereabouts-api and/or prisonApi")
data class AttendanceDetailsDto(
  @JsonFormat(pattern = "yyyy-MM-dd")
  @ApiModelProperty(required = true, value = "Date the event is scheduled", example = "2019-10-01", position = 1)
  val eventDate: LocalDate,

  @ApiModelProperty(
    value = "Any activity outcome captured (n.b. for outcomes captured via Whereabouts, this is also the Case note text)",
    example = "Healthcare issue - speak to SO",
    position = 2
  )
  val comments: String? = null,

  @ApiModelProperty(required = false, value = "Set of active booking and activity ids", position = 3)
  val bookingActivities: Set<BookingActivity>,

  @ApiModelProperty(required = true, value = "Location identifier.", position = 4, example = "721705")
  val locationId: Long,

  @ApiModelProperty(required = true, value = "Location description.", position = 5, example = "MDI-RES-HB1-ALE")
  val location: String
)
