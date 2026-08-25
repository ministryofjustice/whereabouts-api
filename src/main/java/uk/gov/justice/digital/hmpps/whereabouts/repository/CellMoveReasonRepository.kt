package uk.gov.justice.digital.hmpps.whereabouts.repository

import org.springframework.data.repository.CrudRepository
import uk.gov.justice.digital.hmpps.whereabouts.model.CellMoveReason
import uk.gov.justice.digital.hmpps.whereabouts.model.CellMoveReasonPK

interface CellMoveReasonRepository : CrudRepository<CellMoveReason, CellMoveReasonPK>
