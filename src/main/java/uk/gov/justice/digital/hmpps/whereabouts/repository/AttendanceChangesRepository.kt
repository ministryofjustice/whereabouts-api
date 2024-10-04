package uk.gov.justice.digital.hmpps.whereabouts.repository

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import uk.gov.justice.digital.hmpps.whereabouts.model.AttendanceChange
import java.time.LocalDateTime

interface AttendanceChangesRepository : CrudRepository<AttendanceChange, Long> {
  @Query(
    "select ac from AttendanceChange ac " +
      "where ac.createDateTime between :fromDateTime and :toDateTime " +
      "and (:agencyId is null or ac.attendance.prisonId = :agencyId)",
  )
  fun findAttendanceChangeByCreateDateTimeBetween(
    fromDateTime: LocalDateTime,
    toDateTime: LocalDateTime,
    agencyId: String? = null,
  ): Set<AttendanceChange>

  @Query(
    "select ac from AttendanceChange ac " +
      "where ac.createDateTime = :createDateTime " +
      "and (:agencyId is null or ac.attendance.prisonId = :agencyId)",
  )
  fun findAttendanceChangeByCreateDateTime(createDateTime: LocalDateTime, agencyId: String? = null): Set<AttendanceChange>
}
