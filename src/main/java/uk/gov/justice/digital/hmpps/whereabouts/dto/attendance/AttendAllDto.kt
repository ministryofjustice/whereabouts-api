package uk.gov.justice.digital.hmpps.whereabouts.dto.attendance

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import jakarta.validation.constraints.NotNull
import uk.gov.justice.digital.hmpps.whereabouts.dto.BookingActivity
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod
import java.time.LocalDate

@ApiModel(description = "Attend all parameters")
data class AttendAllDto(
  @ApiModelProperty(required = true, value = "Set of active booking and activity ids", position = 1)
  val bookingActivities: Set<BookingActivity>,

  @ApiModelProperty(required = true, value = "Id of the location the event is taking place", example = "4", position = 2)
  @field:NotNull
  val eventLocationId: Long? = null,

  @ApiModelProperty(required = true, value = "Time period for the event", example = "AM", position = 3)
  @field:NotNull
  val period: TimePeriod? = null,

  @ApiModelProperty(required = true, value = "Id of prison the event is taking place", example = "LEI", position = 4)
  @field:NotNull
  val prisonId: String? = null,

  @JsonFormat(pattern = "yyyy-MM-dd")
  @ApiModelProperty(required = true, value = "Date the event is scheduled", example = "2019-10-01", position = 5)
  @field:NotNull
  val eventDate: LocalDate? = null
)
