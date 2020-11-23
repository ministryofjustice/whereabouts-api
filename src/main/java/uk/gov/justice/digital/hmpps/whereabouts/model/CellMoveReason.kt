package uk.gov.justice.digital.hmpps.whereabouts.model

import lombok.Data
import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.IdClass
import javax.persistence.Table

@Entity
@Data
@IdClass(CellMoveReasonPK::class)
@Table(name = "CELL_MOVE_REASON")
data class CellMoveReason(
  @Id
  @Column(name = "BOOKING_ID", nullable = false, updatable = false)
  val bookingId: Long,
  @Id
  @Column(name = "BED_ASSIGNMENT_SEQUENCE", nullable = false, updatable = false)
  val bedAssignmentsSequence: Int,
  @Column(name = "CASE_NOTE_ID", nullable = false, updatable = false)
  val caseNoteId: Long,
)

@Entity
@Embeddable
class CellMoveReasonPK(bookingId: Long, bedAssigmentSequence: Int) : Serializable {
  @Id
  @Column(name = "BOOKING_ID", nullable = false, updatable = false)
  var bookingId: Long = 0
  @Id
  @Column(name = "BED_ASSIGNMENT_SEQUENCE", nullable = false, updatable = false)
  var bedAssignmentsSequence: Int = 0
}
