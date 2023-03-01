package uk.gov.justice.digital.hmpps.whereabouts.model

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.Table
import lombok.Data
import java.io.Serializable

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
data class CellMoveReasonPK(
  @Id
  @Column(name = "BOOKING_ID")
  var bookingId: Long,
  @Id
  @Column(name = "BED_ASSIGNMENT_SEQUENCE")
  var bedAssignmentsSequence: Int,
) : Serializable
