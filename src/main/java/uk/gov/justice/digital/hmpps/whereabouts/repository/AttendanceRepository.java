package uk.gov.justice.digital.hmpps.whereabouts.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason;
import uk.gov.justice.digital.hmpps.whereabouts.model.Attendance;
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod;

import java.time.LocalDate;
import java.util.Set;

@Repository
public interface AttendanceRepository extends CrudRepository<Attendance, Long> {
    Set<Attendance> findByPrisonIdAndEventLocationIdAndEventDateAndPeriod(String prisonId, Long eventLocationId,
                                                                          LocalDate date, TimePeriod period);

    Set<Attendance> findByPrisonIdAndBookingIdInAndEventDateAndPeriod(String prisonId, Set<Long> bookingIds, LocalDate date,
                                                                      TimePeriod period);

    Set<Attendance> findByPrisonIdAndBookingIdAndEventIdAndEventDateAndPeriod(String prisonId, Long bookingId, Long eventId, LocalDate date,
                                                                      TimePeriod period);

    Set<Attendance> findByPrisonIdAndEventDateAndPeriodAndAbsentReasonNotNull(String prisonId, LocalDate date, TimePeriod period);

    Set<Attendance> findByPrisonIdAndPeriodAndEventDateBetween(String prisonId, TimePeriod period, LocalDate from, LocalDate to);

    Set<Attendance> findByPrisonIdAndEventDateBetweenAndPeriodIn(String prisonId, LocalDate from, LocalDate to, Set<TimePeriod> periods);

    Set<Attendance> findByPrisonIdAndEventDateBetweenAndPeriodInAndAbsentReason(String prisonId, LocalDate from, LocalDate to, Set<TimePeriod> periods, AbsentReason reason);

    Set<Attendance> findByBookingId(long bookingId);
}
