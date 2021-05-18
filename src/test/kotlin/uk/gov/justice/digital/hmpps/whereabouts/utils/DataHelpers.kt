package uk.gov.justice.digital.hmpps.whereabouts.utils

import uk.gov.justice.digital.hmpps.whereabouts.model.PrisonAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBooking
import java.time.LocalDateTime

fun makeVideoLinkBooking(
  id: Long,
  bookingId: Long = -1L,
  court: String = "Court 1",
  createdByUsername: String = "SA",
  madeByTheCourt: Boolean = true,
  main: VideoLinkAppointment = VideoLinkAppointment(id = 1L, appointmentId = 1L),
  pre: VideoLinkAppointment = VideoLinkAppointment(id = 2L, appointmentId = 2L),
  post: VideoLinkAppointment = VideoLinkAppointment(id = 3L, appointmentId = 3L)
): VideoLinkBooking = VideoLinkBooking(
  id = id,
  bookingId = bookingId,
  court = court,
  madeByTheCourt = madeByTheCourt,
  createdByUsername = createdByUsername,
  main = main,
  pre = pre,
  post = post
)

fun makePrisonAppointment(
  appointmentId: Long,
  startTime: LocalDateTime = LocalDateTime.now(),
  endTime: LocalDateTime = LocalDateTime.now(),
  bookingId: Long = -1L,
  agencyId: String = "MDI",
  eventSubType: String = "INST",
  comment: String = "test",
  eventLocationId: Long = 2L,
) = PrisonAppointment(
  eventId = appointmentId,
  eventLocationId = eventLocationId,
  agencyId = agencyId,
  bookingId = bookingId,
  startTime = startTime,
  endTime = endTime,
  eventSubType = eventSubType,
  comment = comment
)

fun makeAppointmentDto(
  eventId: Long = 1L,
  eventSubType: String = "INTERV",
  startTime: LocalDateTime = LocalDateTime.now(),
  endTime: LocalDateTime = LocalDateTime.now(),
  eventLocationId: Long = 1L,
  bookingId: Long = 1L,
  comment: String = "test",
  agencyId: String = "MDI"
): PrisonAppointment = PrisonAppointment(
  eventId = eventId,
  eventSubType = eventSubType,
  startTime = startTime,
  endTime = endTime,
  eventLocationId = eventLocationId,
  bookingId = bookingId,
  comment = comment,
  agencyId = agencyId
)
