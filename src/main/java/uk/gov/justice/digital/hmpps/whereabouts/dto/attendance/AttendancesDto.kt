package uk.gov.justice.digital.hmpps.whereabouts.dto.attendance

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import uk.gov.justice.digital.hmpps.whereabouts.dto.BookingActivity
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod
import java.time.LocalDate

@ApiModel(description = "Attend all parameters")
data class AttendancesDto(
  @ApiModelProperty(required = true, value = "Set of active booking and activity ids", position = 1)
  val bookingActivities: Set<BookingActivity>,

  @ApiModelProperty(required = true, value = "Id of the location the event is taking place", example = "4", position = 2)
  val eventLocationId: Long,

  @ApiModelProperty(required = true, value = "Time period for the event", example = "AM", position = 3)
  val period: TimePeriod,

  @ApiModelProperty(required = true, value = "Id of prison the event is taking place", example = "LEI", position = 4)
  val prisonId: String,

  @JsonFormat(pattern = "yyyy-MM-dd")
  @ApiModelProperty(required = true, value = "Date the event is scheduled", example = "2019-10-01", position = 5)
  val eventDate: LocalDate,

  @ApiModelProperty(value = "Absent reason", example = "Refused", position = 6)
  val reason: AbsentReason? = null,

  @ApiModelProperty(value = "Indication of attendance", example = "true", position = 7)
  val attended: Boolean,

  @ApiModelProperty(value = "Indicates that the offender should be paid", example = "true", position = 8)
  val paid: Boolean,

  @ApiModelProperty(
    value = "Comment describing the offenders absence",
    example = "They had a medical appointment scheduled",
    position = 9,
  )
  val comments: String? = null,
)
