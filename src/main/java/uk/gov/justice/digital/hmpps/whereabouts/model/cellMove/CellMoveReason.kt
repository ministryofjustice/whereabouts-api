package uk.gov.justice.digital.hmpps.whereabouts.model.cellMove

enum class CellMoveReason(val description: String) {
  ADM("Administrative"),
  BEH("Behaviour"),
  CLA("Classification or re-classification"),
  CON("Conflict with other prisoners"),
  LN("Local needs"),
  VP("Vulnerable prisoner")
}
