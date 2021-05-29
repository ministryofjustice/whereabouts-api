package uk.gov.justice.digital.hmpps.whereabouts.dto

interface VideoLinkAppointmentsSpecification {
  val comment: String?
  val pre: VideoLinkAppointmentSpecification?
  val main: VideoLinkAppointmentSpecification
  val post: VideoLinkAppointmentSpecification?
}
