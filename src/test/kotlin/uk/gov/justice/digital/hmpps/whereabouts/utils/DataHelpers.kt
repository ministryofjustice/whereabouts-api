package uk.gov.justice.digital.hmpps.whereabouts.utils

import uk.gov.justice.digital.hmpps.whereabouts.dto.Appointment
import uk.gov.justice.digital.hmpps.whereabouts.dto.AppointmentDefaults
import uk.gov.justice.digital.hmpps.whereabouts.dto.CreateAppointmentSpecification
import uk.gov.justice.digital.hmpps.whereabouts.dto.CreatePrisonAppointment
import uk.gov.justice.digital.hmpps.whereabouts.dto.Repeat
import uk.gov.justice.digital.hmpps.whereabouts.listeners.AppointmentChangedEventMessage
import uk.gov.justice.digital.hmpps.whereabouts.listeners.ScheduleEventStatus
import uk.gov.justice.digital.hmpps.whereabouts.model.PrisonAppointment
import java.time.LocalDateTime

class DataHelpers {

  companion object {

    fun makeCreatePrisonAppointment(
      appointmentId: Long,
      startTime: LocalDateTime = LocalDateTime.now(),
      endTime: LocalDateTime = LocalDateTime.now(),
      bookingId: Long = -1L,
      agencyId: String = "MDI",
      eventSubType: String = "INST",
      comment: String = "test",
      eventLocationId: Long = 2L,
      createUserId: String = "SA",
    ) = PrisonAppointment(
      eventId = appointmentId,
      eventLocationId = eventLocationId,
      agencyId = agencyId,
      bookingId = bookingId,
      startTime = startTime,
      endTime = endTime,
      eventSubType = eventSubType,
      comment = comment,
      createUserId = createUserId,
    )

    fun makePrisonAppointment(
      eventId: Long = 1L,
      eventSubType: String = "INTERV",
      startTime: LocalDateTime = LocalDateTime.now(),
      endTime: LocalDateTime = LocalDateTime.now(),
      eventLocationId: Long = 1L,
      bookingId: Long = 1L,
      comment: String = "test",
      agencyId: String = "MDI",
      createUserId: String = "SA",
    ): PrisonAppointment = PrisonAppointment(
      eventId = eventId,
      eventSubType = eventSubType,
      startTime = startTime,
      endTime = endTime,
      eventLocationId = eventLocationId,
      bookingId = bookingId,
      comment = comment,
      agencyId = agencyId,
      createUserId = createUserId,
    )

    fun makeCreateAppointmentSpecification(
      bookingId: Long = -1,
      appointmentType: String = "ABC",
      locationId: Long = 1,
      comment: String? = "test",
      startTime: LocalDateTime = LocalDateTime.now(),
      endTime: LocalDateTime = LocalDateTime.now(),
      repeat: Repeat? = null,
    ): CreateAppointmentSpecification = CreateAppointmentSpecification(
      bookingId = bookingId,
      appointmentType = appointmentType,
      locationId = locationId,
      comment = comment,
      startTime = startTime,
      endTime = endTime,
      repeat = repeat,
    )

    fun makeCreatePrisonAppointment(
      bookingId: Long = -1,
      appointmentType: String = "ABC",
      locationId: Long = 1,
      comment: String? = "test",
      startTime: LocalDateTime = LocalDateTime.now(),
      endTime: LocalDateTime = LocalDateTime.now(),
      repeat: Repeat? = null,
    ) =
      CreatePrisonAppointment(
        appointmentDefaults = AppointmentDefaults(
          appointmentType = appointmentType,
          comment = comment,
          startTime = startTime,
          endTime = endTime,
          locationId = locationId,
        ),
        appointments = listOf(
          Appointment(
            bookingId = bookingId,
            comment = comment,
            startTime = startTime,
            endTime = endTime,
          ),
        ),
        repeat = repeat,
      )

    fun makeAppointmentChangedEventMessage(
      bookingId: Long = 2L,
      scheduleEventId: Long = 13L,
      recordDeleted: Boolean = false,
      agencyLocationId: String = "WWI",
      eventDatetime: String = "2022-01-01T11:00:00",
      scheduledStartTime: String = "2022-01-01T11:00",
      scheduledEndTime: String = "2022-01-01T12:00",
      scheduleEventStatus: ScheduleEventStatus = ScheduleEventStatus.SCH,
    ) = AppointmentChangedEventMessage(
      bookingId = bookingId,
      scheduleEventId = scheduleEventId,
      recordDeleted = recordDeleted,
      agencyLocationId = agencyLocationId,
      eventDatetime = eventDatetime,
      scheduledStartTime = scheduledStartTime,
      scheduledEndTime = scheduledEndTime,
      scheduleEventStatus = scheduleEventStatus,
    )
  }
}
