package uk.gov.justice.digital.hmpps.whereabouts.services

open class ValidationException(message: String?) : RuntimeException(message)

open class InvalidCourtLocation(message: String?) : ValidationException(message)
