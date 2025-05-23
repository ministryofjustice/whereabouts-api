package uk.gov.justice.digital.hmpps.whereabouts.services

open class ValidationException(message: String?) : RuntimeException(message)

open class InvalidCourtLocation(message: String?) : ValidationException(message)

class DatabaseRowLockedException : RuntimeException(DEFAULT_MESSAGE_FOR_ID_FORMAT) {

  companion object {
    private const val DEFAULT_MESSAGE_FOR_ID_FORMAT = "Resource locked, possibly in use in P-Nomis."
  }
}
