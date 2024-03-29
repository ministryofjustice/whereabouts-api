package uk.gov.justice.digital.hmpps.whereabouts.services.court

import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkAppointmentSpecification
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingSpecification
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingUpdateSpecification
import uk.gov.justice.digital.hmpps.whereabouts.model.CourtHearingType
import uk.gov.justice.digital.hmpps.whereabouts.utils.DataHelpers
import java.time.LocalDateTime

class EventListenerTestData private constructor() {

  companion object {
    val startTime: LocalDateTime = LocalDateTime.of(2020, 10, 9, 10, 30)

    val booking = DataHelpers.makeVideoLinkBooking(
      id = 11L,
      offenderBookingId = -1L,
      courtName = "York Crown Court",
      courtId = "TSTCRT",
      courtHearingType = CourtHearingType.APPEAL,
      madeByTheCourt = true,
      prisonId = "WWI",
    ).apply {
      addPreAppointment(
        appointmentId = 12L,
        id = 120L,
        locationId = 20L,
        startDateTime = LocalDateTime.of(2022, 1, 1, 10, 0, 0),
        endDateTime = LocalDateTime.of(2022, 1, 1, 11, 0, 0),
      )
      addMainAppointment(
        appointmentId = 13L,
        id = 130L,
        locationId = 20L,
        startDateTime = LocalDateTime.of(2022, 1, 1, 10, 0, 0),
        endDateTime = LocalDateTime.of(2022, 1, 1, 11, 0, 0),
      )
      addPostAppointment(
        appointmentId = 14L,
        id = 140L,
        locationId = 20L,
        startDateTime = LocalDateTime.of(2022, 1, 1, 10, 0, 0),
        endDateTime = LocalDateTime.of(2022, 1, 1, 11, 0, 0),
      )
    }

    val createSpecification = VideoLinkBookingSpecification(
      bookingId = 1L,
      court = "York Crown Court",
      courtId = "TSTCRT",
      courtHearingType = CourtHearingType.APPEAL,
      comment = "Comment",
      madeByTheCourt = true,
      pre = VideoLinkAppointmentSpecification(
        locationId = 2L,
        startTime = startTime,
        endTime = startTime.plusMinutes(30),
      ),
      main = VideoLinkAppointmentSpecification(
        locationId = 2L,
        startTime = startTime.plusMinutes(60),
        endTime = startTime.plusMinutes(90),
      ),
      post = VideoLinkAppointmentSpecification(
        locationId = 2L,
        startTime = startTime.plusMinutes(120),
        endTime = startTime.plusMinutes(150),
      ),
    )

    val updateSpecification = VideoLinkBookingUpdateSpecification(
      courtId = "TSTCRT2",
      comment = "Comment",
      pre = VideoLinkAppointmentSpecification(
        locationId = 2L,
        startTime = startTime,
        endTime = startTime.plusMinutes(30),
      ),
      main = VideoLinkAppointmentSpecification(
        locationId = 2L,
        startTime = startTime.plusMinutes(60),
        endTime = startTime.plusMinutes(90),
      ),
      post = VideoLinkAppointmentSpecification(
        locationId = 2L,
        startTime = startTime.plusMinutes(120),
        endTime = startTime.plusMinutes(150),
      ),
    )
  }
}
