package uk.gov.justice.digital.hmpps.whereabouts.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason;
import uk.gov.justice.digital.hmpps.whereabouts.model.Attendance;
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface AttendanceRepository extends CrudRepository<Attendance, Long> {
    Set<Attendance> findByPrisonIdAndEventLocationIdAndEventDateAndPeriod(String prisonId, Long eventLocationId,
                                                                          LocalDate date, TimePeriod period);

    Set<Attendance> findByPrisonIdAndBookingIdInAndEventDateAndPeriod(String prisonId, Set<Long> bookingIds, LocalDate date,
                                                                      TimePeriod period);

    Set<Attendance> findByPrisonIdAndBookingIdAndEventIdAndEventDateAndPeriod(String prisonId, Long bookingId, Long eventId, LocalDate date,
                                                                              TimePeriod period);

    Set<Attendance> findByPrisonIdAndBookingIdInAndEventDateBetweenAndPeriodIn(String prisonId, Set<Long> bookingIds, LocalDate from, LocalDate to,
                                                                               Set<TimePeriod> periods);

    Set<Attendance> findByPrisonIdAndEventDateAndPeriodAndAbsentReasonNotNull(String prisonId, LocalDate date, TimePeriod period);

    Set<Attendance> findByPrisonIdAndPeriodAndEventDateBetween(String prisonId, TimePeriod period, LocalDate from, LocalDate to);

    Set<Attendance> findByPrisonIdAndEventDateBetweenAndPeriodIn(String prisonId, LocalDate from, LocalDate to, Set<TimePeriod> periods);

    Set<Attendance> findByPrisonIdAndEventDateBetweenAndPeriodInAndAbsentReason(String prisonId, LocalDate from, LocalDate to, Set<TimePeriod> periods, AbsentReason reason);

    Set<Attendance> findByBookingId(long bookingId);

    List<Attendance> findByBookingIdInAndEventDateBetween(Set<Long> bookingIds, LocalDate from, LocalDate to);
}
