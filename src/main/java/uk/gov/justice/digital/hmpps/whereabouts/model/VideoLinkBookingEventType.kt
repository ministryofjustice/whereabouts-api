package uk.gov.justice.digital.hmpps.whereabouts.model

enum class VideoLinkBookingEventType(val code: Char) {
  CREATE('C'),
  UPDATE('U'),
  DELETE('D'),
}
