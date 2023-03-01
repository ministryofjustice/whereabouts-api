package uk.gov.justice.digital.hmpps.whereabouts.repository

import org.springframework.data.repository.CrudRepository
import uk.gov.justice.digital.hmpps.whereabouts.model.AttendanceChange
import java.time.LocalDateTime

interface AttendanceChangesRepository : CrudRepository<AttendanceChange, Long> {
  fun findAttendanceChangeByCreateDateTimeBetween(
    fromDateTime: LocalDateTime,
    toDateTime: LocalDateTime,
  ): Set<AttendanceChange>

  fun findAttendanceChangeByCreateDateTime(createDateTime: LocalDateTime): Set<AttendanceChange>
}
