package uk.gov.justice.digital.hmpps.whereabouts.utils

import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType
import uk.gov.justice.digital.hmpps.whereabouts.model.PrisonAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBooking
import java.time.LocalDateTime

fun makeVideoLinkBooking(
  id: Long,
  main: VideoLinkAppointment = makeVideoLinkAppointment(id = 1L, appointmentId = 1L, hearingType = HearingType.MAIN),
  pre: VideoLinkAppointment = makeVideoLinkAppointment(id = 2L, appointmentId = 2L, hearingType = HearingType.PRE),
  post: VideoLinkAppointment = makeVideoLinkAppointment(id = 3L, appointmentId = 3L, hearingType = HearingType.POST)
): VideoLinkBooking = VideoLinkBooking(
  id = id,
  main = main,
  pre = pre,
  post = post
)

fun makeVideoLinkAppointment(
  id: Long,
  bookingId: Long = -1L,
  appointmentId: Long,
  court: String = "Court 1",
  hearingType: HearingType,
  createdByUsername: String = "SA",
  madeByTheCourt: Boolean = true
): VideoLinkAppointment = VideoLinkAppointment(
  id = id,
  bookingId = bookingId,
  appointmentId = appointmentId,
  court = court,
  hearingType = hearingType,
  createdByUsername = createdByUsername,
  madeByTheCourt = madeByTheCourt
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
