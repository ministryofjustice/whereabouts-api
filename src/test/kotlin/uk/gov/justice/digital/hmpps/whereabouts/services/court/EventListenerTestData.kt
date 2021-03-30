package uk.gov.justice.digital.hmpps.whereabouts.services.court

import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkAppointmentSpecification
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingSpecification
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingUpdateSpecification
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBooking
import java.time.LocalDateTime

class EventListenerTestData private constructor() {

  companion object {
    val startTime: LocalDateTime = LocalDateTime.of(2020, 10, 9, 10, 30)

    val booking = VideoLinkBooking(
      id = 11,
      pre = VideoLinkAppointment(120L, -1L, 12L, "York Crown Court", hearingType = HearingType.PRE, createdByUsername = "A_USER", madeByTheCourt = true),
      main = VideoLinkAppointment(130L, 1L, 13L, "York Crown Court", hearingType = HearingType.MAIN, createdByUsername = "A_USER", madeByTheCourt = true),
      post = VideoLinkAppointment(140L, -1L, 14L, "York Crown Court", hearingType = HearingType.POST, createdByUsername = "A_USER", madeByTheCourt = true),
    )

    val createSpecification = VideoLinkBookingSpecification(
      bookingId = 1L,
      court = "York Crown Court",
      comment = "Comment",
      madeByTheCourt = true,
      pre = VideoLinkAppointmentSpecification(
        locationId = 2L,
        startTime = startTime,
        endTime = startTime.plusMinutes(30)
      ),
      main = VideoLinkAppointmentSpecification(
        locationId = 2L,
        startTime = startTime.plusMinutes(60),
        endTime = startTime.plusMinutes(90)
      ),
      post = VideoLinkAppointmentSpecification(
        locationId = 2L,
        startTime = startTime.plusMinutes(120),
        endTime = startTime.plusMinutes(150)
      )
    )

    val updateSpecification = VideoLinkBookingUpdateSpecification(

      comment = "Comment",
      pre = VideoLinkAppointmentSpecification(
        locationId = 2L,
        startTime = startTime,
        endTime = startTime.plusMinutes(30)
      ),
      main = VideoLinkAppointmentSpecification(
        locationId = 2L,
        startTime = startTime.plusMinutes(60),
        endTime = startTime.plusMinutes(90)
      ),
      post = VideoLinkAppointmentSpecification(
        locationId = 2L,
        startTime = startTime.plusMinutes(120),
        endTime = startTime.plusMinutes(150)
      )
    )
  }
}
