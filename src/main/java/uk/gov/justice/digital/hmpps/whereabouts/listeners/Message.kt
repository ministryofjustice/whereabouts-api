package uk.gov.justice.digital.hmpps.whereabouts.listeners

import com.google.gson.annotations.SerializedName

data class Message(
  @SerializedName("Message")
  val message: String,
  @SerializedName("MessageAttributes")
  val messageAttributes: MessageAttributes,
)

data class Attribute(
  @SerializedName("Type")
  val type: String,
  @SerializedName("Value")
  val value: String,
)

data class MessageAttributes(val eventType: Attribute)
data class Booking(val offenderBookId: Long)
data class Offender(val offenderId: Long, val bookings: List<Booking>)
data class DeleteOffenderEventMessage(val offenderIdDisplay: String, val offenders: List<Offender>)
data class AppointmentChangedEventMessage(
  val bookingId: Long,
  val scheduleEventId: Long,
  val scheduleEventStatus: ScheduleEventStatus,
  val recordDeleted: Boolean,
  val agencyLocationId: String,
  val eventDatetime: String,
  val scheduledStartTime: String,
  val scheduledEndTime: String,
)

data class AdditionalInformation(
  val nomsNumber: String,
  val reason: Reason,
  val prisonId: String,
)

data class ReleasedOffenderEventMessage(
  val occurredAt: String,
  val additionalInformation: AdditionalInformation,
)
enum class ScheduleEventStatus {
  CANC, COMP, EXP, SCH
}

enum class Reason {
  TEMPORARY_ABSENCE_RELEASE,
  RELEASED_TO_HOSPITAL,
  RELEASED,
  SENT_TO_COURT,
  TRANSFERRED,
  UNKNOWN,
}
