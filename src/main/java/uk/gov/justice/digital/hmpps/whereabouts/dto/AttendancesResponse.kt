package uk.gov.justice.digital.hmpps.whereabouts.dto

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import org.joda.time.DateTime
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason
import uk.gov.justice.digital.hmpps.whereabouts.model.AttendanceChangeValues
import java.time.LocalDateTime

@ApiModel(description = "Attendances response")
data class AttendancesResponse(
    @ApiModelProperty(value = "Set of attendances")
    var attendances: Set<AttendanceDto>? = null
)

@ApiModel(description = "Attendance changes")
data class AttendanceChangesResponse(
    @ApiModelProperty(value= "Set of changes")
    var changes: Set<AttendanceChangeDto>? = null
)

@ApiModel("Attendance change")
data class AttendanceChangeDto (
    @ApiModelProperty(value = "Id of the change record")
    val id: Long,
    @ApiModelProperty(value ="Attendance id")
    val attendanceId: Long,
    @ApiModelProperty(value = "Event id")
    val eventId: Long,
    @ApiModelProperty("Event location id")
    val eventLocationId: Long,
    @ApiModelProperty("Booking id")
    val bookingId: Long,
    @ApiModelProperty(value = "Previous attendance reason")
    val changedFrom: AttendanceChangeValues?,
    @ApiModelProperty(value = "New attendance reason")
    val changedTo: AttendanceChangeValues?,
    @ApiModelProperty("Date and time when the changed occurred")
    val changedOn: LocalDateTime?,
    val changedBy: String?
)
