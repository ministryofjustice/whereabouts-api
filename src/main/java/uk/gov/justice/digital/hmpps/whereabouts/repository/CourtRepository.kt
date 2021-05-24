package uk.gov.justice.digital.hmpps.whereabouts.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.whereabouts.model.Court

interface CourtRepository : JpaRepository<Court, Long>
