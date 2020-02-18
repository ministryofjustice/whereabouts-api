package uk.gov.justice.digital.hmpps.whereabouts.services

open class ValidationException(message: String?) : Exception(message)

class InvalidCourtLocation(message: String?) : ValidationException(message)
