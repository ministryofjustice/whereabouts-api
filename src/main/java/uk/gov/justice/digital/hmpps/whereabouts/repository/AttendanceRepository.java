package uk.gov.justice.digital.hmpps.whereabouts.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.digital.hmpps.whereabouts.model.Attendance;
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod;

import java.time.LocalDate;
import java.util.Set;

@Repository
public interface AttendanceRepository extends CrudRepository<Attendance, Long> {
    Set<Attendance> findByPrisonIdAndEventLocationIdAndEventDateAndPeriod(String prisonId, Long eventLocationId,
                                                                          LocalDate date, TimePeriod period);
}
