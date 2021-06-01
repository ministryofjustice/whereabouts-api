package uk.gov.justice.digital.hmpps.whereabouts.utils

import uk.gov.justice.digital.hmpps.whereabouts.dto.Appointment
import uk.gov.justice.digital.hmpps.whereabouts.dto.AppointmentDefaults
import uk.gov.justice.digital.hmpps.whereabouts.dto.CreateAppointmentSpecification
import uk.gov.justice.digital.hmpps.whereabouts.dto.CreatePrisonAppointment
import uk.gov.justice.digital.hmpps.whereabouts.dto.Repeat
import uk.gov.justice.digital.hmpps.whereabouts.model.PrisonAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBooking
import java.time.LocalDateTime

class DataHelpers {

  companion object {

    fun makeVideoLinkBooking(id: Long): VideoLinkBooking =
      VideoLinkBooking(id = id, offenderBookingId = -1L, courtName = "Court 1", createdByUsername = "SA").apply {
        addMainAppointment(id = 1L, appointmentId = 1L)
        addPreAppointment(id = 2L, appointmentId = 2L)
        addPostAppointment(id = 3L, appointmentId = 3L)
      }

    fun makeCreatePrisonAppointment(
      appointmentId: Long,
      startTime: LocalDateTime = LocalDateTime.now(),
      endTime: LocalDateTime = LocalDateTime.now(),
      bookingId: Long = -1L,
      agencyId: String = "MDI",
      eventSubType: String = "INST",
      comment: String = "test",
      eventLocationId: Long = 2L,
      createUserId: String = "SA"
    ) = PrisonAppointment(
      eventId = appointmentId,
      eventLocationId = eventLocationId,
      agencyId = agencyId,
      bookingId = bookingId,
      startTime = startTime,
      endTime = endTime,
      eventSubType = eventSubType,
      comment = comment,
      createUserId = createUserId
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
      createUserId: String = "SA"
    ): PrisonAppointment = PrisonAppointment(
      eventId = eventId,
      eventSubType = eventSubType,
      startTime = startTime,
      endTime = endTime,
      eventLocationId = eventLocationId,
      bookingId = bookingId,
      comment = comment,
      agencyId = agencyId,
      createUserId = createUserId
    )

    fun makeCreateAppointmentSpecification(
      bookingId: Long = -1,
      appointmentType: String = "ABC",
      locationId: Long = 1,
      comment: String? = "test",
      startTime: LocalDateTime = LocalDateTime.now(),
      endTime: LocalDateTime = LocalDateTime.now(),
      repeat: Repeat? = null
    ): CreateAppointmentSpecification = CreateAppointmentSpecification(
      bookingId = bookingId,
      appointmentType = appointmentType,
      locationId = locationId,
      comment = comment,
      startTime = startTime,
      endTime = endTime,
      repeat = repeat
    )

    fun makeCreatePrisonAppointment(
      bookingId: Long = -1,
      appointmentType: String = "ABC",
      locationId: Long = 1,
      comment: String? = "test",
      startTime: LocalDateTime = LocalDateTime.now(),
      endTime: LocalDateTime = LocalDateTime.now(),
      repeat: Repeat? = null
    ) =
      CreatePrisonAppointment(
        appointmentDefaults = AppointmentDefaults(
          appointmentType = appointmentType,
          comment = comment,
          startTime = startTime,
          endTime = endTime,
          locationId = locationId
        ),
        appointments = listOf(
          Appointment(
            bookingId = bookingId,
            comment = comment,
            startTime = startTime,
            endTime = endTime
          )
        ),
        repeat = repeat
      )
  }
}
