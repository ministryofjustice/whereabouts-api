package uk.gov.justice.digital.hmpps.whereabouts.services.vlboptionsfinder

data class VideoLinkBookingOptions(
  val matched: Boolean,
  val alternatives: List<DescribedLocationAndInterval>
)
