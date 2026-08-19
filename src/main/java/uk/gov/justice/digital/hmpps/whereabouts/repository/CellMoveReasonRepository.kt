package uk.gov.justice.digital.hmpps.whereabouts.repository

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import uk.gov.justice.digital.hmpps.whereabouts.model.CellMoveReason
import uk.gov.justice.digital.hmpps.whereabouts.model.CellMoveReasonPK

interface CellMoveReasonRepository : CrudRepository<CellMoveReason, CellMoveReasonPK> {

  /**
   * A page of cell move reasons after the given key, in primary key order. Keyset pagination
   * rather than offset, so that walking the whole table for the export stays cheap on the last
   * page as well as the first, and a re-run resumes from wherever it stopped.
   */
  @Query(
    """
    select c from CellMoveReason c
    where c.bookingId > :bookingId
       or (c.bookingId = :bookingId and c.bedAssignmentsSequence > :bedAssignmentSequence)
    order by c.bookingId, c.bedAssignmentsSequence
    """,
  )
  fun findPageAfter(bookingId: Long, bedAssignmentSequence: Int, pageable: Pageable): List<CellMoveReason>
}
