package uk.gov.justice.digital.hmpps.whereabouts.services

import jakarta.persistence.EntityNotFoundException

class AttendanceExists : RuntimeException("Attendance already exists")
class AttendanceLocked : RuntimeException("Attendance record is locked")
class AttendanceNotFound : EntityNotFoundException()
