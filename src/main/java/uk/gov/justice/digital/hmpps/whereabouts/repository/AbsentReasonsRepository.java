package uk.gov.justice.digital.hmpps.whereabouts.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason;

import java.util.Set;

@Repository
public interface AbsentReasonsRepository extends CrudRepository<AbsentReason, Long> {
    Set<AbsentReason> findAll();
}
